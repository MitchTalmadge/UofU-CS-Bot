package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateBitrateEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelUpdateBitrateEventListener extends EventListener<VoiceChannelUpdateBitrateEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelUpdateBitrateEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelUpdateBitrateEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
