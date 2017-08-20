package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.EntitySyncService;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class TextChannelDeleteEventListener extends EventListener<TextChannelDeleteEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public TextChannelDeleteEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(TextChannelDeleteEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }

}
