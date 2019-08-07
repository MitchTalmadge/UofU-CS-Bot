package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Represents a Computer Science course, like CS-3500. */
public class Course implements Comparable<Course> {

  /** The number associated with this course (e.g. 3500 for CS-3500). */
  private final int number;

  /**
   * Constructs a Computer Science course reference.
   *
   * @param number The number associated with the course (e.g. 3500 for CS-3500).
   */
  public Course(int number) {
    this.number = number;
  }

  /**
   * Constructs a Computer Science course reference from the given course name. The course name is
   * how the course might be displayed to a user, such as "cs-3500".
   *
   * @param courseName May be formatted like: "CS-3500" "cs-3500-ta" "CS3500" "3500" "3500-TA" ...
   *     etc
   * @throws InvalidCourseNameException If the provided course name cannot be parsed.
   */
  public Course(String courseName) throws InvalidCourseNameException {

    // New course name that will be formatted for parsing while leaving the original intact.
    String formattedCourseName = courseName;

    // Remove all delimiters.
    formattedCourseName = formattedCourseName.replaceAll(CSConstants.COURSE_NUMBER_DELIMITER, "");

    // Remove all whitespace.
    formattedCourseName = formattedCourseName.replaceAll("\\s", "");

    // Remove prefixes.
    if (formattedCourseName.toUpperCase().startsWith(CSConstants.CS_PREFIX))
      formattedCourseName = formattedCourseName.substring(CSConstants.CS_PREFIX.length());

    // Remove suffixes.
    for (CSSuffix suffix : CSSuffix.values()) {
      // Skip the default suffix.
      if (suffix == CSSuffix.NONE) continue;

      // Check that the suffix exists in the string, and remove it if it does.
      if (formattedCourseName.toUpperCase().endsWith(suffix.getSuffix()))
        formattedCourseName =
            formattedCourseName.substring(
                0, formattedCourseName.length() - suffix.getSuffix().length());
    }

    // Try to parse the formattedCourseName as an int.
    try {
      this.number = Integer.parseInt(formattedCourseName.trim());
    } catch (NumberFormatException ignored) {
      throw new InvalidCourseNameException(courseName);
    }
  }

  /** The number associated with this course (e.g. 3500 for CS-3500). */
  public int getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return String.valueOf(number);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Course course = (Course) o;
    return number == course.number;
  }

  @Override
  public int hashCode() {
    return Objects.hash(number);
  }

  /**
   * Compares two Course instances by their numbers.
   *
   * @param other The Course instance to compare to.
   * @return The result of Integer.compare for the two Course instances' numbers.
   */
  @Override
  public int compareTo(@NotNull Course other) {
    return Integer.compare(number, other.number);
  }

  public static class InvalidCourseNameException extends Exception {

    private InvalidCourseNameException(String courseName) {
      super("The provided course name ('" + courseName + "') is invalid and cannot be parsed.");
    }
  }
}
