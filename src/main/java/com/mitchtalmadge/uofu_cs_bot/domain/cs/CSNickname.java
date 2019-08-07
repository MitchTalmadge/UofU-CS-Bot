package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Represents the CS courses within a single guild member's nickname. */
public class CSNickname {

  /**
   * Pattern used for parsing the group of courses in a nickname: "John Doe [3500, CS-2420-TA]" ->
   * "[3500, CS-2420-TA]".
   */
  private static final Pattern CLASS_GROUP_PARSE_PATTERN =
      Pattern.compile(
          "[\\[\\(]\\s*(((cs-?\\s*)?\\d+\\s*(-?[a-z]+)?(,\\s*)*)+)\\s*[\\]\\)]",
          Pattern.CASE_INSENSITIVE);

  /** Pattern used for splitting the group of courses into individual class strings. */
  private static final Pattern CLASS_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");

  /** An immutable, empty CS Nickname. */
  public static final CSNickname EMPTY = new CSNickname();

  /** Maps the member's discovered courses to the suffixes associated with those courses. */
  private Map<Course, CSSuffix> classMap = new TreeMap<>();

  /** Creates an empty CS Nickname instance. */
  private CSNickname() {}

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
    Matcher groupMatcher = CLASS_GROUP_PARSE_PATTERN.matcher(nickname);
    boolean matchFound = groupMatcher.find();

    // No class number group found.
    if (!matchFound) return;

    // Parse each individual class number.
    String classNumberGroup = groupMatcher.group(1);
    String[] classNumbers = CLASS_SPLIT_PATTERN.split(classNumberGroup);
    for (String classNumber : classNumbers) {
      try {
        // Determine class.
        Course course = new Course(classNumber);

        // Determine suffix.
        CSSuffix csSuffix = CSSuffix.fromCourseName(classNumber);

        // Save.
        classMap.put(course, csSuffix);
      } catch (Course.InvalidCourseNameException ignored) {
        // Could not parse the class. Invalid format.
      }
    }
  }

  /** @return An unmodifiable set of courses parsed from this nickname. */
  public Set<Course> getClasses() {
    return Collections.unmodifiableSet(classMap.keySet());
  }

  /**
   * Gets the suffix associated with the given class.
   *
   * @param course The class.
   * @return The suffix of the given class, or null if the nickname does not include the given
   *     class.
   */
  public CSSuffix getSuffixForClass(Course course) {
    return classMap.getOrDefault(course, null);
  }

  /**
   * Given a nickname, replaces the nickname class group with the properly formatted group composed
   * of this instance's CS courses and suffixes. For example: "John Doe (CS3500, CS1410-ta)" ->
   * "John Doe [1410 TA, 3500]"
   *
   * @param nickname The nickname to replace the group of.
   * @return The modified nickname with the new class group.
   */
  public String updateNicknameClassGroup(String nickname) {
    if (nickname == null) return null;

    return CLASS_GROUP_PARSE_PATTERN
        .matcher(nickname)
        .replaceAll(CSNamingConventions.toNicknameClassGroup(classMap));
  }
}
