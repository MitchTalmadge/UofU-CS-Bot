package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationService;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all text-channel-based events.
 */
public class TextChannelEventListener extends EventListenerAbstract<GenericTextChannelEvent> {

    private final DiscordSynchronizationService discordSynchronizationService;

    @Autowired
    public TextChannelEventListener(DiscordSynchronizationService discordSynchronizationService) {
        this.discordSynchronizationService = discordSynchronizationService;
    }

    @Override
    public void onEvent(GenericTextChannelEvent event) {
        discordSynchronizationService.requestSynchronization();
    }

}
