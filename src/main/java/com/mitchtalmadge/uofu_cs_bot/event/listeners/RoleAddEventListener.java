package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleAddEventListener extends EventListenerAbstract<GuildMemberRoleAddEvent> {

    private RoleAssignmentService roleAssignmentService;

    @Autowired
    public RoleAddEventListener(RoleAssignmentService roleAssignmentService) {
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public void onEvent(GuildMemberRoleAddEvent event) {
        roleAssignmentService.assignRoles(event.getMember());
    }
}
