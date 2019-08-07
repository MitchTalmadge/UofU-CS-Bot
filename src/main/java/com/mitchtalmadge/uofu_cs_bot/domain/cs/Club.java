package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Represents a Computer Science club, like ACM. */
public class Club implements Comparable<Club> {

  /** The name associated with this club (eg. "ACM"). */
  private final String name;

  /**
   * Constructs a Computer Science club reference.
   *
   * @param name The name of the club (eg. "ACM").
   */
  public Club(String name) {
    this.name = name;
  }

  /** @return The name of the club. */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Club club = (Club) o;
    return Objects.equals(name, club.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name);
  }

  /**
   * Compares two clubs by their names.
   *
   * @param other The Club instance to compare to.
   * @return The result of string comparison (ignoring case) for the club names.
   */
  @Override
  public int compareTo(@NotNull Club other) {
    return name.compareToIgnoreCase(other.name);
  }
}
