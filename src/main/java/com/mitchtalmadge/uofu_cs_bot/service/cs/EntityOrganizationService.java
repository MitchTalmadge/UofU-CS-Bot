package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
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
    private final ChannelService channelService;
    private final RoleService roleService;

    @Autowired
    public EntityOrganizationService(LogService logService,
                                     RoleAssignmentService roleAssignmentService,
                                     ChannelService channelService,
                                     RoleService roleService) {
        this.logService = logService;
        this.roleAssignmentService = roleAssignmentService;
        this.channelService = channelService;
        this.roleService = roleService;
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
        if (!name.toLowerCase().startsWith(Constants.CS_PREFIX.toLowerCase()))
            return false;

        // Ignore those which do not end in a parsable number.
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(name.substring(Constants.CS_PREFIX.length()));
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
            // Name
            String channelName = channel.getName();
            if (!channelName.equals(channelName.toLowerCase()))
                channel.getManager().setName(channelName.toLowerCase()).queue();
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
            // Name
            String roleName = role.getName();
            if (!roleName.equals(roleName.toUpperCase()))
                role.getManager().setName(roleName.toUpperCase()).queue();

            // Permissions
            role.getManager().setPermissions(Constants.CS_ROLE_PERMISSIONS).queue();

            // Color
            role.getManager().setColor(Constants.CS_ROLE_COLOR).queue();

            // Hoisted (Displayed separately)
            role.getManager().setHoisted(Constants.CS_ROLE_HOISTED).queue();

            // Mentionable
            role.getManager().setMentionable(Constants.CS_ROLE_MENTIONABLE).queue();
        });
    }

}
