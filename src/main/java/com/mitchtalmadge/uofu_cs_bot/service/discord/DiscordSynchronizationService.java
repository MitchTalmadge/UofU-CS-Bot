package com.mitchtalmadge.uofu_cs_bot.service.discord;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizationService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.nickname.NicknameService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleSynchronizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DiscordSynchronizationService {

    private LogService logService;
    private DiscordSynchronizationRequestSurrogate requestSurrogate;
    private final RoleSynchronizationService roleSynchronizationService;
    private RoleAssignmentService roleAssignmentService;
    private final ChannelSynchronizationService channelSynchronizationService;
    private NicknameService nicknameService;

    @Autowired
    public DiscordSynchronizationService(
            LogService logService,
            DiscordSynchronizationRequestSurrogate requestSurrogate,
            RoleSynchronizationService roleSynchronizationService,
            RoleAssignmentService roleAssignmentService,
            ChannelSynchronizationService channelSynchronizationService,
            NicknameService nicknameService) {
        this.logService = logService;
        this.requestSurrogate = requestSurrogate;
        this.roleSynchronizationService = roleSynchronizationService;
        this.roleAssignmentService = roleAssignmentService;
        this.channelSynchronizationService = channelSynchronizationService;
        this.nicknameService = nicknameService;
    }

    @PostConstruct
    public void init() {
        this.synchronizeServer();
    }

    /**
     * Forces server synchronization.
     */
    private void synchronizeServer() {
        logService.logInfo(getClass(), "Beginning Synchronization as Requested.");

        // Synchronize roles.
        logService.logInfo(getClass(), "Synchronizing Roles...");
        roleSynchronizationService.synchronize();

        // Synchronize channels, after roles.
        logService.logInfo(getClass(), "Synchronizing Channels...");
        channelSynchronizationService.synchronize();

        // Validate nicknames.
        logService.logInfo(getClass(), "Validating Nicknames...");
        nicknameService.validateNicknames();

        // Assign Roles.
        logService.logInfo(getClass(), "Assigning Roles...");
        roleAssignmentService.assignRoles();

        logService.logInfo(getClass(), "Synchronization Finished.");
    }

    /**
     * Begins server synchronization. <br/>
     * This may involve creating, deleting, modifying, or moving Roles and Channels as needed, or more.
     * <p>
     * Synchronization only takes place if requested.
     * <p>
     * This method will fire every 15 seconds, with an initial delay of 15 seconds.
     */
    @Scheduled(fixedDelay = 15_000, initialDelay = 15_000)
    @Async
    protected void handleSynchronizationRequests() {
        // Make sure we have a requested synchronization.
        if (!this.requestSurrogate.isSynchronizationRequested())
            return;

        this.requestSurrogate.clearSynchronizationRequests();

        this.synchronizeServer();
    }

}
