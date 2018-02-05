package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizationService;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all text-channel-based events.
 */
public class TextChannelEventListener extends EventListenerAbstract<GenericTextChannelEvent> {

    private final ChannelSynchronizationService channelSynchronizationService;

    @Autowired
    public TextChannelEventListener(ChannelSynchronizationService channelSynchronizationService) {
        this.channelSynchronizationService = channelSynchronizationService;
    }

    @Override
    public void onEvent(GenericTextChannelEvent event) {
        channelSynchronizationService.requestSynchronization();
    }

}
