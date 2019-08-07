package com.mitchtalmadge.uofu_cs_bot.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class CommandPatternComparator implements Comparator<CommandPattern> {

  private final Command command;

  CommandPatternComparator(Command command) {
    this.command = command;
  }

  @Override
  public int compare(CommandPattern pattern1, CommandPattern pattern2) {
    // Determine who is strict and who is loose.
    if (pattern1.strict() && pattern2.strict()) { // Both are strict.
      return compareBothStrict(pattern1, pattern2);
    } else if (pattern1.strict()) { // Left is strict.
      return compareOneStrict(pattern1, pattern2);
    } else if (pattern2.strict()) { // Right is strict.
      return -compareOneStrict(pattern2, pattern1);
    } else { // Both are loose.
      return compareBothLoose(pattern1, pattern2);
    }
  }

  /**
   * Compare two patterns in which both are strict.
   *
   * @param strict1 The first pattern.
   * @param strict2 The second pattern.
   * @return -1 if strict1 matches but not strict2, 0 if both or neither match equally, and 1 if
   *     strict2 matches but not strict1.
   */
  private int compareBothStrict(CommandPattern strict1, CommandPattern strict2) {
    if (Arrays.equals(strict1.value(), command.getArgs())) {
      if (Arrays.equals(strict2.value(), command.getArgs())) return 0;
      return -1;
    } else {
      if (Arrays.equals(strict2.value(), command.getArgs())) return 1;
      return 0;
    }
  }

  /**
   * Compare two patterns in which one is strict and the other is loose.
   *
   * @param strict The strict pattern.
   * @param loose The loose pattern.
   * @return -1 if strict matches more than loose, 0 if both or neither match equally, and 1 if
   *     loose matches more than strict.
   */
  private int compareOneStrict(CommandPattern strict, CommandPattern loose) {
    if (Arrays.equals(strict.value(), command.getArgs())) {
      if (strict.value().length > loose.value().length) return -1;

      int currentArg = 0;
      while (currentArg < loose.value().length
          && loose.value()[currentArg].equals(command.getArgs()[currentArg])) {
        currentArg++;
      }

      return Integer.compare(currentArg, strict.value().length);
    } else {
      int currentArg = 0;
      while (currentArg < loose.value().length
          && loose.value()[currentArg].equals(command.getArgs()[currentArg])) {
        currentArg++;
      }

      return currentArg == 0 ? 0 : 1;
    }
  }

  /**
   * Compare two patterns in which both are loose.
   *
   * @param loose1 The first pattern.
   * @param loose2 The second pattern.
   * @return -1 if loose1 matches but not loose2, 0 if both or neither match equally, and 1 if
   *     loose2 matches but not loose1.
   */
  private int compareBothLoose(CommandPattern loose1, CommandPattern loose2) {
    int pattern1Score = 0, pattern2Score = 0;

    // Compute scores.
    for (int i = 0; i < command.getArgs().length; i++) {
      if (winsPoint(loose1, i)) pattern1Score++;
      if (winsPoint(loose2, i)) pattern2Score++;
    }

    // Return results.
    return Integer.compare(pattern2Score, pattern1Score);
  }

  /**
   * Determines if the loose pattern wins a comparison point.
   *
   * @param pattern The loose pattern.
   * @param argIndex The index of the argument to check.
   * @return True if the pattern wins a point, false otherwise.
   */
  private boolean winsPoint(CommandPattern pattern, int argIndex) {
    // Check for out of bounds
    if (pattern.value().length > argIndex) {
      // Check if the pattern's argument at this index matches the command's argument at this index.
      return pattern.value()[argIndex].equalsIgnoreCase(command.getArgs()[argIndex]);
    }

    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CommandPatternComparator that = (CommandPatternComparator) o;

    return Objects.equals(command, that.command);
  }

  @Override
  public int hashCode() {
    return command != null ? command.hashCode() : 0;
  }
}
