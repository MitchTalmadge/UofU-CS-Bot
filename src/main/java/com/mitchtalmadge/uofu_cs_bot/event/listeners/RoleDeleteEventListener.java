package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.EntitySyncService;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleDeleteEventListener extends EventListener<RoleDeleteEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public RoleDeleteEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(RoleDeleteEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
