package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManager;
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

    /**
     * The prefix to filter out roles for each method in the synchronizer.
     * Only roles that begin with this prefix will be given as parameters to the methods.
     * Case Insensitive.
     */
    private final String rolePrefix;

    /**
     * Determines the order in which different synchronizers will order their roles relative
     * to each other. 0 means the roles for this synchronizer will be placed at the top.
     */
    private final int orderingPriority;

    @Autowired
    protected DiscordService discordService;

    /**
     * Constructs the Role Synchronizer.
     *
     * @param rolePrefix       The prefix to filter out roles for each method in the synchronizer.
     *                         Only roles that begin with this prefix will be given as parameters to the methods.
     *                         Case Insensitive.
     * @param orderingPriority Determines the order in which different synchronizers will order their roles relative
     *                         to each other. 0 means the roles for this synchronizer will be placed at the top.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RoleSynchronizer(String rolePrefix, int orderingPriority) {
        this.rolePrefix = rolePrefix;
        this.orderingPriority = orderingPriority;
    }

    /**
     * Creates and/or deletes Roles as necessary.
     *
     * @param filteredRoles A list of roles beginning with the rolePrefix given in the constructor.
     * @return A Pair containing
     * <ol>
     * <li>A Collection of Roles to delete.</li>
     * <li>A Collection of RoleActions which will be queued later. Used to create Roles.</li>
     * </ol>
     */
    public abstract Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> filteredRoles);

    /**
     * Ensures that all Roles have the correct settings.
     *
     * @param filteredRoles A list of roles beginning with the rolePrefix given in the constructor.
     * @return A Collection of {@link RoleManager} instances with updated settings, which will be queued later.
     */
    public abstract Collection<RoleManager> updateRoleSettings(List<Role> filteredRoles);

    /**
     * Updates the order of Roles in the Guild.
     *
     * @param filteredRoles A list of roles beginning with the rolePrefix given in the constructor.
     * @return A List of all Roles in the order they should appear in the Guild.
     */
    public abstract List<Role> updateRoleOrdering(List<Role> filteredRoles);

    public String getRolePrefix() {
        return rolePrefix;
    }

    public int getOrderingPriority() {
        return orderingPriority;
    }
}
