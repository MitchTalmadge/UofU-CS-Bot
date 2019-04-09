package com.mitchtalmadge.uofu_cs_bot.service.discord.features.club;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service allows Guild Members to join and leave clubs.
 */
@Service
public class ClubMembershipService {

    /**
     * Adds a Member to a Club, assigning roles as necessary.
     * If the Club does not exist or the Member is already in the Club, nothing will happen.
     *
     * @param member The Member joining.
     * @param club   The Club to join.
     */
    public void joinClub(Member member, Club club) {
        // Find Club Role from name.
        List<Role> clubRoles = member.getGuild().getRolesByName("club-" + club.getName(), true);
        if (clubRoles.size() == 0)
            return;

        // Add Role to Member. If they already have it, nothing will happen.
        member.getGuild().getController().addSingleRoleToMember(member, clubRoles.get(0)).queue();
    }

    /**
     * Removes a Member from a Club, un-assigning roles as necessary.
     * If the Club does not exist or the Member is not in the Club, nothing will happen.
     *
     * @param member The Member leaving.
     * @param club   The Club to leave.
     */
    public void leaveClub(Member member, Club club) {
        // Find Club Role from name.
        List<Role> clubRoles = member.getGuild().getRolesByName("club-" + club.getName(), true);
        if (clubRoles.size() == 0)
            return;

        // Remove Role from Member. If they don't have it, nothing will happen.
        member.getGuild().getController().removeSingleRoleFromMember(member, clubRoles.get(0)).queue();
    }

}
