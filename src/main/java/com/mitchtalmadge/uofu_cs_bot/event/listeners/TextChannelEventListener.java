package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationRequestSurrogate;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/** Receives all text-channel-based events. */
public class TextChannelEventListener extends EventListenerAbstract<GenericTextChannelEvent> {

  private final DiscordSynchronizationRequestSurrogate discordSynchronizationService;

  @Autowired
  public TextChannelEventListener(
      DiscordSynchronizationRequestSurrogate discordSynchronizationRequestSurrogate) {
    this.discordSynchronizationService = discordSynchronizationRequestSurrogate;
  }

  @Override
  public void onEvent(GenericTextChannelEvent event) {
    discordSynchronizationService.requestSynchronization();
  }
}
