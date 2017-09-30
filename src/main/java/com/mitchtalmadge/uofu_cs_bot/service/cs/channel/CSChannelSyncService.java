package com.mitchtalmadge.uofu_cs_bot.service.cs.channel;

import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.CSClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CSChannelSyncService {

    private final LogService logService;
    private final DiscordService discordService;
    private final CSClassService csClassService;

    @Autowired
    public CSChannelSyncService(LogService logService,
                                DiscordService discordService,
                                CSClassService csClassService) {
        this.logService = logService;
        this.discordService = discordService;
        this.csClassService = csClassService;
    }

    @PostConstruct
    private void init() {

    }

    private void SyncChannelCategories() {

    }


}
