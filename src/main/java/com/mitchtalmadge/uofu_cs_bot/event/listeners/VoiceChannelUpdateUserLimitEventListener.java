package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateBitrateEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateUserLimitEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelUpdateUserLimitEventListener extends EventListener<VoiceChannelUpdateUserLimitEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelUpdateUserLimitEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelUpdateUserLimitEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
