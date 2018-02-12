package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationService;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all Role-based events.
 */
public class RoleEventListener extends EventListenerAbstract<GenericRoleEvent> {

    private final DiscordSynchronizationService discordSynchronizationService;

    @Autowired
    public RoleEventListener(DiscordSynchronizationService discordSynchronizationService) {
        this.discordSynchronizationService = discordSynchronizationService;
    }

    @Override
    public void onEvent(GenericRoleEvent event) {
        discordSynchronizationService.requestSynchronization();
    }

}
