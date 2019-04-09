package com.mitchtalmadge.uofu_cs_bot.service.discord.channel;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChannelSynchronizationService {

    private final LogService logService;
    private final DiscordService discordService;
    private Set<ChannelSynchronizer> channelSynchronizers;

    @Autowired
    public ChannelSynchronizationService(LogService logService,
                                         DiscordService discordService,
                                         Set<ChannelSynchronizer> channelSynchronizers) {
        this.logService = logService;
        this.discordService = discordService;
        this.channelSynchronizers = channelSynchronizers;
    }

    /**
     * Begins synchronization of Channel Categories, Text Channels, and Voice Channels. <br/>
     * This may involve creating, deleting, modifying, or moving Categories and Channels as needed.
     */
    public void synchronize() {

        logService.logInfo(getClass(), "Beginning Synchronization as Requested.");

        // Creation and Deletion
        synchronizeChannelCategories();
        synchronizeTextChannels();
        synchronizeVoiceChannels();

        // Settings
        updateChannelCategorySettings();
        updateTextChannelSettings();
        updateVoiceChannelSettings();

        // Permissions
        updateChannelCategoryPermissions();
        updateTextChannelPermissions();
        updateVoiceChannelPermissions();

        // Order
        updateChannelCategoryOrdering();
        updateTextChannelOrdering();
        updateVoiceChannelOrdering();
    }

    /**
     * Creates and/or deletes Channel Categories as necessary.
     */
    private void synchronizeChannelCategories() {
        logService.logInfo(getClass(), "Synchronizing Channel Categories...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform synchronization
            Pair<Collection<Category>, Collection<String>> synchronizationResult = channelSynchronizer.synchronizeChannelCategories(discordService.getGuild().getCategories());

            if (synchronizationResult != null) {

                // Delete any requested Categories.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(category -> {
                        logService.logInfo(getClass(), "--> Deleting Category: " + category.getName());
                        category.delete().complete();
                    });
                }

                // Create any requested Categories.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(categoryName -> {
                        logService.logInfo(getClass(), "--> Creating Category: " + categoryName);
                        discordService.getGuild().getController().createCategory(categoryName).complete();
                    });
                }

            }
        });
    }

    /**
     * Creates and/or deletes Text Channels as necessary.
     */
    private void synchronizeTextChannels() {
        logService.logInfo(getClass(), "Synchronizing Text Channels...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform synchronization
            Pair<Collection<TextChannel>, Collection<String>> synchronizationResult = channelSynchronizer.synchronizeTextChannels(getFilteredTextChannelsForSynchronizer(channelSynchronizer));

            if (synchronizationResult != null) {

                // Delete any requested Text Channels.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(textChannel -> {
                        logService.logInfo(getClass(), "--> Deleting Text Channel: " + textChannel.getName());
                        textChannel.delete().complete();
                    });
                }

                // Create any requested Text Channels.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(textChannelName -> {
                        logService.logInfo(getClass(), "--> Creating Text Channel: " + textChannelName);
                        discordService.getGuild().getController().createTextChannel(textChannelName).complete();
                    });
                }

            }
        });
    }

    /**
     * Creates and/or deletes Voice Channels as necessary.
     */
    private void synchronizeVoiceChannels() {
        logService.logInfo(getClass(), "Synchronizing Voice Channels...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform synchronization
            Pair<Collection<VoiceChannel>, Collection<String>> synchronizationResult = channelSynchronizer.synchronizeVoiceChannels(getFilteredVoiceChannelsForSynchronizer(channelSynchronizer));

            if (synchronizationResult != null) {

                // Delete any requested Voice Channels.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(voiceChannel -> {
                        logService.logInfo(getClass(), "--> Deleting Voice Channel: " + voiceChannel.getName());
                        voiceChannel.delete().complete();
                    });
                }

                // Create any requested Voice Channels.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(voiceChannelName -> {
                        logService.logInfo(getClass(), "--> Creating Voice Channel: " + voiceChannelName);
                        discordService.getGuild().getController().createVoiceChannel(voiceChannelName).complete();
                    });
                }

            }
        });
    }

    /**
     * Ensures that all Channel Categories have the correct settings.
     */
    private void updateChannelCategorySettings() {
        logService.logInfo(getClass(), "Updating Channel Category Settings...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Collection<ChannelManager> updateResult = channelSynchronizer.updateChannelCategorySettings(discordService.getGuild().getCategories());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(RestAction::complete);
            }
        });
    }

    /**
     * Ensures that all Text Channels have the correct settings.
     */
    private void updateTextChannelSettings() {
        logService.logInfo(getClass(), "Updating Text Channel Settings...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Collection<ChannelManager> updateResult = channelSynchronizer.updateTextChannelSettings(getFilteredTextChannelsForSynchronizer(channelSynchronizer));

            // Queue any requested managers.
            if (updateResult != null) {
                updateResult.forEach(RestAction::complete);
            }
        });
    }

    /**
     * Ensures that all Voice Channels have the correct settings.
     */
    private void updateVoiceChannelSettings() {
        logService.logInfo(getClass(), "Updating Voice Channel Settings...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Collection<ChannelManager> updateResult = channelSynchronizer.updateVoiceChannelSettings(getFilteredVoiceChannelsForSynchronizer(channelSynchronizer));

            // Queue any requested managers.
            if (updateResult != null) {
                updateResult.forEach(RestAction::complete);
            }
        });
    }

    /**
     * Ensures that all Channel Categories have the correct permissions.
     */
    private void updateChannelCategoryPermissions() {
        logService.logInfo(getClass(), "Updating Channel Category Permissions...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateResult = channelSynchronizer.updateChannelCategoryPermissions(discordService.getGuild().getCategories());

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().complete());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::complete);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(RestAction::complete);
                }
            }
        });
    }

    /**
     * Ensures that all Text Channels have the correct permissions.
     */
    private void updateTextChannelPermissions() {
        logService.logInfo(getClass(), "Updating Text Channel Permissions...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateResult = channelSynchronizer.updateTextChannelPermissions(getFilteredTextChannelsForSynchronizer(channelSynchronizer));

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().complete());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::complete);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(RestAction::complete);
                }
            }
        });
    }

    /**
     * Ensures that all Voice Channels have the correct permissions.
     */
    private void updateVoiceChannelPermissions() {
        logService.logInfo(getClass(), "Updating Voice Channel Permissions...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateResult = channelSynchronizer.updateVoiceChannelPermissions(getFilteredVoiceChannelsForSynchronizer(channelSynchronizer));

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().complete());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::complete);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(RestAction::complete);
                }
            }
        });
    }

    /**
     * Updates the order of Channel Categories in the Guild.
     */
    private void updateChannelCategoryOrdering() {
        logService.logInfo(getClass(), "Updating Channel Category Ordering...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            List<Category> updateResult = channelSynchronizer.updateChannelCategoryOrdering(discordService.getGuild().getCategories());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                DiscordUtils.orderEntities(discordService.getGuild().getController().modifyCategoryPositions(), updateResult);
            }
        });
    }

    /**
     * Updates the order of Text Channels in the Guild.
     */
    private void updateTextChannelOrdering() {
        logService.logInfo(getClass(), "Updating Text Channel Ordering...");

        // Will contain all the roles in their sorted order.
        List<TextChannel> sortedChannels = new ArrayList<>();

        // Add each synchronizer's sorted channels.
        channelSynchronizers
                .stream()
                .sorted(Comparator.comparingInt(ChannelSynchronizer::getOrderingPriority).thenComparing(ChannelSynchronizer::getChannelPrefix))
                .forEach(channelSynchronizer -> {
                    // Perform Update
                    List<TextChannel> updateResult = channelSynchronizer.updateTextChannelOrdering(getFilteredTextChannelsForSynchronizer(channelSynchronizer));

                    // Store results
                    if (updateResult != null) {
                        sortedChannels.addAll(updateResult);
                    }
                });

        // Find any un-sorted roles and place them at the beginning of the sorted roles list.
        sortedChannels.addAll(0,
                discordService.getGuild().getTextChannels()
                        .stream()
                        .filter(channel -> !sortedChannels.contains(channel))
                        .collect(Collectors.toList()));

        // Perform ordering.
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyTextChannelPositions(), sortedChannels);
    }

    /**
     * Updates the order of Voice Channels in the Guild.
     */
    private void updateVoiceChannelOrdering() {
        logService.logInfo(getClass(), "Updating Voice Channel Ordering...");

        // Will contain all the roles in their sorted order.
        List<VoiceChannel> sortedChannels = new ArrayList<>();

        // Add each synchronizer's sorted channels.
        channelSynchronizers
                .stream()
                .sorted(Comparator.comparingInt(ChannelSynchronizer::getOrderingPriority).thenComparing(ChannelSynchronizer::getChannelPrefix))
                .forEach(channelSynchronizer -> {
                    // Perform Update
                    List<VoiceChannel> updateResult = channelSynchronizer.updateVoiceChannelOrdering(getFilteredVoiceChannelsForSynchronizer(channelSynchronizer));

                    // Store results
                    if (updateResult != null) {
                        sortedChannels.addAll(updateResult);
                    }
                });

        // Find any un-sorted roles and place them at the beginning of the sorted roles list.
        sortedChannels.addAll(0,
                discordService.getGuild().getVoiceChannels()
                        .stream()
                        .filter(channel -> !sortedChannels.contains(channel))
                        .collect(Collectors.toList()));

        // Perform ordering.
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyVoiceChannelPositions(), sortedChannels);
    }

    /**
     * Filters and returns the text channels that are requested by the given synchronizer.
     *
     * @param channelSynchronizer The synchronizer.
     * @return The filtered text channels.
     */
    private List<TextChannel> getFilteredTextChannelsForSynchronizer(ChannelSynchronizer channelSynchronizer) {
        return discordService.getGuild().getTextChannels().stream()
                // Ignore case when filtering.
                .filter(channel -> channel.getName().toLowerCase().startsWith(channelSynchronizer.getChannelPrefix().toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Filters and returns the voice channels that are requested by the given synchronizer.
     *
     * @param channelSynchronizer The synchronizer.
     * @return The filtered voice channels.
     */
    private List<VoiceChannel> getFilteredVoiceChannelsForSynchronizer(ChannelSynchronizer channelSynchronizer) {
        return discordService.getGuild().getVoiceChannels().stream()
                // Ignore case when filtering.
                .filter(channel -> channel.getName().toLowerCase().startsWith(channelSynchronizer.getChannelPrefix().toLowerCase()))
                .collect(Collectors.toList());
    }

}








