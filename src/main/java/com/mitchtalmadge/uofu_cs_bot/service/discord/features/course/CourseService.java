package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import org.springframework.beans.factory.InitializingBean;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordSynchronizationRequestSurrogate;
import net.dv8tion.jda.core.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keeps track of the enabled CS Courses from the environment variables.
 */
@Service
public class CourseService implements InitializingBean {

    /**
     * The environment variable that contains the list of blacklisted courses.
     */
    private static final String COURSE_BLACKLIST_ENV_VAR = "COURSE_BLACKLIST";

    /**
     * Pattern used for splitting the courses env var into individual course numbers.
     */
    private static final Pattern COURSE_LIST_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");

    private final LogService logService;
    private DiscordService discordService;
    private DiscordSynchronizationRequestSurrogate discordSynchronizationRequestSurrogate;

    /**
     * The enabled CS courses for the server.
     */
    private Set<Course> enabledCourses = new HashSet<>();

    /**
     * The blacklisted CS courses for the server.
     */
    private Set<Course> blacklistedCourses = new HashSet<>();

    @Autowired
    public CourseService(LogService logService,
                         DiscordService discordService,
                         DiscordSynchronizationRequestSurrogate discordSynchronizationRequestSurrogate) {
        this.logService = logService;
        this.discordService = discordService;
        this.discordSynchronizationRequestSurrogate = discordSynchronizationRequestSurrogate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Blacklisted courses.
        this.loadBlacklistedCourses();

        // Enabled courses.
        this.computeEnabledCourses();
    }

    private void loadBlacklistedCourses() {
        this.blacklistedCourses = new HashSet<>();

        // Get the list.
        String courseNumberList = System.getenv(COURSE_BLACKLIST_ENV_VAR);
        if (courseNumberList == null)
            return; // No list.

        // Split the list into individual course numbers.
        String[] courseNumbers = COURSE_LIST_SPLIT_PATTERN.split(courseNumberList);

        // Parse courses.
        for (String courseNumber : courseNumbers) {

            if (courseNumber.isEmpty())
                continue; // Empty string.

            try {
                blacklistedCourses.add(new Course(courseNumber));
            } catch (Course.InvalidCourseNameException e) {
                logService.logException(getClass(), e, "A blacklisted course could not be parsed.");
            }
        }
    }

    /**
     * From the nicknames of each member in the server, determines the enabled courses.
     */
    public void computeEnabledCourses() {
        List<Member> members = this.discordService.getGuild().getMembers();
        Set<Course> enabledCourses = new HashSet<>();

        // Add courses found in nicknames.
        members.forEach(member -> {
            CSNickname nickname = new CSNickname(member.getNickname());
            enabledCourses.addAll(nickname.getClasses());
        });

        // Exclude blacklisted courses.
        enabledCourses.removeAll(this.blacklistedCourses);

        // Request synchronization if the enabled courses have changed.
        if (!this.enabledCourses.equals(enabledCourses)) {
            this.discordSynchronizationRequestSurrogate.requestSynchronization();
        }

        this.enabledCourses = enabledCourses;
    }

    public Set<Course> getEnabledCourses() {
        return Collections.unmodifiableSet(enabledCourses);
    }

    public Set<Course> getBlacklistedCourses() {
        return Collections.unmodifiableSet(blacklistedCourses);
    }
}
