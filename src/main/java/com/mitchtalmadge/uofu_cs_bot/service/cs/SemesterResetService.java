package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZoneId;

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

    private final LogService logService;
    private final DiscordService discordService;

    @Autowired
    public SemesterResetService(LogService logService,
                                DiscordService discordService) {
        this.logService = logService;
        this.discordService = discordService;
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
        MonthDay currentDay = MonthDay.now(ZoneId.of("MST"));

        // Determine if today is a reset day.
        for (MonthDay day : SEMESTER_RESET_DAYS) {
            if (day.equals(currentDay)){
                reset = true;
                break;
            }
        }

        // Don't reset if today is not a reset day.
        if(!reset)
            return;

        // Today is a reset day, begin resetting.
        resetRoles();
        resetChannels();
        announceReset();
    }

    /**
     * Removes all CS roles from all users in the server.
     */
    private void resetRoles() {
        // TODO
    }

    /**
     * Removes all CS channels, which will automatically be re-added by the
     * {@link com.mitchtalmadge.uofu_cs_bot.service.cs.channel.CSChannelSyncService}
     */
    private void resetChannels() {
        // TODO
    }

    /**
     * Makes an announcement that the server was reset for the beginning of the new semester.
     */
    private void announceReset() {
        // TODO
    }

}
