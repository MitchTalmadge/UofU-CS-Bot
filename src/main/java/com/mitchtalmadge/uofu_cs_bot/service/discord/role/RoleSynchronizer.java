package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Role Synchronizers listen for and act upon Role synchronization lifecycle events.
 * Roles must be added, removed, and organized in a specific order to prevent collisions.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@InheritedComponent
public abstract class RoleSynchronizer {

    @Autowired
    protected DiscordService discordService;

    /**
     * Creates and/or deletes Roles as necessary.
     *
     * @param roles A List of all Roles in the Guild.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Roles to delete.</li>
     * <li>A Collection of RoleActions which will be queued later. Used to create Roles.</li>
     * </ol>
     */
    public abstract Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> roles);

    /**
     * Ensures that all Roles have the correct settings.
     *
     * @param roles A List of all Roles in the Guild.
     * @return A Collection of {@link RoleManagerUpdatable} instances with updated settings, which will be queued later.
     */
    public abstract Collection<RoleManagerUpdatable> updateRoleSettings(List<Role> roles);

    /**
     * Updates the order of Roles in the Guild.
     *
     * @param roles A List of all Roles in the Guild in their current order.
     * @return A List of all Roles in the order they should appear in the Guild.
     */
    public abstract List<Role> updateRoleOrdering(List<Role> roles);

}
