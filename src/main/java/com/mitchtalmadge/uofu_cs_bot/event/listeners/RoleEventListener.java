package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.role.CSRoleOrganizationService;
import net.dv8tion.jda.core.events.role.GenericRoleEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all role-based events.
 */
public class RoleEventListener extends EventListenerAbstract<GenericRoleEvent> {

    private final CSRoleOrganizationService csRoleOrganizationService;

    @Autowired
    public RoleEventListener(CSRoleOrganizationService csRoleOrganizationService) {
        this.csRoleOrganizationService = csRoleOrganizationService;
    }

    @Override
    public void onEvent(GenericRoleEvent event) {
        csRoleOrganizationService.requestOrganization();
    }

}
