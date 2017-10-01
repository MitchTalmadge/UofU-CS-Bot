package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelUpdateNameEventListener extends EventListener<VoiceChannelUpdateNameEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelUpdateNameEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelUpdateNameEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
