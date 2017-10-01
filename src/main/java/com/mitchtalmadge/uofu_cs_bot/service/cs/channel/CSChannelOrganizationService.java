package com.mitchtalmadge.uofu_cs_bot.service.cs.channel;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CSChannelOrganizationService {

    private final LogService logService;
    private final DiscordService discordService;

    /**
     * Determines if organization should take place this tick.
     * Will be changed to true when requested.
     */
    private boolean organizationRequested = false;

    @Autowired
    public CSChannelOrganizationService(LogService logService,
                                        DiscordService discordService) {
        this.logService = logService;
        this.discordService = discordService;
    }

    /**
     * Requests that roles be organized on the next tick.
     */
    public void requestOrganization() {
        this.organizationRequested = true;
    }

    /**
     * Organizes channels and ensures that they have the proper permissions.
     * <p>
     * Will only organize when requested via the requestOrganization method.
     * <p>
     * Scheduled to run every minute, with a 15 second delay on startup.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    @Async
    protected void organize() {
        // Leave immediately if no organization has been requested.
        if (!organizationRequested) {
            return;
        }

        logService.logInfo(getClass(), "Organizing Channels...");

        // Update settings
        updateTextChannelSettings();
        updateVoiceChannelSettings();

        // Update orders
        orderTextChannels();
        orderVoiceChannels();

        //TODO: Category permissions (hide from anyone not in a CS class role)

        logService.logInfo(getClass(), "Organization of Channels Completed.");
        organizationRequested = false;
    }

    /**
     * Updates the settings of all text class channels.
     */
    private void updateTextChannelSettings() {
        List<TextChannel> channels = discordService.getGuild().getTextChannels();

        channels.forEach(channel -> {
            try {
                CSClass channelClass = new CSClass(channel.getName());

                ChannelManagerUpdatable updater = channel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(channelClass));

                // Category
                updater = updater.getParentField().setValue(getClassesCategory());

                // Permissions
                updateChannelPermissions(channel, channelClass);

                // NSFW Off
                updater = updater.getNSFWField().setValue(false);

                updater.update().queue();
            } catch (CSClass.InvalidClassNameException ignored) {
                // This is not a class channel
            }
        });
    }

    /**
     * Updates the settings of all text class channels.
     */
    private void updateVoiceChannelSettings() {
        List<VoiceChannel> channels = discordService.getGuild().getVoiceChannels();

        channels.forEach(channel -> {
            try {
                CSClass channelClass = new CSClass(channel.getName());

                ChannelManagerUpdatable updater = channel.getManagerUpdatable();

                // Name
                updater = updater.getNameField().setValue(CSNamingConventions.toChannelName(channelClass));

                // Category
                updater = updater.getParentField().setValue(getClassesCategory());

                // Permissions
                updateChannelPermissions(channel, channelClass);

                // Bitrate and User Limit
                updater = updater
                        .getBitrateField().setValue(CSConstants.CS_CHANNEL_VOICE_BITRATE)
                        .getUserLimitField().setValue(CSConstants.CS_CHANNEL_VOICE_USERLIMIT);

                updater.update().queue();
            } catch (CSClass.InvalidClassNameException ignored) {
                // This is not a class channel
            }
        });
    }

    /**
     * @return The category that all class channels belong in.
     */
    private Category getClassesCategory() {
        return discordService.getGuild().getCategoriesByName(CSConstants.CS_CHANNEL_CATEGORY, false).get(0);
    }

    /**
     * Ensures that permissions are set correctly for a specific class channel.
     *
     * @param channel      The channel.
     * @param channelClass The channel's associated CSClass instance.
     */
    private void updateChannelPermissions(Channel channel, CSClass channelClass) {
        // Keeps track of whether the channel has a permission override for each suffix, and for @everyone (null key).
        Map<CSSuffix, Boolean> overrideDetectionMap = new HashMap<>();

        // Check each permission override.
        channel.getPermissionOverrides().forEach(override -> {
            // Delete all member overrides.
            if (override.isMemberOverride()) {
                override.delete().queue();
                return;
            }

            Role role = override.getRole();

            if (role.isPublicRole()) { // @everyone role
                overrideDetectionMap.put(null, true);

                PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                // Clear all permissions
                manager = manager.clear(Permission.ALL_PERMISSIONS);

                // Deny viewing
                manager = manager.deny(Permission.VIEW_CHANNEL);

                manager.update().queue();
            } else {
                try { // Class role
                    CSClass roleClass = new CSClass(role.getName());
                    CSSuffix roleSuffix = CSSuffix.fromClassName(role.getName());

                    overrideDetectionMap.put(roleSuffix, true);

                    PermOverrideManagerUpdatable manager = override.getManagerUpdatable();

                    // Clear all permissions
                    manager = manager.clear(Permission.ALL_PERMISSIONS);

                    // Allow viewing
                    manager = manager.grant(Permission.VIEW_CHANNEL);

                    manager.update().queue();
                } catch (CSClass.InvalidClassNameException ignored) {
                    // This is not a class role. Delete its override.
                    override.delete().queue();
                }
            }
        });

        // Create the channel's @everyone role permission override if it does not exist.
        if (!overrideDetectionMap.getOrDefault(null, false)) {
            PermissionOverrideAction override = channel.createPermissionOverride(discordService.getGuild().getPublicRole());
            override = override.setDeny(Permission.VIEW_CHANNEL);
            override.queue();
        }

        // Create the channel's suffix role permission overrides if they do not exist.
        for (CSSuffix suffix : CSSuffix.values()) {
            if (!overrideDetectionMap.getOrDefault(suffix, false)) {
                PermissionOverrideAction override = channel.createPermissionOverride(
                        discordService.getGuild().getRolesByName(CSNamingConventions.toRoleName(channelClass, suffix), true).get(0)
                );
                override = override.setAllow(Permission.VIEW_CHANNEL);
                override.queue();
            }
        }
    }

    /**
     * Orders the text class channels.
     */
    private void orderTextChannels() {
        // Get all the channels.
        List<TextChannel> channels = discordService.getGuild().getTextChannels();

        // Partition the channels into two, based on whether or not they are class channels.
        Map<Boolean, List<Channel>> partitionedChannels = channels.stream().collect(Collectors.partitioningBy(channel -> {
            // Attempt to parse the channel as a CS Class. If successful, return true.
            try {
                new CSClass(channel.getName());
                return true;
            } catch (CSClass.InvalidClassNameException ignored) {
                return false;
            }
        }));

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
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyTextChannelPositions(), orderedChannels);
    }

    /**
     * Orders the voice class channels.
     */
    private void orderVoiceChannels() {
        // Get all the channels.
        List<VoiceChannel> channels = getClassesCategory().getVoiceChannels();

        // Partition the channels into two, based on whether or not they are class channels.
        Map<Boolean, List<Channel>> partitionedChannels = channels.stream().collect(Collectors.partitioningBy(channel -> {
            // Attempt to parse the channel as a CS Class. If successful, return true.
            try {
                new CSClass(channel.getName());
                return true;
            } catch (CSClass.InvalidClassNameException ignored) {
                return false;
            }
        }));

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
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyVoiceChannelPositions(), orderedChannels);
    }


}
