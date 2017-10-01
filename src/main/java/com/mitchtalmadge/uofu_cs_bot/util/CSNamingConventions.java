package com.mitchtalmadge.uofu_cs_bot.util;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Utilities for conforming to role, channel, and nickname naming conventions for Computer Science entities.
 */
public class CSNamingConventions {

    private CSNamingConventions() {}

    /**
     * Combines a CSClass and CSSuffix into a properly formatted role name.
     *
     * @param csClass  The class.
     * @param csSuffix The suffix.
     * @return The role name.
     */
    public static String toRoleName(CSClass csClass, CSSuffix csSuffix) {
        StringBuilder output = new StringBuilder();

        // Append prefix and class number.
        output.append(CSConstants.CS_PREFIX).append(CSConstants.CLASS_NUMBER_DELIMITER).append(csClass.getNumber());

        // Append suffix if necessary.
        if (csSuffix != CSSuffix.NONE)
            output.append(CSConstants.CLASS_NUMBER_DELIMITER).append(csSuffix.getSuffix());

        return output.toString().toLowerCase();
    }

    /**
     * From a CSClass instance, creates a properly formatted text/voice channel name.
     *
     * @param csClass The class.
     * @return The channel name.
     */
    public static String toChannelName(CSClass csClass) {
        return (CSConstants.CS_PREFIX + CSConstants.CLASS_NUMBER_DELIMITER + csClass.getNumber()).toLowerCase();
    }

    /**
     * From a map of classes to suffixes, creates a properly formatted nickname class group (e.g. "[2420 TA, 3500]")
     *
     * @param csClassMap The map of classes to suffixes.
     * @return The nickname class group.
     */
    public static String toNicknameClassGroup(Map<CSClass, CSSuffix> csClassMap) {
        // Sort the classes.
        Map<CSClass, CSSuffix> sortedMap = new TreeMap<>();
        sortedMap.putAll(csClassMap);

        StringBuilder output = new StringBuilder();
        output.append('[');

        // Join all classes together.
        StringJoiner classJoiner = new StringJoiner(", ");
        sortedMap.forEach((csClass, suffix) -> classJoiner.add(toNicknameClass(csClass, suffix)));
        output.append(classJoiner);

        output.append(']');

        return output.toString();
    }

    /**
     * Combines a CSClass and CSSuffix into a properly formatted class name for a guild member's nickname.
     *
     * @param csClass  The class.
     * @param csSuffix The suffix.
     * @return The class name.
     */
    public static String toNicknameClass(CSClass csClass, CSSuffix csSuffix) {
        StringBuilder output = new StringBuilder();
        output.append(csClass.getNumber());

        if (csSuffix != CSSuffix.NONE)
            output.append(' ').append(csSuffix.getSuffix());

        return output.toString().toUpperCase();
    }

}
