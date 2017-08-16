package com.mitchtalmadge.uofu_cs_bot.service.cs;

import java.util.regex.Pattern;

/**
 * Constants for the CS component of the application.
 */
class Constants {

    /**
     * The prefix for roles and channels based on the nickname suffix class numbers.
     * For example, 3500 = cs-3500
     * Case insensitive.
     */
    static final String CS_PREFIX = "cs-";

    /**
     * The environment variable that contains the list of valid classes.
     */
    static final String CS_CLASS_ENV_VAR = "CLASSES";

    /**
     * Matches class numbers in the suffix of a nickname. Use group #1 to get all, split with the CLASS_NUMBER_SPLIT_REGEX.
     */
    static final Pattern NICKNAME_CLASS_SUFFIX_PATTERN = Pattern.compile("\\[\\s*((\\d{4}(,\\s*)*)+)\\s*]");

    /**
     * Used to split a list of class numbers from a nickname suffix into individual numbers.
     */
    static final String CLASS_SPLIT_REGEX = "(,\\s*)+";

}
