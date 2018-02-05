package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.CSRoleAssignmentService;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class NicknameEventListener extends EventListenerAbstract<GuildMemberNickChangeEvent> {

    private final LogService logService;
    private final CSRoleAssignmentService csRoleAssignmentService;

    @Autowired
    public NicknameEventListener(LogService logService,
                                 CSRoleAssignmentService csRoleAssignmentService) {
        this.logService = logService;
        this.csRoleAssignmentService = csRoleAssignmentService;
    }

    @Override
    public void onEvent(GuildMemberNickChangeEvent event) {
        logService.logInfo(getClass(), "Nickname changed for " + event.getMember().getUser().getName() + ". Old: " + event.getPrevNick() + " - New: " + event.getNewNick());

        csRoleAssignmentService.updateRoleAssignments(event.getMember());
    }


}
