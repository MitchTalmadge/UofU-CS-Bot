package com.mitchtalmadge.uofu_cs_bot.service.discord.club;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleSynchronizer;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RoleSynchronizer} for Clubs.
 */
public class ClubRoleSynchronizer extends RoleSynchronizer {

    /**
     * Permissions for the public club roles.
     */
    private static final Permission[] PUBLIC_ROLE_PERMISSIONS = {
            Permission.NICKNAME_CHANGE,
            Permission.MESSAGE_READ,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_ADD_REACTION,
            Permission.VOICE_CONNECT,
            Permission.VOICE_SPEAK,
            Permission.VOICE_USE_VAD
    };

    /**
     * Permissions for the admin club roles.
     */
    private static final Permission[] ADMIN_ROLE_PERMISSIONS = {
            Permission.NICKNAME_CHANGE,
            Permission.MESSAGE_READ,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_ADD_REACTION,
            Permission.VOICE_CONNECT,
            Permission.VOICE_SPEAK,
            Permission.VOICE_USE_VAD
    };

    private ClubService clubService;

    @Autowired
    ClubRoleSynchronizer(ClubService clubService) {
        this.clubService = clubService;
    }

    @Override
    public Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> roles) {

        // Create Collections for returning.
        Collection<Role> rolesToRemove = new HashSet<>();
        Collection<RoleAction> rolesToCreate = new HashSet<>();

        // Get all enabled courses.
        Set<Club> enabledClubs = clubService.getEnabledClubs();

        // Determine which roles need to be created.
        enabledClubs.forEach(club -> {

            // Public club role
            if (discordService.getGuild().getTextChannelsByName(getRoleNameFromClub(club, false), false).size() == 0)
                rolesToCreate.add(
                        discordService
                                .getGuild()
                                .getController()
                                .createRole()
                                .setName(getRoleNameFromClub(club, false))
                                .setHoisted(false)
                                .setColor(Color.WHITE)
                                .setMentionable(false)
                                .setPermissions(PUBLIC_ROLE_PERMISSIONS)
                );

            // Admin club role
            if (discordService.getGuild().getTextChannelsByName(getRoleNameFromClub(club, true), false).size() == 0)
                rolesToCreate.add(
                        discordService
                                .getGuild()
                                .getController()
                                .createRole()
                                .setName(getRoleNameFromClub(club, true))
                                .setHoisted(false)
                                .setColor(Color.WHITE)
                                .setMentionable(false)
                                .setPermissions(ADMIN_ROLE_PERMISSIONS)
                );
        });

        // Determine which roles need to be deleted.
        roles.forEach(role -> {
            // Ensure role is a club role.
            if (!role.getName().startsWith("club-"))
                return;

            // Get club from role.
            Club club = getClubFromRole(role);

            // Delete role if the club is not enabled.
            if (club == null)
                rolesToRemove.add(role);
        });

        // Return collections.
        return Pair.of(rolesToRemove, rolesToCreate);
    }

    @Override
    public Collection<RoleManagerUpdatable> updateRoleSettings(List<Role> roles) {
        // TODO: Update club default and admin role settings.
        return null;
    }

    @Override
    public List<Role> updateRoleOrdering(List<Role> roles) {
        // Partition the roles into two, based on whether or not they are club roles.
        Map<Boolean, List<Role>> partitionedRoles = roles.stream().collect(
                Collectors.partitioningBy(role -> role.getName().startsWith("club-")));

        // Combine the roles back together with the club roles in order at the bottom.
        // Do not re-order the other roles. We are not concerned with their order.

        // Add non-club roles.
        List<Role> orderedRoles = new ArrayList<>(partitionedRoles.get(false));

        // Sort club Roles before adding.
        List<Role> clubRoles = partitionedRoles.get(true);
        clubRoles.sort(Comparator.comparing(Role::getName)); // Order by name.

        // Add club Roles
        orderedRoles.addAll(clubRoles);

        // Return ordered Roles.
        return orderedRoles;
    }

    /**
     * From the given role, returns the associated club (determined by role name).
     *
     * @param role The role.
     * @return The club associated with the role, or null if one could not be associated.
     */
    private Club getClubFromRole(Role role) {
        // Iterate over all clubs and compare names.
        return clubService.getEnabledClubs()
                .stream()
                .filter(club -> role.getName().toLowerCase().startsWith("club-" + club.getName().toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    /**
     * From a club, determines the name of the role related to that club.
     *
     * @param club      The club.
     * @param adminRole Whether the channel is for admins or not.
     * @return The name of the role for the club.
     */
    private String getRoleNameFromClub(Club club, boolean adminRole) {
        return "club-" + club.getName().toLowerCase() + (adminRole ? "-admin" : "");
    }

}
