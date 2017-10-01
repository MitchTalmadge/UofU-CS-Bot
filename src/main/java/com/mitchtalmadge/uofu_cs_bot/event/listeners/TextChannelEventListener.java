package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.cs.channel.CSChannelOrganizationService;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Receives all text-channel-based events.
 */
public class TextChannelEventListener extends EventListenerAbstract<GenericTextChannelEvent> {

    private final CSChannelOrganizationService csChannelOrganizationService;

    @Autowired
    public TextChannelEventListener(CSChannelOrganizationService csChannelOrganizationService) {
        this.csChannelOrganizationService = csChannelOrganizationService;
    }

    @Override
    public void onEvent(GenericTextChannelEvent event) {
        csChannelOrganizationService.requestOrganization();
    }

}
