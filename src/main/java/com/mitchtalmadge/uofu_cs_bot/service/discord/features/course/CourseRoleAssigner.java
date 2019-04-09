package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssigner;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Assigns course roles to members where needed.
 */
public class CourseRoleAssigner extends RoleAssigner {

    private final DiscordService discordService;
    private final CourseService courseService;

    @Autowired
    public CourseRoleAssigner(
            DiscordService discordService,
            CourseService courseService) {
        this.discordService = discordService;
        this.courseService = courseService;
    }

    @Override
    public void updateRoleAssignments(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) {
        // Get nickname of member.
        CSNickname csNickname = new CSNickname(member.getNickname());

        // This map will initially contain all expected combinations of Course instances and CSSuffix instances that the
        // member should be assigned to. Once found, the suffixes will be removed one-by-one. The remaining suffixes determine
        // which roles are missing from the member.
        Map<Course, Set<CSSuffix>> missingRolesMap = new HashMap<>();
        // Populate the missing roles map.
        csNickname.getClasses().forEach(csClass -> {
            // Don't allow courses which are not enabled.
            if (!courseService.getEnabledCourses().contains(csClass))
                return;

            // The suffixes that a member will be added to always includes NONE, as well as any specific suffix they may have.
            Set<CSSuffix> allowedSuffixes = new HashSet<>();
            allowedSuffixes.add(CSSuffix.NONE);
            allowedSuffixes.add(csNickname.getSuffixForClass(csClass));
            missingRolesMap.put(csClass, allowedSuffixes);
        });

        // Check each role of the member.
        member.getRoles().forEach(role -> {
            try {
                Course roleClass = new Course(role.getName());
                CSSuffix roleSuffix = CSSuffix.fromCourseName(role.getName());

                // Check that this class role is allowed.
                if (!csNickname.getClasses().contains(roleClass)) {
                    rolesToRemove.add(role);
                    return;
                }

                // Check that the current role's suffix is either the default or matches the nickname's role suffix.
                CSSuffix nicknameRoleSuffix = csNickname.getSuffixForClass(roleClass);
                if (nicknameRoleSuffix != roleSuffix && roleSuffix != CSSuffix.NONE) {
                    // This role suffix is not allowed for the current class.
                    rolesToRemove.add(role);
                    return;
                }

                // Remove this role from the missing map as it is present.
                missingRolesMap.get(roleClass).remove(roleSuffix);
            } catch (Course.InvalidCourseNameException ignored) {
                // Not a class role.
            }
        });

        // Determine the roles to be added to the member.
        missingRolesMap.forEach((csClass, suffixes) -> {
            suffixes.forEach(suffix -> {
                rolesToAdd.add(discordService.getGuild().getRolesByName(CSNamingConventions.toRoleName(csClass, suffix), false).get(0));
            });
        });
    }

}
