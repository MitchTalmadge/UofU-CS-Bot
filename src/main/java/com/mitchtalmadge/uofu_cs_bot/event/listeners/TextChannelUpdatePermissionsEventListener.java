package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class TextChannelUpdatePermissionsEventListener extends EventListener<TextChannelUpdatePermissionsEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public TextChannelUpdatePermissionsEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(TextChannelUpdatePermissionsEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
