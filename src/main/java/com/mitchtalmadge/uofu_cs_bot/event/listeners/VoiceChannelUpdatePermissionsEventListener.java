package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdatePermissionsEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class VoiceChannelUpdatePermissionsEventListener extends EventListener<VoiceChannelUpdatePermissionsEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public VoiceChannelUpdatePermissionsEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(VoiceChannelUpdatePermissionsEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
