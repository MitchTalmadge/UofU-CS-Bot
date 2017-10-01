package com.mitchtalmadge.uofu_cs_bot.service.cs.role;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.CSClassService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Assigns roles to members where needed.
 */
@Service
public class CSRoleAssignmentService {

    private final LogService logService;
    private final DiscordService discordService;
    private final CSClassService csClassService;

    @Autowired
    public CSRoleAssignmentService(LogService logService,
                                   DiscordService discordService,
                                   CSClassService csClassService) {
        this.logService = logService;
        this.discordService = discordService;
        this.csClassService = csClassService;
    }

    /**
     * Computes the CS roles that each member in the guild belongs to.
     */
    public void updateAllMemberRoleAssignments() {
        // Update each member of the guild.
        for (Member member : discordService.getGuild().getMembers())
            updateRoleAssignments(member);
    }

    /**
     * Computes the CS roles that the member belongs to.
     *
     * @param member The member whose roles should be updated.
     */
    public void updateRoleAssignments(Member member) {
        logService.logInfo(getClass(), "Updating CS roles for " + member.getUser().getName());

        // Get nickname of member.
        CSNickname csNickname = new CSNickname(member.getNickname());

        // This map will initially contain all expected combinations of CSClass instances and CSSuffix instances that the
        // member should be assigned to. Once found, the suffixes will be removed one-by-one. The remaining suffixes determine
        // which roles are missing from the member.
        Map<CSClass, Set<CSSuffix>> missingRolesMap = new HashMap<>();
        // Populate the missing roles map.
        csNickname.getClasses().forEach(csClass -> {
            // Don't allow classes which are not enabled.
            if (!csClassService.getEnabledClasses().contains(csClass))
                return;

            // The suffixes that a member will be added to always includes NONE, as well as any specific suffix they may have.
            Set<CSSuffix> allowedSuffixes = new HashSet<>();
            allowedSuffixes.add(CSSuffix.NONE);
            allowedSuffixes.add(csNickname.getSuffixForClass(csClass));
            missingRolesMap.put(csClass, allowedSuffixes);
        });

        // The roles that should be removed from the member.
        Set<Role> rolesToRemove = new HashSet<>();

        // Check each role of the member.
        member.getRoles().forEach(role -> {
            try {
                CSClass roleClass = new CSClass(role.getName());
                CSSuffix roleSuffix = CSSuffix.fromClassName(role.getName());

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
            } catch (CSClass.InvalidClassNameException ignored) {
                // Not a class role.
            }
        });

        // Determine the roles to be added to the member.
        Set<Role> rolesToAdd = new HashSet<>();
        missingRolesMap.forEach((csClass, suffixes) -> {
            suffixes.forEach(suffix -> {
                rolesToAdd.add(discordService.getGuild().getRolesByName(CSNamingConventions.toRoleName(csClass, suffix), false).get(0));
            });
        });

        // Modify the roles of the member.
        modifyMemberRoles(member, rolesToAdd, rolesToRemove);

        // Update the member's nickname if we have power over them.
        if (!DiscordUtils.hasEqualOrHigherRole(discordService.getGuild().getSelfMember(), member))
            updateMemberNickname(member, csNickname);
    }

    /**
     * Modifies the roles of a guild member.
     *
     * @param member        The member whose roles to remove.
     * @param rolesToAdd    The roles to add to the member.
     * @param rolesToRemove The roles to remove from the member.
     */
    private void modifyMemberRoles(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) {
        // Log the removed roles.
        if (rolesToRemove.size() > 0) {
            Set<String> removeRoleNames = rolesToRemove.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Removing roles " + removeRoleNames + " from member " + member.getUser().getName());
        }

        // Log the added roles.
        if (rolesToAdd.size() > 0) {
            Set<String> addRoleNames = rolesToAdd.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Adding roles " + addRoleNames + " to member " + member.getUser().getName());
        }

        // Modify the roles.
        discordService.getGuild().getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
    }

    /**
     * Updates the member's nickname to match naming conventions.
     *
     * @param member     The member.
     * @param csNickname The parsed CS nickname for the member.
     */
    private void updateMemberNickname(Member member, CSNickname csNickname) {
        discordService.getGuild().getController().setNickname(member, csNickname.updateNicknameClassGroup(member.getNickname())).queue();
    }

}
