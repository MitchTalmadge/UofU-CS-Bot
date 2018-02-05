package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.course.CourseService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Creates or deletes the necessary roles on startup for the server based on the enabled CS Classes.
 * Does not organize roles; only creates / deletes them as needed.
 *
 * @author Mitch Talmadge
 */
@Service
public class CSRoleSyncService {

    private final LogService logService;
    private final DiscordService discordService;
    private final CourseService courseService;
    private final CSRoleOrganizationService csRoleOrganizationService;
    private final CSRoleAssignmentService csRoleAssignmentService;

    @Autowired
    public CSRoleSyncService(LogService logService,
                             DiscordService discordService,
                             CourseService courseService,
                             CSRoleOrganizationService csRoleOrganizationService,
                             CSRoleAssignmentService csRoleAssignmentService) {
        this.logService = logService;
        this.discordService = discordService;
        this.courseService = courseService;
        this.csRoleOrganizationService = csRoleOrganizationService;
        this.csRoleAssignmentService = csRoleAssignmentService;
    }

    @PostConstruct
    private void init() {
        // Creates and deletes roles as needed.
        SyncRoles();

        // Organize roles.
        csRoleOrganizationService.requestOrganization();

        // Assign roles.
        csRoleAssignmentService.updateAllMemberRoleAssignments();
    }

    /**
     * Finds and creates any missing roles based on the enabled courses.
     * Deletes any roles which are based on non-enabled courses.
     */
    private void SyncRoles() {
        Guild guild = discordService.getGuild();
        Set<Course> enabledClasses = courseService.getEnabledCourses();
        logService.logInfo(getClass(), "Synchronizing Roles...");

        // This map starts by containing all enabled courses mapped to all suffixes. Suffixes are removed one-by-one as their roles are found.
        // The remaining suffixes which have not been removed must be created as new roles for the mapped courses.
        Map<Course, Set<CSSuffix>> missingRoles = new HashMap<>();

        // Pre-fill the missingRoles map.
        for (Course enabledClass : enabledClasses) {
            Set<CSSuffix> missingSuffixes = new HashSet<>();
            missingSuffixes.addAll(Arrays.asList(CSSuffix.values()));
            missingRoles.put(enabledClass, missingSuffixes);
        }

        // Find the existing roles and delete invalid roles.
        for (Role role : guild.getRoles()) {
            try {
                // Parse the role as a class.
                Course roleClass = new Course(role.getName());
                CSSuffix roleSuffix = CSSuffix.fromClassName(role.getName());

                // Remove the class if it exists.
                if (missingRoles.containsKey(roleClass)) {
                    missingRoles.get(roleClass).remove(roleSuffix);
                } else {
                    // Delete the role as it should not exist.
                    logService.logInfo(getClass(), "Deleting invalid Role: " + role.getName());
                    role.delete().queue();
                }
            } catch (Course.InvalidCourseNameException ignored) {
                // This role is not a class role.
            }
        }

        // Add the missing roles.
        missingRoles.forEach((csClass, suffixes) -> suffixes.forEach(suffix -> {
            String roleName = CSNamingConventions.toRoleName(csClass, suffix);
            logService.logInfo(getClass(), "Creating new Role: " + roleName);

            guild.getController().createRole()
                    .setName(roleName)
                    .setColor(suffix.getRoleColor())
                    .setHoisted(suffix.isRoleHoisted())
                    .setMentionable(suffix.isRoleMentionable())
                    .setPermissions(suffix.getPermissions())
                    .queue();
        }));
    }

}