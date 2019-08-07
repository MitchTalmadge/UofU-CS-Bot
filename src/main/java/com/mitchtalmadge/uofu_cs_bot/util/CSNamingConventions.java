package com.mitchtalmadge.uofu_cs_bot.util;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSSuffix;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Course;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Utilities for conforming to role, channel, and nickname naming conventions for Computer Science
 * entities.
 */
public class CSNamingConventions {

  private CSNamingConventions() {
  }

  /**
   * Combines a Course and CSSuffix into a properly formatted role name.
   *
   * @param course   The class.
   * @param csSuffix The suffix.
   * @return The role name.
   */
  public static String toRoleName(Course course, CSSuffix csSuffix) {
    StringBuilder output = new StringBuilder();

    // Append prefix and class number.
    output
            .append(CSConstants.CS_PREFIX)
            .append(CSConstants.COURSE_NUMBER_DELIMITER)
            .append(course.getNumber());

    // Append suffix if necessary.
    if (csSuffix != CSSuffix.NONE)
      output.append(CSConstants.COURSE_NUMBER_DELIMITER).append(csSuffix.getSuffix());

    return output.toString().toLowerCase();
  }

  /**
   * From a Course instance, creates a properly formatted channel name.
   *
   * @param course The class.
   * @return The channel name.
   */
  public static String toChannelName(Course course) {
    return (CSConstants.CS_PREFIX + CSConstants.COURSE_NUMBER_DELIMITER + course.getNumber())
            .toLowerCase();
  }

  /**
   * From a map of courses to suffixes, creates a properly formatted nickname class group (e.g.
   * "[2420 TA, 3500]")
   *
   * @param csClassMap The map of courses to suffixes.
   * @return The nickname class group.
   */
  public static String toNicknameClassGroup(Map<Course, CSSuffix> csClassMap) {
    // Sort the courses.
    Map<Course, CSSuffix> sortedMap = new TreeMap<>(csClassMap);

    StringBuilder output = new StringBuilder();
    output.append('[');

    // Join all courses together.
    StringJoiner classJoiner = new StringJoiner(",");
    sortedMap.forEach((csClass, suffix) -> classJoiner.add(toNicknameClass(csClass, suffix)));
    output.append(classJoiner);

    output.append(']');

    return output.toString();
  }

  /**
   * Combines a Course and CSSuffix into a properly formatted class name for a guild member's
   * nickname.
   *
   * @param course   The class.
   * @param csSuffix The suffix.
   * @return The class name.
   */
  public static String toNicknameClass(Course course, CSSuffix csSuffix) {
    StringBuilder output = new StringBuilder();
    output.append(course.getNumber());

    if (csSuffix != CSSuffix.NONE) output.append(csSuffix.getSuffix());

    return output.toString().toUpperCase();
  }
}
