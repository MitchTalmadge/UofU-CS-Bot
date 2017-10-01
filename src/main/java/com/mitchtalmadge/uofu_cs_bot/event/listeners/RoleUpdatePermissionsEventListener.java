package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.EntityOrganizationService;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePositionEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleUpdatePermissionsEventListener extends EventListener<RoleUpdatePermissionsEvent> {

    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public RoleUpdatePermissionsEventListener(EntityOrganizationService entityOrganizationService) {
        this.entityOrganizationService = entityOrganizationService;
    }

    @Override
    public void onEvent(RoleUpdatePermissionsEvent event) {
        entityOrganizationService.requestOrganization(event.getGuild());
    }
}
