package com.mitchtalmadge.uofu_cs_bot.service.discord.features.club;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizer;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.PermOverrideManager;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
    public Collection<ChannelManager> updateChannelCategorySettings(List<Category> categories) {
        return null;
    }

    @Override
    public Collection<ChannelManager> updateTextChannelSettings(List<TextChannel> filteredChannels) {

        // Create Collection to be returned.
        Collection<ChannelManager> channelManagers = new HashSet<>();

        filteredChannels.forEach(textChannel -> {

            // Get club from channel.
            Club club = getClubFromChannel(textChannel);

            ChannelManager manager = textChannel.getManager()
                    .setName(getChannelNameFromClub(club, textChannel.getName().endsWith("admin")))
                    .setParent(getClubsCategory(textChannel.getGuild()))
                    .setNSFW(false);

            channelManagers.add(manager);
        });

        // Return managers to be queued.
        return channelManagers;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateChannelCategoryPermissions(List<Category> categories) {
        return null;
    }

    @Override
    public Pair<Pair<Collection<PermissionOverride>, Collection<PermissionOverrideAction>>, Collection<PermOverrideManager>> updateTextChannelPermissions(List<TextChannel> filteredChannels) {
        // Create Collections for returning.
        Collection<PermissionOverride> permissionOverrides = new HashSet<>();
        Collection<PermissionOverrideAction> permissionOverrideActions = new HashSet<>();
        Collection<PermOverrideManager> permOverrideManagers = new HashSet<>();

        filteredChannels.forEach(textChannel -> {

            // Get club
            Club club = getClubFromChannel(textChannel);

            // Admin Permissions
            if (textChannel.getName().endsWith("admin")) {
                // Delete any overrides that don't belong.
                textChannel.getPermissionOverrides().forEach(permissionOverride -> {
                    if (permissionOverride.getRole().isPublicRole()
                            || (permissionOverride.getRole().getName().startsWith("club-") && permissionOverride.getRole().getName().endsWith("admin")))
                        return;

                    permissionOverrides.add(permissionOverride);
                });

                // Recreate overrides that do belong.

                PermissionOverride override;

                // @everyone
                if ((override = textChannel.getPermissionOverride(textChannel.getGuild().getPublicRole())) == null) {
                    // Create new Permissions
                    PermissionOverrideAction overrideAction = textChannel.createPermissionOverride(textChannel.getGuild().getPublicRole());
                    overrideAction = overrideAction.setDeny(Permission.VIEW_CHANNEL);
                    permissionOverrideActions.add(overrideAction);
                } else {
                    // Update existing Permissions
                    permOverrideManagers.add(
                            override.getManager()
                                    .clear(Permission.ALL_PERMISSIONS)
                                    .deny(Permission.VIEW_CHANNEL));
                }

                // Admin role
                if ((override = textChannel.getPermissionOverride(textChannel.getGuild().getRolesByName("club-" + club.getName().toLowerCase() + "-admin", true).get(0))) == null) {
                    // Create new Permissions
                    PermissionOverrideAction overrideAction = textChannel.createPermissionOverride(textChannel.getGuild().getRolesByName("club-" + club.getName().toLowerCase() + "-admin", true).get(0));
                    overrideAction = overrideAction.setAllow(Permission.VIEW_CHANNEL);
                    permissionOverrideActions.add(overrideAction);
                } else {
                    // Update existing Permissions
                    permOverrideManagers.add(
                            override.getManager()
                                    .clear(Permission.ALL_PERMISSIONS)
                                    .grant(Permission.VIEW_CHANNEL));
                }
            } else { // Public permissions

                // Delete any overrides that don't belong.
                textChannel.getPermissionOverrides().forEach(permissionOverride -> {
                    if (permissionOverride.getRole().isPublicRole()
                            || (permissionOverride.getRole().getName().startsWith("club-") && !permissionOverride.getRole().getName().endsWith("admin")))
                        return;

                    permissionOverrides.add(permissionOverride);
                });

                // Recreate overrides that do belong.

                PermissionOverride override;

                // @everyone
                if ((override = textChannel.getPermissionOverride(textChannel.getGuild().getPublicRole())) == null) {
                    // Create new Permissions
                    PermissionOverrideAction overrideAction = textChannel.createPermissionOverride(textChannel.getGuild().getPublicRole());
                    overrideAction = overrideAction.setDeny(Permission.VIEW_CHANNEL);
                    permissionOverrideActions.add(overrideAction);
                } else {
                    // Update existing Permissions
                    permOverrideManagers.add(
                            override.getManager()
                                    .clear(Permission.ALL_PERMISSIONS)
                                    .deny(Permission.VIEW_CHANNEL));
                }

                // Public role
                if ((override = textChannel.getPermissionOverride(textChannel.getGuild().getRolesByName("club-" + club.getName().toLowerCase(), true).get(0))) == null) {
                    // Create new Permissions
                    PermissionOverrideAction overrideAction = textChannel.createPermissionOverride(textChannel.getGuild().getRolesByName("club-" + club.getName().toLowerCase(), true).get(0));
                    overrideAction = overrideAction.setAllow(Permission.VIEW_CHANNEL);
                    permissionOverrideActions.add(overrideAction);
                } else {
                    // Update existing Permissions
                    permOverrideManagers.add(
                            override.getManager()
                                    .clear(Permission.ALL_PERMISSIONS)
                                    .grant(Permission.VIEW_CHANNEL));
                }
            }
        });

        // Return Collections.
        return Pair.of(Pair.of(permissionOverrides, permissionOverrideActions), permOverrideManagers);
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
