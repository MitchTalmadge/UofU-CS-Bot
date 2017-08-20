package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelUpdatePositionEventListener extends EventListener<VoiceChannelUpdatePositionEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelUpdatePositionEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelUpdatePositionEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
