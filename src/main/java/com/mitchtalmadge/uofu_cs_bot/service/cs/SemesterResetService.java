package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.channel.CSChannelSyncService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.role.CSRoleAssignmentService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Resets all assigned CS roles and wipes all CS chat rooms at the end of each semester.
 *
 * @author Mitch Talmadge
 */
@Service
public class SemesterResetService {

    /**
     * Contains days on which the semester reset should be triggered.
     */
    private static final MonthDay[] SEMESTER_RESET_DAYS = {
            MonthDay.of(Month.AUGUST, 10),
            MonthDay.of(Month.JANUARY, 5)
    };

    /**
     * The announcement to send when the semester resets.
     */
    private static final String SEMESTER_RESET_ANNOUNCEMENT;

    static {

        // Load announcement from file
        String tempAnnouncement = null;
        try {
            tempAnnouncement = new String(Files.readAllBytes(new ClassPathResource("semester_reset_announcement.md").getFile().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SEMESTER_RESET_ANNOUNCEMENT = tempAnnouncement;
    }

    private final LogService logService;
    private final DiscordService discordService;
    private final CSChannelSyncService channelSyncService;
    private final CSRoleAssignmentService roleAssignmentService;

    @Autowired
    public SemesterResetService(LogService logService,
                                DiscordService discordService,
                                CSChannelSyncService channelSyncService,
                                CSRoleAssignmentService roleAssignmentService) {
        this.logService = logService;
        this.discordService = discordService;
        this.channelSyncService = channelSyncService;
        this.roleAssignmentService = roleAssignmentService;
    }

    /**
     * Checks if it is the end of the semester each day at 12pm MST.
     * <p>
     * If it is the end of the semester, deletes all CS class channels
     * and removes all CS roles from users.
     * <p>
     * Finally, puts out an announcement that it is the end of the semester.
     */
    @Scheduled(cron = "0 0 19 * * *")
    @Async
    protected void semesterReset() {

        boolean reset = false;
        MonthDay currentDay = MonthDay.now(ZoneId.of("America/Denver"));

        // Determine if today is a reset day.
        for (MonthDay day : SEMESTER_RESET_DAYS) {
            if (day.equals(currentDay)) {
                reset = true;
                break;
            }
        }

        // Don't reset if today is not a reset day.
        if (!reset)
            return;

        logService.logInfo(getClass(), "!!!!!!!!!!!!!! Initiating Semester Reset !!!!!!!!!!!!!!");

        // Today is a reset day, begin resetting.
        resetRoles();
        resetChannels();
        announceReset();
    }

    /**
     * Removes all CS roles from all users in the server.
     */
    private void resetRoles() {
        // Clear all CS roles from all members.
        discordService.getGuild().getMembers().forEach(member -> {
            // Update the member's nickname if we have power over them.
            if (!DiscordUtils.hasEqualOrHigherRole(discordService.getGuild().getSelfMember(), member)) {
                roleAssignmentService.updateMemberNickname(member, CSNickname.EMPTY);
                roleAssignmentService.updateRoleAssignments(member);
            }
        });
    }

    /**
     * Removes all CS channels, which will automatically be re-added by the
     * {@link com.mitchtalmadge.uofu_cs_bot.service.cs.channel.CSChannelSyncService}
     */
    private void resetChannels() {
        // Delete all CS channels
        discordService.getGuild().getTextChannels().forEach(channel -> {
            try {
                // Parse the channel as a class to ensure it is actually a class channel.
                new CSClass(channel.getName());

                logService.logInfo(getClass(), "Deleting Text Channel: " + channel.getName());
                channel.delete().complete();

            } catch (CSClass.InvalidClassNameException ignored) {
                // This channel is not a class channel.
            }
        });

        // Request organization, which will re-add CS channels.
        channelSyncService.BeginSynchronization();
    }

    /**
     * Makes an announcement that the server was reset for the beginning of the new semester.
     */
    private void announceReset() {

        // Find announcement channel
        List<TextChannel> channels = discordService.getGuild().getTextChannelsByName("announcements", true);

        if (channels.isEmpty()) {
            logService.logError(getClass(), "No #announcements text channel found!");
            return;
        }

        TextChannel announcementChannel = channels.get(0);

        // Try to make an announcement
        if (!announcementChannel.canTalk()) {
            logService.logError(getClass(), "No permission to talk in the #announcements text channel.");
            return;
        }

        // Delay 1 minute to allow for channel synchronization.
        announcementChannel.sendMessage(SEMESTER_RESET_ANNOUNCEMENT).queueAfter(1, TimeUnit.MINUTES);
        logService.logInfo(getClass(), "Announcement scheduled to post in 1 minute.");
    }

}
