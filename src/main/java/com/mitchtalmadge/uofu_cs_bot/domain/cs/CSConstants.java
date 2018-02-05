package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import java.util.regex.Pattern;

/**
 * Constants for Computer Science nicknames, channels, roles, etc.
 */
public class CSConstants {

    /**
     * The prefix of class numbers in nicknames, channels, and roles.
     * Always in uppercase.
     */
    public static final String CS_PREFIX = "CS";

    /**
     * The delimiter between prefixes, class numbers, and suffixes in a nickname, channel, or role.
     */
    public static final String COURSE_NUMBER_DELIMITER = "-";

    /**
     * The bitrate for voice channels.
     */
    public static final int CS_CHANNEL_VOICE_BITRATE = 64000;

    /**
     * The user limit for voice channels; 0 = unlimited. May not be lower than 0 or higher than 99.
     */
    public static final int CS_CHANNEL_VOICE_USERLIMIT = 0;

}
