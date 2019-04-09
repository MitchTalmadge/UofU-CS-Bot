package com.mitchtalmadge.uofu_cs_bot.service.discord.role;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the structure for assigning roles to users.
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

    public void assignRoles() {
        for(RoleAssigner roleAssigner : roleAssigners) {
            for (Member member : discordService.getGuild().getMembers()) {
                Pair<Set<Role>, Set<Role>> assignmentResult = roleAssigner.updateRoleAssignments(member);

                // Log the removed roles.
                if (assignmentResult.getLeft() != null && assignmentResult.getLeft().size() > 0) {
                    Set<String> removeRoleNames = assignmentResult.getLeft().stream().map(Role::getName).collect(Collectors.toSet());
                    logService.logInfo(getClass(), "Removing roles " + removeRoleNames + " from member " + member.getUser().getName());
                }

                // Log the added roles.
                if (assignmentResult.getRight() != null && assignmentResult.getRight().size() > 0) {
                    Set<String> addRoleNames = assignmentResult.getRight().stream().map(Role::getName).collect(Collectors.toSet());
                    logService.logInfo(getClass(), "Adding roles " + addRoleNames + " to member " + member.getUser().getName());
                }

                // Modify the roles.
                discordService.getGuild().getController().modifyMemberRoles(member, assignmentResult.getRight(), assignmentResult.getLeft()).queue();
            }
        }
    }

}
