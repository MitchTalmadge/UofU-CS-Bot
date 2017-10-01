package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleUpdateNameEventListener extends EventListener<RoleUpdateNameEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public RoleUpdateNameEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(RoleUpdateNameEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
