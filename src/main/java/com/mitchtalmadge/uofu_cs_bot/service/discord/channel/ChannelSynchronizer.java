package com.mitchtalmadge.uofu_cs_bot.service.discord.channel;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Channel Synchronizers listen for and act upon channel synchronization lifecycle events.
 * Channels must be added, removed, and organized in a specific order to prevent collisions.
 */
@InheritedComponent
public abstract class ChannelSynchronizer {

    /**
     * Creates and/or deletes Channel Categories as necessary.
     *
     * @param categories A List of all Channel Categories in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Categories to delete.</li>
     * <li>A Collection of names for which to create Categories.</li>
     * </ol>
     */
    public abstract Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(List<Category> categories);

    /**
     * Creates and/or deletes Text Channels as necessary.
     *
     * @param textChannels A List of all Text Channels in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Text Channels to delete.</li>
     * <li>A Collection of names for which to create Text Channels.</li>
     * </ol>
     */
    public abstract Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(List<TextChannel> textChannels);

    /**
     * Creates and/or deletes Voice Channels as necessary.
     *
     * @param voiceChannels A List of all Voice Channels in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Voice Channels to delete.</li>
     * <li>A Collection of names for which to create Voice Channels.</li>
     * </ol>
     */
    public abstract Pair<Collection<VoiceChannel>, Collection<String>> synchronizeVoiceChannels(List<VoiceChannel> voiceChannels);

    /**
     * Ensures that all Channel Categories have the correct settings.
     *
     * @param categories A List of all Channel Categories in the Guild.
     * @return A Collection of ChannelManagerUpdatable instances with updated settings, which will be queued later.
     */
    public abstract Collection<ChannelManagerUpdatable> updateChannelCategorySettings(List<Category> categories);

    /**
     * Ensures that all Text Channels have the correct settings.
     *
     * @param textChannels A List of all Text Channels in the Guild.
     * @return A Collection of ChannelManagerUpdatable instances with updated settings, which will be queued later.
     */
    public abstract Collection<ChannelManagerUpdatable> updateTextChannelSettings(List<TextChannel> textChannels);

    /**
     * Ensures that all Voice Channels have the correct settings.
     *
     * @param voiceChannels A List of all Voice Channels in the Guild.
     * @return A Collection of ChannelManagerUpdatable instances with updated settings, which will be queued later.
     */
    public abstract Collection<ChannelManagerUpdatable> updateVoiceChannelSettings(List<VoiceChannel> voiceChannels);

    /**
     * Ensures that all Channel Categories have the correct permissions.
     *
     * @param categories A List of all Channel Categories in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>Another Pair containing
     * <ol>
     * <li>A Collection of PermissionOverrides to delete.</li>
     * <li>A Collection of PermissionOverrideActions to queue.</li>
     * </ol>
     * </li>
     * <li>A Collection of PermOverrideManagerUpdatables to queue.</li>
     * </ol>
     */
    public abstract Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateChannelCategoryPermissions(List<Category> categories);

    /**
     * Ensures that all Text Channels have the correct permissions.
     *
     * @param textChannels A List of all Text Channels in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>Another Pair containing
     * <ol>
     * <li>A Collection of PermissionOverrides to delete.</li>
     * <li>A Collection of PermissionOverrideActions to queue.</li>
     * </ol>
     * </li>
     * <li>A Collection of PermOverrideManagerUpdatables to queue.</li>
     * </ol>
     */
    public abstract Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateTextChannelPermissions(List<TextChannel> textChannels);

    /**
     * Ensures that all Voice Channels have the correct permissions.
     *
     * @param voiceChannels A List of all Voice Channels in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>Another Pair containing
     * <ol>
     * <li>A Collection of PermissionOverrides to delete.</li>
     * <li>A Collection of PermissionOverrideActions to queue.</li>
     * </ol>
     * </li>
     * <li>A Collection of PermOverrideManagerUpdatables to queue.</li>
     * </ol>
     */
    public abstract Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateVoiceChannelPermissions(List<VoiceChannel> voiceChannels);

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
     * @param textChannels A List of all Text Channels in the Guild in their current order.
     * @return A List of all Text Channels in the order they should appear in the Guild.
     */
    public abstract List<TextChannel> updateTextChannelOrdering(List<TextChannel> textChannels);

    /**
     * Updates the order of Voice Channels in the Guild.
     *
     * @param voiceChannels A List of all Voice Channels in the Guild in their current order.
     * @return A List of all Voice Channels in the order they should appear in the Guild.
     */
    public abstract List<VoiceChannel> updateVoiceChannelOrdering(List<VoiceChannel> voiceChannels);
}
