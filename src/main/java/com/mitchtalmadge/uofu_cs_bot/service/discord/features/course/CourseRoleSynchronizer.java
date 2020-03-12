package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleSynchronizer;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/** Implementation of {@link RoleSynchronizer} for Courses. */
public class CourseRoleSynchronizer extends RoleSynchronizer {

  private CourseService courseService;

  @Autowired
  public CourseRoleSynchronizer(CourseService courseService) {
    super("cs-", 0);
    this.courseService = courseService;
  }

  @Override
  public Pair<Collection<Role>, Collection<RoleAction>> synchronizeRoles(List<Role> filteredRoles) {

    // Create Collections for returning.
    Collection<Role> rolesToRemove = new HashSet<>();
    Collection<RoleAction> rolesToCreate = new HashSet<>();

    // Get all enabled courses.
    Set<Course> enabledCourses = courseService.getEnabledCourses();

    // This map starts by containing all enabled courses mapped to all suffixes. Suffixes are
    // removed one-by-one as their roles are found.
    // The remaining suffixes which have not been removed must be created as new roles for the
    // mapped courses.
    Map<Course, Set<CSSuffix>> missingRoles = new HashMap<>();

    // Pre-fill the missingRoles map.
    for (Course course : enabledCourses) {
      Set<CSSuffix> missingSuffixes = new HashSet<>(Arrays.asList(CSSuffix.values()));
      missingRoles.put(course, missingSuffixes);
    }

    // Find the existing roles and delete invalid roles.
    for (Role role : filteredRoles) {
      try {
        // Parse the role as a course.
        Course course = new Course(role.getName());
        CSSuffix roleSuffix = CSSuffix.fromCourseName(role.getName());

        // Remove the course if it exists.
        if (missingRoles.containsKey(course)) {
          missingRoles.get(course).remove(roleSuffix);
        } else {
          // Delete the role as it should not exist.
          rolesToRemove.add(role);
        }
      } catch (Course.InvalidCourseNameException ignored) {
        // This role is not a course role.
      }
    }

    // Create missing roles.
    missingRoles.forEach(
        (course, suffixes) ->
            suffixes.forEach(
                suffix -> {
                  String roleName = CSNamingConventions.toRoleName(course, suffix);

                  RoleAction roleAction =
                      discordService
                          .getGuild()
                          .createRole()
                          .setName(roleName)
                          .setColor(suffix.getRoleColor())
                          .setHoisted(suffix.isRoleHoisted())
                          .setMentionable(suffix.isRoleMentionable())
                          .setPermissions(suffix.getPermissions());

                  rolesToCreate.add(roleAction);
                }));

    // Return collections.
    return Pair.of(rolesToRemove, rolesToCreate);
  }

  @Override
  public Collection<RoleManager> updateRoleSettings(List<Role> filteredRoles) {

    // Create collection to return.
    Collection<RoleManager> roleManagers = new HashSet<>();

    filteredRoles.forEach(
        role -> {
          try {
            Course course = new Course(role.getName());
            CSSuffix roleSuffix = CSSuffix.fromCourseName(role.getName());

            RoleManager manager =
                role.getManager()
                    .setName(CSNamingConventions.toRoleName(course, roleSuffix))
                    .setColor(roleSuffix.getRoleColor())
                    .setHoisted(roleSuffix.isRoleHoisted())
                    .setMentionable(roleSuffix.isRoleMentionable())
                    .setPermissions(roleSuffix.getPermissions());

            // Add for queue later.
            roleManagers.add(manager);
          } catch (Course.InvalidCourseNameException ignored) {
            // This is not a Course Role.
          }
        });

    return roleManagers;
  }

  @Override
  public List<Role> updateRoleOrdering(List<Role> filteredRoles) {
    // Sort filtered roles by suffix, then by name.
    filteredRoles.sort(
        Comparator.comparing(
                obj -> CSSuffix.fromCourseName(((Role) obj).getName())) // Order by suffix
            .thenComparing(
                obj ->
                    ((Role) obj)
                        .getName()
                        .toUpperCase()) // Order by name; ignore case by forcing all to uppercase.
            .reversed()); // Reverse order so suffixes are at top of roles.

    return filteredRoles;
  }
}
