package com.mitchtalmadge.uofu_cs_bot.service.cs.role;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CSRoleOrganizationService {

    private final LogService logService;
    private final DiscordService discordService;

    /**
     * Determines if organization should take place this tick.
     * Will be changed to true when requested.
     */
    private boolean organizationRequested = false;

    @Autowired
    public CSRoleOrganizationService(LogService logService,
                                     DiscordService discordService) {
        this.logService = logService;
        this.discordService = discordService;
    }

    /**
     * Requests that roles be organized on the next tick.
     */
    public void requestOrganization() {
        this.organizationRequested = true;
    }

    /**
     * Organizes roles and ensures that they have the proper permissions.
     * Afterwards, calls on the {@link CSRoleAssignmentService} to re-check assignments.
     * <p>
     * Will only organize when requested via the requestOrganization method.
     * <p>
     * Scheduled to run every minute, with a 15 second delay on startup.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    @Async
    protected void organize() {
        // Leave immediately if no organization has been requested.
        if (!organizationRequested) {
            return;
        }

        logService.logInfo(getClass(), "Organizing Roles...");

        // Update role settings.
        updateRoleSettings();

        // Order roles
        orderRoles();

        //TODO: Check assignments again.

        logService.logInfo(getClass(), "Organization of Roles Completed.");
        organizationRequested = false;
    }

    /**
     * Updates the settings of all class roles.
     */
    private void updateRoleSettings() {
        List<Role> roles = discordService.getGuild().getRoles();

        roles.forEach(role -> {
            try {
                CSClass roleClass = new CSClass(role.getName());
                CSSuffix roleSuffix = CSSuffix.fromClassName(role.getName());

                RoleManagerUpdatable roleManager = role.getManagerUpdatable();

                // Make sure name is correct.
                roleManager.getNameField().setValue(CSNamingConventions.toRoleName(roleClass, roleSuffix));

                // Make sure color is correct.
                roleManager.getColorField().setValue(roleSuffix.getRoleColor());

                // Make sure hoist is correct.
                roleManager.getHoistedField().setValue(roleSuffix.isRoleHoisted());

                // Make sure mentionable is correct.
                roleManager.getMentionableField().setValue(roleSuffix.isRoleMentionable());

                // Make sure permissions are correct.
                roleManager.getPermissionField().setPermissions(roleSuffix.getPermissions());

                roleManager.update().queue();
            } catch (CSClass.InvalidClassNameException ignored) {
                // This is not a class role.
            }
        });
    }

    /**
     * Ensures that roles are in the correct order.
     */
    private void orderRoles() {
        // Get all the roles.
        List<Role> roles = discordService.getGuild().getRoles();

        // Partition the roles into two, based on whether or not they are class roles.
        Map<Boolean, List<Role>> partitionedRoles = roles.stream().collect(Collectors.partitioningBy(role -> {
            // Attempt to parse the role as a CS Class. If successful, return true.
            try {
                new CSClass(role.getName());
                return true;
            } catch (CSClass.InvalidClassNameException ignored) {
                return false;
            }
        }));

        // Combine the roles back together with the class roles in order at the bottom.
        // Do not re-order the other roles. We do not care about their order.
        List<Role> orderedRoles = new ArrayList<>();
        // Add non class roles.
        orderedRoles.addAll(partitionedRoles.get(false));
        // Sort class roles before adding
        List<Role> classRoles = partitionedRoles.get(true);
        classRoles.sort(
                Comparator.comparing(obj -> CSSuffix.fromClassName(((Role) obj).getName())) // Order by suffix
                        .thenComparing(obj -> ((Role) obj).getName().toUpperCase()) // Order by name; ignore case by forcing all to uppercase.
                        .reversed()); // Reverse order so suffixes are at top of roles.
        // Add class roles
        orderedRoles.addAll(classRoles);

        // Remove @everyone role, as its order cannot be changed.
        orderedRoles.removeIf(Role::isPublicRole);

        // Perform ordering.
        DiscordUtils.orderEntities(discordService.getGuild().getController().modifyRolePositions(false), orderedRoles);
    }

}
