package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keeps track of the enabled CS Courses from the environment variables.
 */
@Service
public class CourseService implements InitializingBean {

    /**
     * The environment variable that contains the list of enabled courses.
     */
    private static final String CS_COURSES_ENV_VAR = "COURSES";

    /**
     * Pattern used for splitting the enabled courses into individual course numbers.
     */
    private static final Pattern COURSE_LIST_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");
    private final LogService logService;

    /**
     * The enabled CS courses for the server.
     */
    private Set<Course> enabledCourses = new HashSet<>();

    @Autowired
    public CourseService(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Get the course number list from the environment variable.
        String courseNumberList = System.getenv(CS_COURSES_ENV_VAR);
        if (courseNumberList == null) {
            throw new IllegalArgumentException("The course list environment variable (" + CS_COURSES_ENV_VAR + ") is missing!");
        }

        // Split the list into individual class numbers.
        String[] courseNumbers = COURSE_LIST_SPLIT_PATTERN.split(courseNumberList);

        // Parse each class number.
        for (String courseNumber : courseNumbers) {
            if(courseNumber.isEmpty())
                continue;

            try {
                enabledCourses.add(new Course(courseNumber));
            } catch (Course.InvalidCourseNameException e) {
                logService.logException(getClass(), e, "A course name could not be parsed from the list of enabled courses.");
            }
        }
    }

    /**
     * @return An unmodifiable set of enabled courses.
     */
    public Set<Course> getEnabledCourses() {
        return Collections.unmodifiableSet(enabledCourses);
    }
}
