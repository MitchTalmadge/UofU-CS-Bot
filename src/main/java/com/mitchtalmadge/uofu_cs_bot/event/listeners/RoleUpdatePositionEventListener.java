package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleUpdatePositionEventListener extends EventListener<RoleUpdatePositionEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public RoleUpdatePositionEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(RoleUpdatePositionEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
