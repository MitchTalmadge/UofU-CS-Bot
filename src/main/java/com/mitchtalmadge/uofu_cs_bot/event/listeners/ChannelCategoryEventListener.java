package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizationService;
import net.dv8tion.jda.core.events.channel.category.GenericCategoryEvent;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all Channel-Category-based events.
 */
public class ChannelCategoryEventListener extends EventListenerAbstract<GenericCategoryEvent> {

    private final ChannelSynchronizationService channelSynchronizationService;

    @Autowired
    public ChannelCategoryEventListener(ChannelSynchronizationService channelSynchronizationService) {
        this.channelSynchronizationService = channelSynchronizationService;
    }

    @Override
    public void onEvent(GenericCategoryEvent event) {
        channelSynchronizationService.requestSynchronization();
    }

}
