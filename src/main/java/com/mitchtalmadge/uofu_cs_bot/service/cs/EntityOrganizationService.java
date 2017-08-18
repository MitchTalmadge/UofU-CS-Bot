package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.core.requests.restaction.order.RoleOrderAction;
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
    private final ChannelService channelService;
    private final RoleService roleService;

    @Autowired
    public EntityOrganizationService(LogService logService,
                                     ChannelService channelService,
                                     RoleService roleService) {
        this.logService = logService;
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

        // Order text channels.
        orderChannels(guild, true);

        // Order voice channels.
        orderChannels(guild, false);

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
        List<Channel> organizedChannels = new ArrayList<>();
        // Add non class channels.
        organizedChannels.addAll(partitionedChannels.get(false));
        // Sort class channels before adding
        List<Channel> classChannels = partitionedChannels.get(true);
        classChannels.sort(Comparator.comparing(Channel::getName));
        // Add class channels
        organizedChannels.addAll(classChannels);

        // Get the channel ordering action
        ChannelOrderAction<? extends Channel> channelOrderAction = textChannels ? guild.getController().modifyTextChannelPositions() : guild.getController().modifyVoiceChannelPositions();

        // Order the channels.
        for (int i = 0; i < organizedChannels.size(); i++) {
            // Get the index of the current channel in the order list.
            int currentOrderPosition = channelOrderAction.getCurrentOrder().indexOf(organizedChannels.get(i));

            // Swap the current channel with the channel at the desired position.
            channelOrderAction.selectPosition(currentOrderPosition).swapPosition(i);
        }

        // Submit the changes to order.
        channelOrderAction.queue();
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
        List<Role> organizedRoles = new ArrayList<>();
        // Add non class roles.
        organizedRoles.addAll(partitionedRoles.get(false));
        // Sort class roles before adding
        List<Role> classRoles = partitionedRoles.get(true);
        classRoles.sort(Comparator.comparing(Role::getName));
        // Add class roles
        organizedRoles.addAll(classRoles);

        // Remove @everyone role
        organizedRoles.removeIf(Role::isPublicRole);

        // Get the channel ordering action
        RoleOrderAction roleOrderAction = guild.getController().modifyRolePositions(false);

        // Order the roles.
        for (int i = 0; i < organizedRoles.size(); i++) {
            // Get the index of the current channel in the order list.
            int currentOrderPosition = roleOrderAction.getCurrentOrder().indexOf(organizedRoles.get(i));

            // Swap the current channel with the channel at the desired position.
            roleOrderAction.selectPosition(currentOrderPosition).swapPosition(i);
        }

        // Submit the changes to order.
        roleOrderAction.queue();
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

}
