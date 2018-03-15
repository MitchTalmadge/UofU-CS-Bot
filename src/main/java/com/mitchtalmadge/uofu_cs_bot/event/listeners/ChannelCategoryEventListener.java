package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationService;
import net.dv8tion.jda.core.events.channel.category.GenericCategoryEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all Channel-Category-based events.
 */
public class ChannelCategoryEventListener extends EventListenerAbstract<GenericCategoryEvent> {

    private final DiscordSynchronizationService discordSynchronizationService;

    @Autowired
    public ChannelCategoryEventListener(DiscordSynchronizationService discordSynchronizationService) {
        this.discordSynchronizationService = discordSynchronizationService;
    }

    @Override
    public void onEvent(GenericCategoryEvent event) {
        discordSynchronizationService.requestSynchronization();
    }

}
