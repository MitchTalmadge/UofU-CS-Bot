package com.mitchtalmadge.uofu_cs_bot.service.discord.club;

import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleSynchronizer;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link RoleSynchronizer} for Clubs.
 */
public class ClubRoleSynchronizer extends RoleSynchronizer {

    @Override
    public Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> roles) {
        // TODO: Create club default and admin roles.
        return null;
    }

    @Override
    public Collection<RoleManagerUpdatable> updateRoleSettings(List<Role> roles) {
        // TODO: Update club default and admin role settings.
        return null;
    }

    @Override
    public List<Role> updateRoleOrdering(List<Role> roles) {
        // TODO: Order club default and admin roles.
        return null;
    }

}
