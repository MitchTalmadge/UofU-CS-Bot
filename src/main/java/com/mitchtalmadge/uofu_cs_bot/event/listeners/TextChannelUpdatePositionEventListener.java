package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class TextChannelUpdatePositionEventListener extends EventListener<TextChannelUpdatePositionEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public TextChannelUpdatePositionEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(TextChannelUpdatePositionEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
