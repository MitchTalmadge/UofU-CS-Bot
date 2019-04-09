package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class RoleAssigner {

    /**
     * Assigns and/or un-assigns roles as necessary for a member.
     *
     * @param member The Member.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Roles to remove.</li>
     * <li>A Collection of Roles to add.</li>
     * </ol>
     */
    public abstract Pair<Set<Role>, Set<Role>> updateRoleAssignments(Member member);

}
