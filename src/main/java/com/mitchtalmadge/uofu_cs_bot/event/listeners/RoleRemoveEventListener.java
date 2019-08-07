package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleRemoveEventListener extends EventListenerAbstract<GuildMemberRoleRemoveEvent> {

    private RoleAssignmentService roleAssignmentService;

    @Autowired
    public RoleRemoveEventListener(RoleAssignmentService roleAssignmentService) {
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public void onEvent(GuildMemberRoleRemoveEvent event) {
        roleAssignmentService.assignRoles(event.getMember());
    }
}
