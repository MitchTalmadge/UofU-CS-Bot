package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.EntitySyncService;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelCreateEventListener extends EventListener<VoiceChannelCreateEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelCreateEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelCreateEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }

}
