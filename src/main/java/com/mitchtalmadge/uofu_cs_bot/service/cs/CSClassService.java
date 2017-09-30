package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keeps track of the enabled CS Classes from the environment variables.
 */
@Service
public class CSClassService {

    /**
     * The environment variable that contains the list of enabled classes.
     */
    private static final String CS_CLASSES_ENV_VAR = "CLASSES";

    /**
     * Pattern used for splitting the enabled classes into individual class numbers.
     */
    private static final Pattern CLASS_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");
    private final LogService logService;

    /**
     * The enabled CS classes for the server.
     */
    private Set<CSClass> enabledClasses = new HashSet<>();

    @Autowired
    public CSClassService(LogService logService) {
        this.logService = logService;
    }

    @PostConstruct
    private void init() {
        // Get the class number list from the environment variable.
        String classNumberList = System.getenv(CS_CLASSES_ENV_VAR);
        if (classNumberList == null || classNumberList.isEmpty()) {
            throw new IllegalArgumentException("The class list environment variable (" + CS_CLASSES_ENV_VAR + ") is missing or empty!");
        }

        // Split the list into individual class numbers.
        String[] classNumbers = CLASS_SPLIT_PATTERN.split(classNumberList);

        // Parse each class number.
        for (String classNumber : classNumbers) {
            try {
                enabledClasses.add(new CSClass(classNumber));
            } catch (IllegalArgumentException e) {
                logService.logException(getClass(), e, "A class number could not be parsed from the list of enabled classes.");
            }
        }
    }

    /**
     * @return An unmodifiable set of enabled classes.
     */
    public Set<CSClass> getEnabledClasses() {
        return Collections.unmodifiableSet(enabledClasses);
    }
}
