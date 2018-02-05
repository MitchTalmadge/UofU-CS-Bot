package com.mitchtalmadge.uofu_cs_bot.service.discord.channel;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class ChannelSynchronizationService {

    private final LogService logService;
    private final DiscordService discordService;
    private Set<ChannelSynchronizer> channelSynchronizers;

    /**
     * Whenever another class requests synchronization, this field will be set to true. <br/>
     * On the next scheduled synchronization, this field is checked to determine if synchronization should take place. <br/>
     * Once finished, this field is set to false again.
     * <p>
     * This field is set to true initially so that synchronization occurs at least once on startup.
     */
    private boolean synchronizationRequested = true;

    @Autowired
    public ChannelSynchronizationService(LogService logService,
                                         DiscordService discordService,
                                         Set<ChannelSynchronizer> channelSynchronizers) {
        this.logService = logService;
        this.discordService = discordService;
        this.channelSynchronizers = channelSynchronizers;
    }

    /**
     * Requests that Categories and Channels be synchronized at the next scheduled time.
     */
    public void requestSynchronization() {
        synchronizationRequested = true;
    }

    /**
     * Begins synchronization of Channel Categories, Text Channels, and Voice Channels. <br/>
     * This may involve creating, deleting, modifying, or moving Categories and Channels as needed.
     * <p>
     * Synchronization only takes place if synchronizationRequested is true.
     * <p>
     * This method will fire every 60 seconds, with an initial delay of 15 seconds.
     *
     * @see ChannelSynchronizationService#synchronizationRequested
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    @Async
    protected void synchronize() {

        // Skip synchronization if not requested.
        if (!synchronizationRequested)
            return;

        synchronizationRequested = false;

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
                        category.delete().queue();
                    });
                }

                // Create any requested Categories.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(categoryName -> {
                        logService.logInfo(getClass(), "--> Creating Category: " + categoryName);
                        discordService.getGuild().getController().createCategory(categoryName).queue();
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
            Pair<Collection<TextChannel>, Collection<String>> synchronizationResult = channelSynchronizer.synchronizeTextChannels(discordService.getGuild().getTextChannels());

            if (synchronizationResult != null) {

                // Delete any requested Text Channels.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(textChannel -> {
                        logService.logInfo(getClass(), "--> Deleting Text Channel: " + textChannel.getName());
                        textChannel.delete().queue();
                    });
                }

                // Create any requested Text Channels.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(textChannelName -> {
                        logService.logInfo(getClass(), "--> Creating Text Channel: " + textChannelName);
                        discordService.getGuild().getController().createTextChannel(textChannelName).queue();
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
            Pair<Collection<VoiceChannel>, Collection<String>> synchronizationResult = channelSynchronizer.synchronizeVoiceChannels(discordService.getGuild().getVoiceChannels());

            if (synchronizationResult != null) {

                // Delete any requested Voice Channels.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(voiceChannel -> {
                        logService.logInfo(getClass(), "--> Deleting Voice Channel: " + voiceChannel.getName());
                        voiceChannel.delete().queue();
                    });
                }

                // Create any requested Voice Channels.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(voiceChannelName -> {
                        logService.logInfo(getClass(), "--> Creating Voice Channel: " + voiceChannelName);
                        discordService.getGuild().getController().createVoiceChannel(voiceChannelName).queue();
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
            Collection<ChannelManagerUpdatable> updateResult = channelSynchronizer.updateChannelCategorySettings(discordService.getGuild().getCategories());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(channelManagerUpdatable -> {
                    channelManagerUpdatable.update().queue();
                });
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
            Collection<ChannelManagerUpdatable> updateResult = channelSynchronizer.updateTextChannelSettings(discordService.getGuild().getTextChannels());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(channelManagerUpdatable -> {
                    channelManagerUpdatable.update().queue();
                });
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
            Collection<ChannelManagerUpdatable> updateResult = channelSynchronizer.updateVoiceChannelSettings(discordService.getGuild().getVoiceChannels());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(channelManagerUpdatable -> {
                    channelManagerUpdatable.update().queue();
                });
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
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateResult = channelSynchronizer.updateChannelCategoryPermissions(discordService.getGuild().getCategories());

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().queue());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::queue);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(permOverrideManagerUpdatable -> {
                        permOverrideManagerUpdatable.update().queue();
                    });
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
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateResult = channelSynchronizer.updateTextChannelPermissions(discordService.getGuild().getTextChannels());

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().queue());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::queue);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(permOverrideManagerUpdatable -> {
                        permOverrideManagerUpdatable.update().queue();
                    });
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
            Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateResult = channelSynchronizer.updateVoiceChannelPermissions(discordService.getGuild().getVoiceChannels());

            if (updateResult != null) {

                if (updateResult.getLeft() != null) {

                    // Delete any requested Overrides.
                    if (updateResult.getLeft().getLeft() != null) {
                        updateResult.getLeft().getLeft().forEach(permissionOverride -> permissionOverride.delete().queue());
                    }

                    // Create any requested Overrides.
                    if (updateResult.getLeft().getRight() != null) {
                        updateResult.getLeft().getRight().forEach(RestAction::queue);
                    }

                }

                // Queue any requested Override Updates.
                if (updateResult.getRight() != null) {
                    updateResult.getRight().forEach(permOverrideManagerUpdatable -> {
                        permOverrideManagerUpdatable.update().queue();
                    });
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

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            List<TextChannel> updateResult = channelSynchronizer.updateTextChannelOrdering(discordService.getGuild().getTextChannels());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                DiscordUtils.orderEntities(discordService.getGuild().getController().modifyTextChannelPositions(), updateResult);
            }
        });
    }

    /**
     * Updates the order of Voice Channels in the Guild.
     */
    private void updateVoiceChannelOrdering() {
        logService.logInfo(getClass(), "Updating Voice Channel Ordering...");

        channelSynchronizers.forEach(channelSynchronizer -> {
            // Perform Update
            List<VoiceChannel> updateResult = channelSynchronizer.updateVoiceChannelOrdering(discordService.getGuild().getVoiceChannels());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                DiscordUtils.orderEntities(discordService.getGuild().getController().modifyVoiceChannelPositions(), updateResult);
            }
        });
    }

}








