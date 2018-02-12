package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationService;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all voice-channel-based events.
 */
public class VoiceChannelEventListener extends EventListenerAbstract<GenericVoiceChannelEvent> {

    private final DiscordSynchronizationService discordSynchronizationService;

    @Autowired
    public VoiceChannelEventListener(DiscordSynchronizationService discordSynchronizationService) {
        this.discordSynchronizationService = discordSynchronizationService;
    }

    @Override
    public void onEvent(GenericVoiceChannelEvent event) {
        discordSynchronizationService.requestSynchronization();
    }

}
