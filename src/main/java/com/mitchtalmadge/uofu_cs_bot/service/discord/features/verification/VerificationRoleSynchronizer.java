package com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification;

import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleSynchronizer;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of {@link RoleSynchronizer} for verification of users.
 */
public class VerificationRoleSynchronizer extends RoleSynchronizer {

    static final String VERIFIED_ROLE_NAME = "verified";

    @Autowired
    public VerificationRoleSynchronizer() {
        super(VERIFIED_ROLE_NAME, 0);
    }

    @Override
    public Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> filteredRoles) {

        // Create Collections for returning.
        Collection<Role> rolesToRemove = new HashSet<>();
        Collection<RoleAction> rolesToCreate = new HashSet<>();

        // Search for verified role, and delete any that shouldn't be here.
        for (Role role : filteredRoles) {
            if (!role.getName().equals(VERIFIED_ROLE_NAME)) {
                rolesToRemove.add(role);
            }
        }

        // Create "verified" role if it is missing.
        if (rolesToRemove.size() == filteredRoles.size()) {
            RoleAction roleAction = discordService.getGuild().getController().createRole()
                    .setName(VERIFIED_ROLE_NAME)
                    .setColor(Color.decode("0x3498DB"))
                    .setHoisted(true)
                    .setMentionable(false)
                    .setPermissions(
                            Permission.NICKNAME_CHANGE,
                            Permission.MESSAGE_WRITE,
                            Permission.MESSAGE_EMBED_LINKS,
                            Permission.MESSAGE_ATTACH_FILES,
                            Permission.MESSAGE_HISTORY,
                            Permission.MESSAGE_ADD_REACTION,
                            Permission.VOICE_SPEAK,
                            Permission.VOICE_USE_VAD
                    );

            rolesToCreate.add(roleAction);
        }


        // Return collections.
        return Pair.of(rolesToRemove, rolesToCreate);
    }

    @Override
    public Collection<RoleManagerUpdatable> updateRoleSettings(List<Role> filteredRoles) {

        // Create collection to return.
        Collection<RoleManagerUpdatable> updatables = new HashSet<>();

        filteredRoles.forEach(role -> {
            RoleManagerUpdatable updatable = role.getManagerUpdatable();

            // Make sure color is correct.
            updatable.getColorField().setValue(Color.decode("0x3498DB"));

            // Make sure hoist is correct.
            updatable.getHoistedField().setValue(true);

            // Make sure mentionable is correct.
            updatable.getMentionableField().setValue(false);

            // Make sure permissions are correct.
            updatable.getPermissionField().setPermissions(
                    Permission.NICKNAME_CHANGE,
                    Permission.MESSAGE_WRITE,
                    Permission.MESSAGE_EMBED_LINKS,
                    Permission.MESSAGE_ATTACH_FILES,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION,
                    Permission.VOICE_SPEAK,
                    Permission.VOICE_USE_VAD
            );

            // Add for queue later.
            updatables.add(updatable);
        });

        return updatables;
    }

    @Override
    public List<Role> updateRoleOrdering(List<Role> filteredRoles) {
        // There's only one role. No sorting needed.
        return filteredRoles;
    }

}
