package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.channel.CSChannelOrganizationService;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all voice-channel-based events.
 */
public class VoiceChannelEventListener extends EventListenerAbstract<GenericVoiceChannelEvent> {

    private final CSChannelOrganizationService csChannelOrganizationService;

    @Autowired
    public VoiceChannelEventListener(CSChannelOrganizationService csChannelOrganizationService) {
        this.csChannelOrganizationService = csChannelOrganizationService;
    }

    @Override
    public void onEvent(GenericVoiceChannelEvent event) {
        csChannelOrganizationService.requestOrganization();
    }

}
