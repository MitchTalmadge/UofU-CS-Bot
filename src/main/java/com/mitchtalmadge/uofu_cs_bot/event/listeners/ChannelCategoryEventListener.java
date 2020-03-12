package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationRequestSurrogate;
import net.dv8tion.jda.api.events.channel.category.GenericCategoryEvent;
import org.springframework.beans.factory.annotation.Autowired;

/** Receives all Channel-Category-based events. */
public class ChannelCategoryEventListener extends EventListenerAbstract<GenericCategoryEvent> {

  private final DiscordSynchronizationRequestSurrogate discordSynchronizationService;

  @Autowired
  public ChannelCategoryEventListener(
      DiscordSynchronizationRequestSurrogate discordSynchronizationRequestSurrogate) {
    this.discordSynchronizationService = discordSynchronizationRequestSurrogate;
  }

  @Override
  public void onEvent(GenericCategoryEvent event) {
    discordSynchronizationService.requestSynchronization();
  }
}
