package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class NicknameEventListener extends EventListenerAbstract<GuildMemberNickChangeEvent> {

    private final LogService logService;
    private RoleAssignmentService roleAssignmentService;

    @Autowired
    public NicknameEventListener(LogService logService,
                                 RoleAssignmentService roleAssignmentService) {
        this.logService = logService;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public void onEvent(GuildMemberNickChangeEvent event) {
        logService.logInfo(getClass(), "Nickname changed for " + event.getMember().getUser().getName() + ". Old: " + event.getPrevNick() + " - New: " + event.getNewNick());

        roleAssignmentService.assignRoles(event.getMember());
    }


}
