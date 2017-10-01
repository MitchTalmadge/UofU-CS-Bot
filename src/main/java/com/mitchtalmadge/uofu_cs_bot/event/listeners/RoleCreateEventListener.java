package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleCreateEventListener extends EventListener<RoleCreateEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public RoleCreateEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(RoleCreateEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }

}
