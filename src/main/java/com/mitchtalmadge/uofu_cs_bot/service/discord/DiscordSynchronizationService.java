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

@Service
public class DiscordSynchronizationService {

    private LogService logService;
    private final RoleSynchronizationService roleSynchronizationService;
    private DiscordService discordService;
    private RoleAssignmentService roleAssignmentService;
    private final ChannelSynchronizationService channelSynchronizationService;
    private NicknameService nicknameService;

    /**
     * Whenever another class requests synchronization, this field will be set to true. <br/>
     * On the next scheduled synchronization, this field is checked to determine if synchronization should take place. <br/>
     * Once finished, this field is set to false again.
     * <p>
     * This field is set to true initially so that synchronization occurs at least once on startup.
     */
    private boolean synchronizationRequested = true;

    @Autowired
    public DiscordSynchronizationService(
            LogService logService,
            DiscordService discordService,
            RoleSynchronizationService roleSynchronizationService,
            RoleAssignmentService roleAssignmentService,
            ChannelSynchronizationService channelSynchronizationService,
            NicknameService nicknameService) {
        this.logService = logService;
        this.discordService = discordService;
        this.roleSynchronizationService = roleSynchronizationService;
        this.roleAssignmentService = roleAssignmentService;
        this.channelSynchronizationService = channelSynchronizationService;
        this.nicknameService = nicknameService;
    }

    /**
     * Requests that Roles and Channels be synchronized at the next scheduled time.
     */
    public void requestSynchronization() {
        synchronizationRequested = true;
    }

    /**
     * Begins synchronization of Roles and Channels. <br/>
     * This may involve creating, deleting, modifying, or moving Roles and Channels as needed.
     * <p>
     * Synchronization only takes place if synchronizationRequested is true.
     * <p>
     * This method will fire every 15 seconds, with an initial delay of 15 seconds.
     *
     * @see #synchronizationRequested
     */
    @Scheduled(fixedDelay = 15_000, initialDelay = 15_000)
    @Async
    protected void synchronize() {
        // Make sure we have a requested synchronization.
        if (!synchronizationRequested)
            return;

        synchronizationRequested = false;

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

}
