package com.mitchtalmadge.uofu_cs_bot.service.discord.channel;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Channel Synchronizers listen for and act upon Channel synchronization lifecycle events. Channels
 * must be added, removed, and organized in a specific order to prevent collisions.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@InheritedComponent
public abstract class ChannelSynchronizer {

  /**
   * The prefix to filter out channels for each method in the synchronizer. Only channels that begin
   * with this prefix will be given as parameters to the methods. Case Insensitive.
   */
  private final String channelPrefix;

  /**
   * Determines the order in which different synchronizers will order their channels relative to
   * each other. 0 means the channels for this synchronizer will be placed at the top.
   */
  private final int orderingPriority;

  @Autowired protected DiscordService discordService;

  /**
   * Constructs the Role Synchronizer.
   *
   * @param channelPrefix The prefix to filter out channels for each method in the synchronizer.
   *     Only channels that begin with this prefix will be given as parameters to the methods. Case
   *     Insensitive.
   * @param orderingPriority Determines the order in which different synchronizers will order their
   *     channels relative to each other. 0 means the channels for this synchronizer will be placed
   *     at the top.
   */
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public ChannelSynchronizer(String channelPrefix, int orderingPriority) {
    this.channelPrefix = channelPrefix;
    this.orderingPriority = orderingPriority;
  }

  /**
   * Creates and/or deletes Channel Categories as necessary.
   *
   * @param categories A List of all Channel Categories in the Guild.
   * @return A Pair containing
   *     <ol>
   *       <li>A Collection of Categories to delete.
   *       <li>A Collection of names for which to create Categories.
   *     </ol>
   */
  public abstract Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(
      List<Category> categories);

  /**
   * Creates and/or deletes Text Channels as necessary.
   *
   * @param filteredChannels A list of channels whose names start with the channelPrefix provided in
   *     the constructor.
   * @return A Pair containing
   *     <ol>
   *       <li>A Collection of Text Channels to delete.
   *       <li>A Collection of names for which to create Text Channels.
   *     </ol>
   */
  public abstract Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(
      List<TextChannel> filteredChannels);

  /**
   * Ensures that all Channel Categories have the correct settings.
   *
   * @param categories A List of all Channel Categories in the Guild.
   * @return A Collection of {@link ChannelManager} instances with updated settings, which will be
   *     queued later.
   */
  public abstract Collection<ChannelManager> updateChannelCategorySettings(
      List<Category> categories);

  /**
   * Ensures that all Text Channels have the correct settings.
   *
   * @param filteredChannels A list of channels whose names start with the channelPrefix provided in
   *     the constructor.
   * @return A Collection of {@link ChannelManager} instances with updated settings, which will be
   *     queued later.
   */
  public abstract Collection<ChannelManager> updateTextChannelSettings(
      List<TextChannel> filteredChannels);

  /**
   * Ensures that all Channel Categories have the correct permissions.
   *
   * @param categories A List of all Channel Categories in the Guild.
   * @return A Pair containing
   *     <ol>
   *       <li>Another Pair containing
   *           <ol>
   *             <li>A Collection of {@link PermissionOverride} to delete.
   *             <li>A Collection of {@link PermissionOverrideAction} to queue.
   *           </ol>
   *       <li>A Collection of {@link PermissionOverrideAction} to queue.
   *     </ol>
   */
  public abstract Pair<
          Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>,
          Collection<PermissionOverrideAction>>
      updateChannelCategoryPermissions(List<Category> categories);

  /**
   * Ensures that all Text Channels have the correct permissions.
   *
   * @param filteredChannels A list of channels whose names start with the channelPrefix provided in
   *     the constructor.
   * @return A Pair containing
   *     <ol>
   *       <li>Another Pair containing
   *           <ol>
   *             <li>A Collection of {@link PermissionOverride} to delete.
   *             <li>A Collection of {@link PermissionOverrideAction} to queue.
   *           </ol>
   *       <li>A Collection of {@link PermissionOverrideAction} to queue.
   *     </ol>
   */
  public abstract Pair<
          Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>,
          Collection<PermissionOverrideAction>>
      updateTextChannelPermissions(List<TextChannel> filteredChannels);

  /**
   * Updates the order of Channel Categories in the Guild.
   *
   * @param categories A List of all Channel Categories in the Guild in their current order.
   * @return A List of all Channel Categories in the order they should appear in the Guild.
   */
  public abstract List<Category> updateChannelCategoryOrdering(List<Category> categories);

  /**
   * Updates the order of Text Channels in the Guild.
   *
   * @param filteredChannels A list of channels whose names start with the channelPrefix provided in
   *     the constructor.
   * @return A List of all Text Channels in the order they should appear in the Guild.
   */
  public abstract List<TextChannel> updateTextChannelOrdering(List<TextChannel> filteredChannels);

  public String getChannelPrefix() {
    return channelPrefix;
  }

  public int getOrderingPriority() {
    return orderingPriority;
  }
}
