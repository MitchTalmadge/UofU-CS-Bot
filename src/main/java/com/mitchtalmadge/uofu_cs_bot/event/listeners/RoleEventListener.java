package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationRequestSurrogate;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all Role-based events.
 */
public class RoleEventListener extends EventListenerAbstract<GenericRoleEvent> {

    private final DiscordSynchronizationRequestSurrogate discordSynchronizationService;

    @Autowired
    public RoleEventListener(
            DiscordSynchronizationRequestSurrogate discordSynchronizationRequestSurrogate) {
        this.discordSynchronizationService = discordSynchronizationRequestSurrogate;
    }

    @Override
    public void onEvent(GenericRoleEvent event) {
        discordSynchronizationService.requestSynchronization();
    }
}
