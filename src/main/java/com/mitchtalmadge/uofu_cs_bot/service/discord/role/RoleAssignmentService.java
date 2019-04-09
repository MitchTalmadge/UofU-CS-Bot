package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This service provides the ability to trigger the functionality of assigning or un-assigning various roles of users.
 */
@Service
public class RoleAssignmentService {

    private LogService logService;
    private DiscordService discordService;
    private Set<RoleAssigner> roleAssigners;

    @Autowired
    public RoleAssignmentService(
            LogService logService,
            DiscordService discordService,
            Set<RoleAssigner> roleAssigners) {
        this.logService = logService;
        this.discordService = discordService;
        this.roleAssigners = roleAssigners;
    }

    /**
     * Updates the role assignments for all members.
     */
    public void assignRoles() {
        for (Member member : discordService.getGuild().getMembers()) {
            assignRoles(member);
        }
    }

    /**
     * Updates the role assignments for one member.
     *
     * @param member The member to update.
     */
    public void assignRoles(Member member) {
        logService.logInfo(getClass(), "Assigning roles for member " + member.getUser().getName());

        Set<Role> rolesToAdd = new HashSet<>();
        Set<Role> rolesToRemove = new HashSet<>();

        for (RoleAssigner roleAssigner : roleAssigners) {
            roleAssigner.updateRoleAssignments(member, rolesToAdd, rolesToRemove);
        }

        // Log the added roles.
        if (!rolesToAdd.isEmpty()) {
            Set<String> addRoleNames = rolesToAdd.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Adding roles " + addRoleNames + " to member " + member.getUser().getName());
        }

        // Log the removed roles.
        if (!rolesToRemove.isEmpty()) {
            Set<String> removeRoleNames = rolesToRemove.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Removing roles " + removeRoleNames + " from member " + member.getUser().getName());
        }

        // Modify the roles.
        discordService.getGuild().getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
    }

}
