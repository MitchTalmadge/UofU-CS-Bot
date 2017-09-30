package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a Computer Science class, like CS-3500.
 */
public class CSClass implements Comparable<CSClass> {

    /**
     * The number associated with this class (e.g. 3500 for CS-3500).
     */
    private final int number;

    /**
     * Constructs a Computer Science class reference.
     *
     * @param number The number associated with the class (e.g. 3500 for CS-3500).
     */
    public CSClass(int number) {
        this.number = number;
    }

    /**
     * Constructs a Computer Science class reference from the given class number string.
     * The number string is how the class might be displayed to a user, such as "cs-3500".
     * Prefixes and suffixes allowed are those defined in {@link CSConstants}.
     *
     * @param numberString May be formatted like: "CS-3500" "cs-3500-ta" "CS3500" "3500" "3500-TA" ... etc
     */
    public CSClass(String numberString) {

        // New string that will be formatted for parsing while leaving the original intact.
        String formattedNumberString = numberString;

        // Remove all delimiters.
        formattedNumberString = formattedNumberString.replaceAll(CSConstants.CLASS_NUMBER_DELIMITER, "");

        // Remove all whitespace.
        formattedNumberString = formattedNumberString.replaceAll("\\s", "");

        // Remove prefixes.
        if (formattedNumberString.toUpperCase().startsWith(CSConstants.CS_PREFIX))
            formattedNumberString = formattedNumberString.substring(CSConstants.CS_PREFIX.length());

        // Remove suffixes.
        for (CSSuffix suffix : CSSuffix.values()) {
            // Skip the default suffix.
            if (suffix == CSSuffix.NONE)
                continue;

            // Check that the suffix exists in the string, and remove it if it does.
            if (formattedNumberString.toUpperCase().endsWith(suffix.getSuffix()))
                formattedNumberString = formattedNumberString.substring(0, formattedNumberString.length() - suffix.getSuffix().length());
        }

        // Try to parse the formattedNumberString as an int.
        try {
            this.number = Integer.parseInt(formattedNumberString.trim());
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Could not parse class number string to integer: " + numberString);
        }
    }

    /**
     * The number associated with this class (e.g. 3500 for CS-3500).
     */
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CSClass csClass = (CSClass) o;

        return number == csClass.number;
    }

    @Override
    public int hashCode() {
        return number;
    }

    /**
     * Compares two CSClass instances by their numbers.
     *
     * @param other The CSClass instance to compare to.
     * @return The result of Integer.compare for the two CSClass instances' numbers.
     */
    @Override
    public int compareTo(@NotNull CSClass other) {
        return Integer.compare(number, other.number);
    }
}
