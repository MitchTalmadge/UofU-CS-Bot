package com.mitchtalmadge.uofu_cs_bot.service.cs;

import net.dv8tion.jda.core.Permission;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Constants for the CS component of the application.
 */
class Constants {

    /**
     * The prefix for roles and channels based on the nickname suffix class numbers.
     * For example, cs-3500
     * Case insensitive.
     */
    static final String CS_PREFIX = "cs-";

    /**
     * The suffix for roles of Teacher Aides (TAs).
     * For example, cs-3500-ta
     * Case insensitive.
     */
    static final String CS_TA_SUFFIX = "-ta";

    /**
     * The environment variable that contains the list of valid classes.
     */
    static final String CS_CLASS_ENV_VAR = "CLASSES";

    /**
     * The color to assign to CS roles.
     */
    static final Color CS_ROLE_COLOR = Color.decode("0x3498DB");

    /**
     * The color to assign to CS TA roles.
     */
    static final Color CS_TA_ROLE_COLOR = Color.decode("0x43C567");

    /**
     * Determines if the class roles are hoisted (displayed separately)
     */
    static final boolean CS_ROLE_HOISTED = false;

    /**
     * Determines if the class roles are mentionable by anyone.
     */
    static final boolean CS_ROLE_MENTIONABLE = true;

    /**
     * The permissions used for all CS roles.
     */
    static final Set<Permission> CS_ROLE_PERMISSIONS = new HashSet<>();

    static {
        // Assign permissions.
        CS_ROLE_PERMISSIONS.add(Permission.NICKNAME_CHANGE);
        CS_ROLE_PERMISSIONS.add(Permission.MESSAGE_WRITE);
        CS_ROLE_PERMISSIONS.add(Permission.MESSAGE_EMBED_LINKS);
        CS_ROLE_PERMISSIONS.add(Permission.MESSAGE_ATTACH_FILES);
        CS_ROLE_PERMISSIONS.add(Permission.MESSAGE_HISTORY);
        CS_ROLE_PERMISSIONS.add(Permission.MESSAGE_ADD_REACTION);
        CS_ROLE_PERMISSIONS.add(Permission.VOICE_SPEAK);
        CS_ROLE_PERMISSIONS.add(Permission.VOICE_USE_VAD);
    }

    /**
     * The bitrate for voice channels.
     */
    static final int CS_CHANNEL_VOICE_BITRATE = 64000;

    /**
     * The user limit for voice channels; 0 = unlimited. May not be lower than 0 or higher than 99.
     */
    static final int CS_CHANNEL_VOICE_USERLIMIT = 0;

    /**
     * Matches class numbers in the suffix of a nickname. Use group #1 to get all, split with the CLASS_SPLIT_REGEX.
     */
    static final Pattern NICKNAME_CLASS_SUFFIX_PATTERN = Pattern.compile("\\[\\s*((\\d{4}\\s*(TA)?(,\\s*)*)+)\\s*]");

    /**
     * Used to split a list of class numbers from a nickname suffix into individual numbers.
     */
    static final String CLASS_SPLIT_REGEX = "(,\\s*)+";

}
