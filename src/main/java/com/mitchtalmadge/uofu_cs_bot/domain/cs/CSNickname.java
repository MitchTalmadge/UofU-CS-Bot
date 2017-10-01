package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the CS classes within a single guild member's nickname.
 */
public class CSNickname {

    /**
     * Pattern used for parsing the group of classes in a nickname: "John Doe [3500, CS-2420-TA]" -> "[3500, CS-2420-TA]".
     */
    private static final Pattern CLASS_GROUP_PARSE_PATTERN = Pattern.compile("[\\[\\(]\\s*(((CS-?\\s*)?\\d+\\s*(-?[a-zA-Z]+)?(,\\s*)*)+)\\s*[\\]\\)]");

    /**
     * Pattern used for splitting the group of classes into individual class strings.
     */
    private static final Pattern CLASS_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");

    /**
     * Maps the member's discovered classes to the suffixes associated with those classes.
     */
    private Map<CSClass, CSSuffix> classMap = new TreeMap<>();

    /**
     * Constructs an instance from the specified nickname.
     *
     * @param nickname The nickname to parse. May be null for no roles.
     */
    public CSNickname(String nickname) {
        if (nickname == null) {
            return;
        }

        // Search for class number group in nickname.
        Matcher groupMatcher = CLASS_GROUP_PARSE_PATTERN.matcher(nickname.toUpperCase());
        boolean matchFound = groupMatcher.find();

        // No class number group found.
        if (!matchFound)
            return;

        // Parse each individual class number.
        String classNumberGroup = groupMatcher.group(1);
        String[] classNumbers = CLASS_SPLIT_PATTERN.split(classNumberGroup);
        for (String classNumber : classNumbers) {
            try {
                // Determine class.
                CSClass csClass = new CSClass(classNumber);

                // Determine suffix.
                CSSuffix csSuffix = CSSuffix.fromClassName(classNumber);

                // Save.
                classMap.put(csClass, csSuffix);
            } catch (CSClass.InvalidClassNameException ignored) {
                // Could not parse the class. Invalid format.
            }
        }
    }

    /**
     * @return An unmodifiable set of classes parsed from this nickname.
     */
    public Set<CSClass> getClasses() {
        return Collections.unmodifiableSet(classMap.keySet());
    }

    /**
     * Gets the suffix associated with the given class.
     *
     * @param csClass The class.
     * @return The suffix of the given class, or null if the nickname does not include the given class.
     */
    public CSSuffix getSuffixForClass(CSClass csClass) {
        return classMap.getOrDefault(csClass, null);
    }

}
