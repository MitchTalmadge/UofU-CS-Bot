package com.mitchtalmadge.uofu_cs_bot.service.cs.channel;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.CSClassService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CSChannelOrganizationService {

    private final LogService logService;
    private final DiscordService discordService;
    private final CSClassService csClassService;

    /**
     * Determines if organization should take place this tick.
     * Will be changed to true when requested.
     */
    private boolean organizationRequested = false;

    @Autowired
    public CSChannelOrganizationService(LogService logService,
                                        DiscordService discordService,
                                        CSClassService csClassService) {
        this.logService = logService;
        this.discordService = discordService;
        this.csClassService = csClassService;
    }

    /**
     * Requests that roles be organized on the next tick.
     */
    public void requestOrganization() {
        this.organizationRequested = true;
    }

    /**
     * Organizes channels and ensures that they have the proper permissions.
     * <p>
     * Will only organize when requested via the requestOrganization method.
     * <p>
     * Scheduled to run every minute, with a 15 second delay on startup.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    @Async
    protected void organize() {
        // Leave immediately if no organization has been requested.
        if (!organizationRequested) {
            return;
        }

        logService.logInfo(getClass(), "Organizing Channels...");



        logService.logInfo(getClass(), "Organization of Channels Completed.");
        organizationRequested = false;
    }



}
