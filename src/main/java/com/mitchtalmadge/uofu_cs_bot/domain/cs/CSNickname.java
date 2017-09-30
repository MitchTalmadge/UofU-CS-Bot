package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import net.dv8tion.jda.core.entities.Member;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the CS classes within a single guild member's nickname.
 */
public class CSNickname {

    /**
     * Pattern used for parsing the group of classes in a nickname: "John Doe [3500, CS-2420-TA]" -> "[3500, CS-2420-TA]".
     */
    private static final Pattern CLASS_GROUP_PARSE_PATTERN = Pattern.compile("\\[?\\(?\\s*(((CS\\s*)?\\d+\\s*(-?[a-zA-Z]+)?(,\\s*)*)+)\\s*]?\\)?");

    /**
     * Pattern used for splitting the group of classes into individual class strings.
     */
    private static final Pattern CLASS_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");

    /**
     * The member associated with this nickname.
     */
    private final Member member;

    /**
     * Maps the member's discovered classes to the suffixes associated with those classes.
     */
    private Map<CSClass, CSSuffix> classMap = new HashMap<>();

    /**
     * Constructs an instance from the specified guild member.
     *
     * @param member The guild member whose nickname should be parsed.
     */
    public CSNickname(Member member) {
        this.member = member;

        // Make sure member has a nickname
        if (member.getNickname() == null) {
            return;
        }

        // Search for class number group in nickname.
        Matcher groupMatcher = CLASS_GROUP_PARSE_PATTERN.matcher(member.getNickname());
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
                CSSuffix csSuffix = CSSuffix.fromClassNumber(classNumber);

                // Save.
                classMap.put(csClass, csSuffix);
            } catch (IllegalArgumentException ignored) {
                // Could not parse the class. Invalid format.
            }
        }
    }

    /**
     * @return The member associated with this nickname.
     */
    public Member getMember() {
        return member;
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
