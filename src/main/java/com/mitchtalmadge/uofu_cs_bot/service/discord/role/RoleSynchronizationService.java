package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManager;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleSynchronizationService {

    private final LogService logService;
    private final DiscordService discordService;
    private Set<RoleSynchronizer> roleSynchronizers;

    @Autowired
    public RoleSynchronizationService(LogService logService,
                                      DiscordService discordService,
                                      Set<RoleSynchronizer> roleSynchronizers) {
        this.logService = logService;
        this.discordService = discordService;
        this.roleSynchronizers = roleSynchronizers;
    }

    /**
     * Begins synchronization of Roles. <br/>
     * This may involve creating, deleting, modifying, or moving Roles as needed.
     */
    public void synchronize() {

        // Creation and Deletion
        synchronizeRoles();

        // Settings
        updateRoleSettings();

        // Ordering
        updateRoleOrdering();

    }

    /**
     * Creates and/or deletes Roles as necessary.
     */
    private void synchronizeRoles() {
        logService.logInfo(getClass(), "Creating and Deleting Roles...");

        roleSynchronizers.forEach(roleSynchronizer -> {
            // Perform synchronization
            Pair<Collection<Role>, Collection<RoleAction>> synchronizationResult = roleSynchronizer.synchronizeRoles(getFilteredRolesForSynchronizer(roleSynchronizer));

            if (synchronizationResult != null) {

                // Delete any requested Roles.
                if (synchronizationResult.getLeft() != null) {
                    synchronizationResult.getLeft().forEach(role -> {
                        logService.logInfo(getClass(), "--> Deleting Role: " + role.getName());
                        role.delete().complete();
                    });
                }

                // Create any requested Categories.
                if (synchronizationResult.getRight() != null) {
                    synchronizationResult.getRight().forEach(roleAction -> {

                        // Use Reflection to get Role name.
                        try {
                            Field nameField = RoleAction.class.getDeclaredField("name");
                            nameField.setAccessible(true);

                            String name = (String) nameField.get(roleAction);
                            logService.logInfo(getClass(), "--> Creating Role: " + name);
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            logService.logInfo(getClass(), "--> Creating Role (Name Unknown)");
                        }

                        roleAction.complete();
                    });
                }

            }
        });
    }

    /**
     * Ensures that all Roles have the correct settings.
     */
    private void updateRoleSettings() {
        logService.logInfo(getClass(), "Updating Role Settings...");

        roleSynchronizers.forEach(roleSynchronizer -> {
            // Perform Update
            Collection<RoleManager> updateResult = roleSynchronizer.updateRoleSettings(getFilteredRolesForSynchronizer(roleSynchronizer));

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(RestAction::complete);
            }
        });
    }

    /**
     * Updates the order of Roles in the Guild.
     */
    private void updateRoleOrdering() {
        logService.logInfo(getClass(), "Updating Role Ordering...");

        // Will contain all the roles in their sorted order.
        List<Role> sortedRoles = new ArrayList<>();

        // Add each synchronizer's sorted roles.
        roleSynchronizers
                .stream()
                .sorted(Comparator.comparingInt(RoleSynchronizer::getOrderingPriority).thenComparing(RoleSynchronizer::getRolePrefix))
                .forEach(roleSynchronizer -> {
                    // Perform Update
                    List<Role> updateResult = roleSynchronizer.updateRoleOrdering(getFilteredRolesForSynchronizer(roleSynchronizer));

                    // Store results
                    if (updateResult != null) {
                        sortedRoles.addAll(updateResult);
                    }
                });

        // Find any un-sorted roles and place them at the beginning of the sorted roles list.
        sortedRoles.addAll(0,
                discordService.getGuild().getRoles()
                        .stream()
                        .filter(role -> !sortedRoles.contains(role))
                        .collect(Collectors.toList()));


        // Remove @everyone role since it cannot be sorted.
        sortedRoles.removeIf(Role::isPublicRole);

        // Perform ordering.
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyRolePositions(false), sortedRoles);
    }

    /**
     * Filters and returns the roles that are requested by the given synchronizer.
     *
     * @param roleSynchronizer The synchronizer.
     * @return The filtered roles.
     */
    private List<Role> getFilteredRolesForSynchronizer(RoleSynchronizer roleSynchronizer) {
        return discordService.getGuild().getRoles().stream()
                // Ignore case when filtering.
                .filter(role -> role.getName().toLowerCase().startsWith(roleSynchronizer.getRolePrefix().toLowerCase()))
                .collect(Collectors.toList());
    }

}
