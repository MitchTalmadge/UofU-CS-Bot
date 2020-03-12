package com.mitchtalmadge.uofu_cs_bot.service.discord.channel;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
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
  public ChannelSynchronizationService(
      LogService logService,
      DiscordService discordService,
      Set<ChannelSynchronizer> channelSynchronizers) {
    this.logService = logService;
    this.discordService = discordService;
    this.channelSynchronizers = channelSynchronizers;
  }

  /**
   * Begins synchronization of Channel Categories & Text Channels. <br>
   * This may involve creating, deleting, modifying, or moving Categories and Channels as needed.
   */
  public void synchronize() {

    logService.logInfo(getClass(), "Beginning Synchronization as Requested.");

    // Creation and Deletion
    synchronizeChannelCategories();
    synchronizeTextChannels();

    // Settings
    updateChannelCategorySettings();
    updateTextChannelSettings();

    // Permissions
    updateChannelCategoryPermissions();
    updateTextChannelPermissions();

    // Order
    updateChannelCategoryOrdering();
    updateTextChannelOrdering();
  }

  /** Creates and/or deletes Channel Categories as necessary. */
  private void synchronizeChannelCategories() {
    logService.logInfo(getClass(), "Synchronizing Channel Categories...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform synchronization
          Pair<Collection<Category>, Collection<String>> synchronizationResult =
              channelSynchronizer.synchronizeChannelCategories(
                  discordService.getGuild().getCategories());

          if (synchronizationResult != null) {

            // Delete any requested Categories.
            if (synchronizationResult.getLeft() != null) {
              synchronizationResult
                  .getLeft()
                  .forEach(
                      category -> {
                        logService.logInfo(
                            getClass(), "--> Deleting Category: " + category.getName());
                        category.delete().complete();
                      });
            }

            // Create any requested Categories.
            if (synchronizationResult.getRight() != null) {
              synchronizationResult
                  .getRight()
                  .forEach(
                      categoryName -> {
                        logService.logInfo(getClass(), "--> Creating Category: " + categoryName);
                        discordService
                            .getGuild()
                            .createCategory(categoryName)
                            .complete();
                      });
            }
          }
        });
  }

  /** Creates and/or deletes Text Channels as necessary. */
  private void synchronizeTextChannels() {
    logService.logInfo(getClass(), "Synchronizing Text Channels...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform synchronization
          Pair<Collection<TextChannel>, Collection<String>> synchronizationResult =
              channelSynchronizer.synchronizeTextChannels(
                  getFilteredTextChannelsForSynchronizer(channelSynchronizer));

          if (synchronizationResult != null) {

            // Delete any requested Text Channels.
            if (synchronizationResult.getLeft() != null) {
              synchronizationResult
                  .getLeft()
                  .forEach(
                      textChannel -> {
                        logService.logInfo(
                            getClass(), "--> Deleting Text Channel: " + textChannel.getName());
                        textChannel.delete().complete();
                      });
            }

            // Create any requested Text Channels.
            if (synchronizationResult.getRight() != null) {
              synchronizationResult
                  .getRight()
                  .forEach(
                      textChannelName -> {
                        logService.logInfo(
                            getClass(), "--> Creating Text Channel: " + textChannelName);
                        discordService
                            .getGuild()
                            .createTextChannel(textChannelName)
                            .complete();
                      });
            }
          }
        });
  }

  /** Ensures that all Channel Categories have the correct settings. */
  private void updateChannelCategorySettings() {
    logService.logInfo(getClass(), "Updating Channel Category Settings...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform Update
          Collection<ChannelManager> updateResult =
              channelSynchronizer.updateChannelCategorySettings(
                  discordService.getGuild().getCategories());

          // Queue any requested Updatable instances.
          if (updateResult != null) {
            updateResult.forEach(RestAction::queue);
          }
        });
  }

  /** Ensures that all Text Channels have the correct settings. */
  private void updateTextChannelSettings() {
    logService.logInfo(getClass(), "Updating Text Channel Settings...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform Update
          Collection<ChannelManager> updateResult =
              channelSynchronizer.updateTextChannelSettings(
                  getFilteredTextChannelsForSynchronizer(channelSynchronizer));

          // Queue any requested managers.
          if (updateResult != null) {
            updateResult.forEach(RestAction::queue);
          }
        });
  }

  /** Ensures that all Channel Categories have the correct permissions. */
  private void updateChannelCategoryPermissions() {
    logService.logInfo(getClass(), "Updating Channel Category Permissions...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform Update
          Pair<
                  Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>,
                  Collection<PermissionOverrideAction>>
              updateResult =
                  channelSynchronizer.updateChannelCategoryPermissions(
                      discordService.getGuild().getCategories());

          if (updateResult != null) {

            if (updateResult.getLeft() != null) {

              // Delete any requested Overrides.
              if (updateResult.getLeft().getLeft() != null) {
                updateResult
                    .getLeft()
                    .getLeft()
                    .forEach(permissionOverride -> permissionOverride.delete().queue());
              }

              // Create any requested Overrides.
              if (updateResult.getLeft().getRight() != null) {
                updateResult.getLeft().getRight().forEach(RestAction::queue);
              }
            }

            // Queue any requested Override Updates.
            if (updateResult.getRight() != null) {
              updateResult.getRight().forEach(RestAction::queue);
            }
          }
        });
  }

  /** Ensures that all Text Channels have the correct permissions. */
  private void updateTextChannelPermissions() {
    logService.logInfo(getClass(), "Updating Text Channel Permissions...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform Update
          Pair<
                  Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>,
                  Collection<PermissionOverrideAction>>
              updateResult =
                  channelSynchronizer.updateTextChannelPermissions(
                      getFilteredTextChannelsForSynchronizer(channelSynchronizer));

          if (updateResult != null) {

            if (updateResult.getLeft() != null) {

              // Delete any requested Overrides.
              if (updateResult.getLeft().getLeft() != null) {
                updateResult
                    .getLeft()
                    .getLeft()
                    .forEach(permissionOverride -> permissionOverride.delete().queue());
              }

              // Create any requested Overrides.
              if (updateResult.getLeft().getRight() != null) {
                updateResult.getLeft().getRight().forEach(RestAction::queue);
              }
            }

            // Queue any requested Override Updates.
            if (updateResult.getRight() != null) {
              updateResult.getRight().forEach(RestAction::queue);
            }
          }
        });
  }

  /** Updates the order of Channel Categories in the Guild. */
  private void updateChannelCategoryOrdering() {
    logService.logInfo(getClass(), "Updating Channel Category Ordering...");

    channelSynchronizers.forEach(
        channelSynchronizer -> {
          // Perform Update
          List<Category> updateResult =
              channelSynchronizer.updateChannelCategoryOrdering(
                  discordService.getGuild().getCategories());

          // Queue any requested Updatable instances.
          if (updateResult != null) {
            DiscordUtils.orderEntities(
                discordService.getGuild().modifyCategoryPositions(), updateResult);
          }
        });
  }

  /** Updates the order of Text Channels in the Guild. */
  private void updateTextChannelOrdering() {
    logService.logInfo(getClass(), "Updating Text Channel Ordering...");

    // Will contain all the roles in their sorted order.
    List<TextChannel> sortedChannels = new ArrayList<>();

    // Add each synchronizer's sorted channels.
    channelSynchronizers.stream()
        .sorted(
            Comparator.comparingInt(ChannelSynchronizer::getOrderingPriority)
                .thenComparing(ChannelSynchronizer::getChannelPrefix))
        .forEach(
            channelSynchronizer -> {
              // Perform Update
              List<TextChannel> updateResult =
                  channelSynchronizer.updateTextChannelOrdering(
                      getFilteredTextChannelsForSynchronizer(channelSynchronizer));

              // Store results
              if (updateResult != null) {
                sortedChannels.addAll(updateResult);
              }
            });

    // Find any un-sorted roles and place them at the beginning of the sorted roles list.
    sortedChannels.addAll(
        0,
        discordService.getGuild().getTextChannels().stream()
            .filter(channel -> !sortedChannels.contains(channel))
            .collect(Collectors.toList()));

    // Perform ordering.
    DiscordUtils.orderEntities(
        discordService.getGuild().modifyTextChannelPositions(), sortedChannels);
  }

  /**
   * Filters and returns the text channels that are requested by the given synchronizer.
   *
   * @param channelSynchronizer The synchronizer.
   * @return The filtered text channels.
   */
  private List<TextChannel> getFilteredTextChannelsForSynchronizer(
      ChannelSynchronizer channelSynchronizer) {
    return discordService.getGuild().getTextChannels().stream()
        // Ignore case when filtering.
        .filter(
            channel ->
                channel
                    .getName()
                    .toLowerCase()
                    .startsWith(channelSynchronizer.getChannelPrefix().toLowerCase()))
        .collect(Collectors.toList());
  }
}
