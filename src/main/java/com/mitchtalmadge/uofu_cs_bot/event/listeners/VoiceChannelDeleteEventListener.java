package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelDeleteEventListener extends EventListener<VoiceChannelDeleteEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelDeleteEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelDeleteEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }

}
