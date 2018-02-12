package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class RoleSynchronizationService {

    private final LogService logService;
    private final DiscordService discordService;
    private CSRoleAssignmentService csRoleAssignmentService;
    private Set<RoleSynchronizer> roleSynchronizers;

    @Autowired
    public RoleSynchronizationService(LogService logService,
                                      DiscordService discordService,
                                      CSRoleAssignmentService csRoleAssignmentService,
                                      Set<RoleSynchronizer> roleSynchronizers) {
        this.logService = logService;
        this.discordService = discordService;
        this.csRoleAssignmentService = csRoleAssignmentService;
        this.roleSynchronizers = roleSynchronizers;
    }

    /**
     * Begins synchronization of Roles. <br/>
     * This may involve creating, deleting, modifying, or moving Roles as needed.
     */
    public void synchronize() {

        logService.logInfo(getClass(), "Beginning Synchronization as Requested.");

        // Creation and Deletion
        synchronizeRoles();

        // Settings
        updateRoleSettings();

        // Ordering
        updateRoleOrdering();

        // Assignment
        // TODO: Optimize this class into listener pattern similar to synchronizers.
        csRoleAssignmentService.updateAllMemberRoleAssignments();
    }

    /**
     * Creates and/or deletes Roles as necessary.
     */
    private void synchronizeRoles() {
        logService.logInfo(getClass(), "Synchronizing Roles...");

        roleSynchronizers.forEach(roleSynchronizer -> {
            // Perform synchronization
            Pair<Collection<Role>, Collection<RoleAction>> synchronizationResult = roleSynchronizer.synchronizeRoles(discordService.getGuild().getRoles());

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
            Collection<RoleManagerUpdatable> updateResult = roleSynchronizer.updateRoleSettings(discordService.getGuild().getRoles());

            // Queue any requested Updatable instances.
            if (updateResult != null) {
                updateResult.forEach(roleManagerUpdatable -> {
                    roleManagerUpdatable.update().complete();
                });
            }
        });
    }

    /**
     * Updates the order of Roles in the Guild.
     */
    private void updateRoleOrdering() {
        logService.logInfo(getClass(), "Updating Role Ordering...");

        roleSynchronizers.forEach(roleSynchronizer -> {
            // Perform Update
            List<Role> updateResult = roleSynchronizer.updateRoleOrdering(discordService.getGuild().getRoles());

            // Queue any requested Updatable instances.
            if (updateResult != null) {

                // Remove @everyone role since it cannot be sorted.
                updateResult.removeIf(Role::isPublicRole);

                // Perform ordering.
                DiscordUtils.orderEntities(discordService.getGuild().getController().modifyRolePositions(false), updateResult);
            }
        });
    }

}
