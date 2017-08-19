package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class TextChannelUpdateNSFWEventListener extends EventListener<TextChannelUpdateNSFWEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public TextChannelUpdateNSFWEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(TextChannelUpdateNSFWEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
