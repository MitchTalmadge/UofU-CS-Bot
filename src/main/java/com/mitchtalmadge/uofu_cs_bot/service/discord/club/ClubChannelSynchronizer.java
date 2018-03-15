package com.mitchtalmadge.uofu_cs_bot.service.discord.club;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizer;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ChannelSynchronizer} for Clubs.
 */
public class ClubChannelSynchronizer extends ChannelSynchronizer {

    private static final String CLUB_CATEGORY_NAME = "Clubs";

    private ClubService clubService;

    @Autowired
    ClubChannelSynchronizer(ClubService clubService) {
        super("club-", 1);
        this.clubService = clubService;
    }

    @Override
    public Pair<Collection<Category>, Collection<String>> synchronizeChannelCategories(List<Category> categories) {
        // Create Collections for returning.
        Collection<Category> categoriesToRemove = new HashSet<>();
        Collection<String> categoriesToCreate = new HashSet<>();

        // Check if the Clubs Category does not exist.
        if (categories.stream().noneMatch(category -> category.getName().equalsIgnoreCase(CLUB_CATEGORY_NAME))) {
            // Create the Clubs Category.
            categoriesToCreate.add(CLUB_CATEGORY_NAME);
        }

        // TODO: Create Clubs Voice Category

        // Return Collections.
        return Pair.of(categoriesToRemove, categoriesToCreate);
    }

    @Override
    public Pair<Collection<TextChannel>, Collection<String>> synchronizeTextChannels(List<TextChannel> filteredChannels) {
        // Create collections for returning.
        Collection<TextChannel> channelsToRemove = new HashSet<>();
        Collection<String> channelsToCreate = new HashSet<>();

        // Get all enabled Clubs.
        Set<Club> enabledClubs = clubService.getEnabledClubs();

        // For each club, determine what channels must be created.
        enabledClubs.forEach(club -> {

            // Find public club channel
            if (discordService.getGuild().getTextChannelsByName(getChannelNameFromClub(club, false), false).size() == 0)
                channelsToCreate.add(getChannelNameFromClub(club, false));

            // Find admin club channel
            if (discordService.getGuild().getTextChannelsByName(getChannelNameFromClub(club, true), false).size() == 0)
                channelsToCreate.add(getChannelNameFromClub(club, true));
        });

        // Make sure each the club associated with any given channel is enabled.
        // If not, delete the channel.
        filteredChannels.forEach(channel -> {
            // Ensure channel is a club channel.
            if (!channel.getName().startsWith("club-"))
                return;

            // Get club from channel.
            Club club = getClubFromChannel(channel);

            // Delete channel if the club is not enabled.
            if (club == null)
                channelsToRemove.add(channel);
        });

        // Return collections.
        return Pair.of(channelsToRemove, channelsToCreate);
    }

    @Override
    public Pair<Collection<VoiceChannel>, Collection<String>> synchronizeVoiceChannels(List<VoiceChannel> filteredChannels) {
        // TODO: Create club and club admin channels.

        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateChannelCategorySettings(List<Category> categories) {
        return null;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateTextChannelSettings(List<TextChannel> filteredChannels) {

        // Create Collection to be returned.
        Collection<ChannelManagerUpdatable> channelManagerUpdatables = new HashSet<>();

        filteredChannels.forEach(textChannel -> {

            // Ensure channel is a club channel.
            if (!textChannel.getName().startsWith("club-"))
                return;

            // Get club from channel.
            Club club = getClubFromChannel(textChannel);

            ChannelManagerUpdatable updater = textChannel.getManagerUpdatable();

            // Name
            updater = updater.getNameField().setValue(getChannelNameFromClub(club, textChannel.getName().endsWith("admin")));

            // Category
            updater = updater.getParentField().setValue(getClubsCategory(textChannel.getGuild()));

            // NSFW Off
            updater = updater.getNSFWField().setValue(false);

            channelManagerUpdatables.add(updater);
        });

        // Return updatables to be queued.
        return channelManagerUpdatables;
    }

    @Override
    public Collection<ChannelManagerUpdatable> updateVoiceChannelSettings(List<VoiceChannel> filteredChannels) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateChannelCategoryPermissions(List<Category> categories) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateTextChannelPermissions(List<TextChannel> filteredChannels) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManagerUpdatable>> updateVoiceChannelPermissions(List<VoiceChannel> filteredChannels) {
        return null;
    }

    @Override
    public List<Category> updateChannelCategoryOrdering(List<Category> categories) {
        return null;
    }

    @Override
    public List<TextChannel> updateTextChannelOrdering(List<TextChannel> filteredChannels) {
        // Sort filtered channels by name.
        filteredChannels.sort(Comparator.comparing(Channel::getName));

        return filteredChannels;
    }

    @Override
    public List<VoiceChannel> updateVoiceChannelOrdering(List<VoiceChannel> filteredChannels) {
        return null;
    }

    /**
     * From the given channel, returns the associated club (determined by channel name).
     *
     * @param channel The channel.
     * @return The club associated with the channel, or null if one could not be associated.
     */
    private Club getClubFromChannel(Channel channel) {
        // Iterate over all clubs and compare names.
        return clubService.getEnabledClubs()
                .stream()
                .filter(club -> channel.getName().toLowerCase().startsWith("club-" + club.getName().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /**
     * From a club, determines the name of the channel related to that club.
     *
     * @param club         The club.
     * @param adminChannel Whether the channel is for admins or not.
     * @return The name of the channel for the club.
     */
    private String getChannelNameFromClub(Club club, boolean adminChannel) {
        return "club-" + club.getName().toLowerCase() + (adminChannel ? "-admin" : "");
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

}
