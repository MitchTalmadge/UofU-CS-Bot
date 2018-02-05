package com.mitchtalmadge.uofu_cs_bot.service.discord.club;

import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizer;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of {@link ChannelSynchronizer} for Clubs.
 */
public class ClubChannelSynchronizer extends ChannelSynchronizer {

    private static final String CLUB_CATEGORY_NAME = "Clubs";

    private ClubService clubService;

    @Autowired
    ClubChannelSynchronizer(ClubService clubService) {
        this.clubService = clubService;
    }

    /**
     * @return The category that all Club Channels belong to. May be null if the Category does not yet exist.
     */
    private Category getClubsCategory(Guild guild) {
        List<Category> categories = guild.getCategoriesByName(CLUB_CATEGORY_NAME, false);
        if (categories.size() > 0)
            return categories.get(0);

        return null;
    }

    @Override
    public Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(List<Category> categories) {
        // TODO: Create Clubs Voice Category

        // Create Collections for returning.
        Collection<Category> categoriesToRemove = new HashSet<>();
        Collection<String> categoriesToCreate = new HashSet<>();

        // Check if the Clubs Category does not exist.
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(CLUB_CATEGORY_NAME))) {
            // Create the Clubs Category.
            categoriesToCreate.add(CLUB_CATEGORY_NAME);
        }

        // Return Collections.
        return Pair.of(categoriesToRemove, categoriesToCreate);
    }

    @Override
    public Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(List<TextChannel> textChannels) {
        // TODO: Create club and club admin channels.

        return null;
    }

    @Override
    public Pair<Collection<VoiceChannel>, Collection<String>> synchronizeVoiceChannels(List<VoiceChannel> voiceChannels) {
        // TODO: Create club and club admin channels.

        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateChannelCategorySettings(List<Category> categories) {
        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateTextChannelSettings(List<TextChannel> textChannels) {
        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateVoiceChannelSettings(List<VoiceChannel> voiceChannels) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateChannelCategoryPermissions(List<Category> categories) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateTextChannelPermissions(List<TextChannel> textChannels) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateVoiceChannelPermissions(List<VoiceChannel> voiceChannels) {
        return null;
    }

    @Override
    public List<Category> updateChannelCategoryOrdering(List<Category> categories) {
        return null;
    }

    @Override
    public List<TextChannel> updateTextChannelOrdering(List<TextChannel> textChannels) {
        return null;
    }

    @Override
    public List<VoiceChannel> updateVoiceChannelOrdering(List<VoiceChannel> voiceChannels) {
        return null;
    }

}
