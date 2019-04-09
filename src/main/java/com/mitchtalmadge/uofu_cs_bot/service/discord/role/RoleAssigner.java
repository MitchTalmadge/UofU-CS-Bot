package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.Set;

@InheritedComponent
public abstract class RoleAssigner {

    /**
     * Assigns and/or un-assigns roles as necessary for a member.
     *
     * @param member        The Member.
     * @param rolesToAdd    The roles to add to the member. May already be populated from other filters.
     *                      Modify as necessary.
     * @param rolesToRemove The roles to remove from the member. May already be populated from other filters.
     *                      Modify as necessary.
     */
    public abstract void updateRoleAssignments(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove);

}
