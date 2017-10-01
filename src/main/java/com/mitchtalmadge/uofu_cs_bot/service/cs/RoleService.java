package com.mitchtalmadge.uofu_cs_bot.service.cs;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Collection;
import java.util.List;

@Service
public class RoleService {

    /**
     * Gets the "@everyone" role of the guild.
     *
     * @param guild The guild.
     * @return The "@everyone" role, aka. the public role.
     */
    public Role getEveryoneRole(Guild guild) {
        return guild.getPublicRole();
    }

    /**
     * Gets all roles for the guild.
     *
     * @param guild The guild.
     * @return All roles in the guild.
     */
    public List<Role> getAllRoles(Guild guild) {
        return guild.getRoles();
    }

    /**
     * Searches the guild for a role with the given name.
     *
     * @param guild The guild to search.
     * @param name  The name of the role.
     * @return The role if found, null if not.
     */
    public Role getRoleByName(Guild guild, String name) {
        try {
            return guild.getRolesByName(name, true).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Creates a new role on the guild.
     *
     * @param guild             The guild.
     * @param name              The name of the new role.
     * @param color             The color of the new role.
     * @param displaySeparately Whether to display the new role separately in the sidebar.
     * @param mentionable       Whether the role can be mentioned.
     * @param permissions       Any permissions for the new role.
     */
    public void createRole(Guild guild, String name, Color color, boolean displaySeparately, boolean mentionable, Collection<Permission> permissions) {
        // Settings
        RoleAction roleAction = guild.getController().createRole().setName(name);
        if (color != null)
            roleAction = roleAction.setColor(color);
        roleAction = roleAction.setHoisted(displaySeparately);
        roleAction = roleAction.setMentionable(mentionable);
        if (permissions != null)
            roleAction = roleAction.setPermissions(permissions);

        // Create
        roleAction.queue();
    }

    /**
     * Deletes a given role.
     *
     * @param role The role to delete.
     */
    public void deleteRole(Role role) {
        role.delete().queue();
    }

}
