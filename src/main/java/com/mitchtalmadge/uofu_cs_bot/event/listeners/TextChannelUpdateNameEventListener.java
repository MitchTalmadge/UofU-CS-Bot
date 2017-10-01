package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class TextChannelUpdateNameEventListener extends EventListener<TextChannelUpdateNameEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public TextChannelUpdateNameEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(TextChannelUpdateNameEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
