package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.core.requests.restaction.order.OrderAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Organizes the channels and roles, assigning permissions as necessary.
 */
@Service
public class EntityOrganizationService {

    /**
     * The guilds that are currently requesting organization.
     */
    private final Set<Guild> organizationRequests = new HashSet<>();

    private final LogService logService;
    private final RoleAssignmentService roleAssignmentService;

    @Autowired
    public EntityOrganizationService(LogService logService,
                                     RoleAssignmentService roleAssignmentService) {
        this.logService = logService;
        this.roleAssignmentService = roleAssignmentService;
    }

    /**
     * Requests that this guild be organized on the next scheduled organization.
     * Will not happen immediately.
     *
     * @param guild The guild to organize.
     */
    public void requestOrganization(Guild guild) {
        organizationRequests.add(guild);
    }

    /**
     * Organizes entities and ensures that they have the proper permissions.
     * Afterwards, calls on the {@link RoleAssignmentService} to re-check assignments.
     * <p>
     * Will only organize the guilds which have previously requested it
     * via the {@link EntityOrganizationService#requestOrganization(Guild)} method.
     * <p>
     * Scheduled to run every minute, with a 15 second delay on startup.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    @Async
    protected void organizeAndVerifyEntities() {
        logService.logDebug(getClass(), "Checking for Organization Requests...");

        // Handle each requested organization.
        for (Guild guild : organizationRequests) {
            logService.logInfo(getClass(), "Organizing Entities for Guild '" + guild.getName() + "'...");

            // Organize the guild.
            organizeGuild(guild);

            logService.logInfo(getClass(), "Organization Complete.");

            // Update the role assignments for the guild.
            roleAssignmentService.updateRoleAssignments(guild);
        }

        // Clear any old organization requests.
        organizationRequests.clear();
    }

    /**
     * Organizes a single Guild.
     *
     * @param guild The guild to organize.
     */
    private void organizeGuild(Guild guild) {

        // Update text channel settings.
        updateChannelSettings(guild, true);

        // Order text channels.
        orderChannels(guild, true);

        // Update voice channel settings.
        updateChannelSettings(guild, false);

        // Order voice channels.
        orderChannels(guild, false);

        // Update role settings.
        updateRoleSettings(guild);

        // Order roles
        orderRoles(guild);
    }

    /**
     * Orders the channels of a guild.
     *
     * @param guild        The guild.
     * @param textChannels True to order the text channels, false to order the voice channels.
     */
    private void orderChannels(Guild guild, boolean textChannels) {
        // Get all the channels.
        List<Channel> channels = channelService.getAllChannels(guild, textChannels ? ChannelType.TEXT : ChannelType.VOICE);

        // Partition the channels into two, based on whether or not they are class channels.
        Map<Boolean, List<Channel>> partitionedChannels = channels.stream().collect(Collectors.partitioningBy(c -> isClassName(c.getName())));

        // Combine the channels back together with the class channels in order at the bottom.
        // Do not re-order the other channels. We do not care about their order.
        List<Channel> orderedChannels = new ArrayList<>();
        // Add non class channels.
        orderedChannels.addAll(partitionedChannels.get(false));
        // Sort class channels before adding
        List<Channel> classChannels = partitionedChannels.get(true);
        classChannels.sort(Comparator.comparing(Channel::getName));
        // Add class channels
        orderedChannels.addAll(classChannels);

        // Perform ordering.
        orderEntities(textChannels
                ? guild.getController().modifyTextChannelPositions()
                : guild.getController().modifyVoiceChannelPositions(), orderedChannels);
    }

    /**
     * Orders the roles of a guild.
     *
     * @param guild The guild.
     */
    private void orderRoles(Guild guild) {
        // Get all the roles.
        List<Role> roles = roleService.getAllRoles(guild);

        // Partition the roles into two, based on whether or not they are class roles.
        Map<Boolean, List<Role>> partitionedRoles = roles.stream().collect(Collectors.partitioningBy(r -> isClassName(r.getName())));

        // Combine the roles back together with the class roles in order at the bottom.
        // Do not re-order the other roles. We do not care about their order.
        List<Role> orderedRoles = new ArrayList<>();
        // Add non class roles.
        orderedRoles.addAll(partitionedRoles.get(false));
        // Sort class roles before adding
        List<Role> classRoles = partitionedRoles.get(true);
        classRoles.sort(Comparator.comparing(o -> o.getName().toUpperCase())); // Ignore case
        // Add class roles
        orderedRoles.addAll(classRoles);

        // Remove @everyone role, as its order cannot be changed.
        orderedRoles.removeIf(Role::isPublicRole);

        // Perform ordering.
        orderEntities(guild.getController().modifyRolePositions(false), orderedRoles);
    }

    /**
     * Determines if the name is for a CS class.
     *
     * @param name The name.
     * @return True if the name is for a CS class (like cs-3500).
     */
    private boolean isClassName(String name) {
        // Ignore those which do not start with the CS prefix.
        if (!name.toLowerCase().startsWith(CSConstants.CS_PREFIX.toLowerCase()))
            return false;

        // Ignore those which do not end in a parsable number.
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(name.substring(CSConstants.CS_PREFIX.length()));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Orders the entities, with the provided order action, to their order in the provided list.
     * Submits the order to Discord.
     *
     * @param orderAction The order action to use.
     * @param order       The desired order of the entities.
     * @param <E>         The entity type.
     * @param <O>         The OrderAction type.
     */
    @SuppressWarnings("unchecked")
    private <E, O extends OrderAction<? extends E, ? extends O>> void orderEntities(O orderAction, List<E> order) {
        // Order the channels.
        for (int i = 0; i < order.size(); i++) {
            // Get the index of the current channel in the order list.
            int currentOrderPosition = orderAction.getCurrentOrder().indexOf(order.get(i));

            // Swap the current channel with the channel at the desired position.
            orderAction.selectPosition(currentOrderPosition).swapPosition(i);
        }

        // Submit the changes to order.
        orderAction.queue();
    }

    /**
     * Updates the settings of all class channels, including:
     * - Name
     * - Permissions (including visibility)
     *
     * @param guild        The guild.
     * @param textChannels True to update text channels, false to update voice channels.
     */
    private void updateChannelSettings(Guild guild, boolean textChannels) {
        List<Channel> channels = channelService.getAllChannels(guild, textChannels ? ChannelType.TEXT : ChannelType.VOICE);

        channels.stream().filter(channel -> isClassName(channel.getName())).forEach(channel -> {
            ChannelManagerUpdatable channelManager = channel.getManagerUpdatable();

            // Name
            String channelName = channel.getName();
            if (!channelName.equals(channelName.toLowerCase()))
                channelManager = channelManager.getNameField().setValue(channelName.toLowerCase());

            // Permissions

            // Create permission override for the channel role if not already exists.
            List<PermissionOverride> permissionOverrides = channel.getPermissionOverrides();
            // Keeps track of whether the channel has a permission override for @everyone and for its own role, respectively.
            final boolean[] hasPermissionOverride = new boolean[2];
            permissionOverrides.forEach(override -> {
                // Delete member overrides
                if (override.isMemberOverride()) {
                    override.delete().queue();
                    return;
                }

                // @everyone - Deny read/connect, inherit all others.
                if (override.getRole().isPublicRole()) {
                    hasPermissionOverride[0] = true;
                    PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                    // Clear all permissions
                    manager = manager.clear(Permission.ALL_PERMISSIONS);

                    // Deny viewing
                    manager = manager.deny(Permission.VIEW_CHANNEL);

                    manager.update().queue();
                    return;
                }

                // Channel's own role - Grant read/connect, inherit all others.
                if (override.getRole().getName().equalsIgnoreCase(channelName)) {
                    hasPermissionOverride[1] = true;
                    PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                    // Clear all permissions
                    manager = manager.clear(Permission.ALL_PERMISSIONS);

                    // Allow viewing
                    manager = manager.grant(Permission.VIEW_CHANNEL);

                    manager.update().queue();
                    return;
                }

                // Delete all other overrides.
                override.delete().queue();
            });

            // Create the channel's @everyone role permission override if not exists.
            if (!hasPermissionOverride[0]) {
                PermissionOverrideAction override = channel.createPermissionOverride(roleService.getEveryoneRole(guild));
                override = override.setDeny(Permission.VIEW_CHANNEL);
                override.queue();
            }

            // Create the channel's own role permission override if not exists.
            if (!hasPermissionOverride[1]) {
                PermissionOverrideAction override = channel.createPermissionOverride(roleService.getRoleByName(guild, channelName));
                override = override.setAllow(Permission.VIEW_CHANNEL);
                override.queue();
            }

            // NSFW Off
            if (textChannels)
                channelManager = channelManager.getNSFWField().setValue(false);

            // Bitrate and User Limit
            if (!textChannels) {
                channelManager = channelManager
                        .getBitrateField().setValue(CSConstants.CS_CHANNEL_VOICE_BITRATE)
                        .getUserLimitField().setValue(CSConstants.CS_CHANNEL_VOICE_USERLIMIT);
            }

            channelManager.update().queue();
        });
    }

    /**
     * Updates the settings of all class roles, including:
     * - Name
     * - Permissions
     * - Colors
     * - Mentionable status
     * - Hoisted status
     *
     * @param guild The guild.
     */
    private void updateRoleSettings(Guild guild) {
        List<Role> roles = roleService.getAllRoles(guild);

        roles.stream().filter(role -> isClassName(role.getName())).forEach(role -> {
            RoleManagerUpdatable roleManager = role.getManagerUpdatable();

            // Name
            String roleName = role.getName();
            if (!roleName.equals(roleName.toLowerCase()))
                roleManager.getNameField().setValue(roleName.toLowerCase());

            // Permissions
            roleManager.getPermissionField().setPermissions(CSConstants.CS_ROLE_PERMISSIONS);

            // Color
            roleManager.getColorField().setValue(CSConstants.CS_ROLE_COLOR);

            // Hoisted (Displayed separately)
            roleManager.getHoistedField().setValue(CSConstants.CS_ROLE_HOISTED);

            // Mentionable
            roleManager.getMentionableField().setValue(CSConstants.CS_ROLE_MENTIONABLE);

            roleManager.update().queue();
        });
    }

}
