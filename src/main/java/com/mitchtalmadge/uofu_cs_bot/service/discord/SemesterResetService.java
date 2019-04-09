package com.mitchtalmadge.uofu_cs_bot.service.discord;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.channel.ChannelSynchronizationService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.course.CourseRoleAssignmentService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private static final String SEMESTER_RESET_ANNOUNCEMENT = "@everyone\n" +
            "\n" +
            "Welcome to a new semester! **All class-specific channels and roles have been reset.** Please update your nickname with any CS courses you are enrolled in this semester, and remember to invite your friends!\n" +
            "\n" +
            "*Note: If you are a TA for any courses, just let a moderator know.*\n" +
            "*Invite Link:* **bit.ly/csattheu**";

    private final LogService logService;
    private final DiscordService discordService;
    private final DiscordSynchronizationService discordSynchronizationService;
    private final CourseRoleAssignmentService courseRoleAssignmentService;

    @Autowired
    public SemesterResetService(LogService logService,
                                DiscordService discordService,
                                DiscordSynchronizationService discordSynchronizationService,
                                CourseRoleAssignmentService courseRoleAssignmentService) {
        this.logService = logService;
        this.discordService = discordService;
        this.discordSynchronizationService = discordSynchronizationService;
        this.courseRoleAssignmentService = courseRoleAssignmentService;
    }

    /**
     * Checks if it is the end of the semester each day at 12pm MST.
     * <p>
     * If it is the end of the semester, deletes all CS class channels
     * and removes all CS roles from users.
     * <p>
     * Finally, puts out an announcement that it is the end of the semester.
     */
    @Scheduled(cron = "0 0 12 * * *", zone = "America/Denver")
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
                courseRoleAssignmentService.updateMemberNickname(member, CSNickname.EMPTY);
                courseRoleAssignmentService.updateRoleAssignments(member);
            }
        });
    }

    /**
     * Removes all CS channels, which will automatically be re-added by the
     * {@link ChannelSynchronizationService}
     */
    private void resetChannels() {
        // Delete all CS channels
        discordService.getGuild().getTextChannels().forEach(channel -> {
            try {
                // Parse the channel as a course to ensure it is actually a course channel.
                new Course(channel.getName());

                logService.logInfo(getClass(), "Deleting Text Channel: " + channel.getName());
                channel.delete().complete();

            } catch (Course.InvalidCourseNameException ignored) {
                // This channel is not a course channel.
            }
        });

        // Request synchronization, which will re-add CS channels.
        discordSynchronizationService.requestSynchronization();
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
