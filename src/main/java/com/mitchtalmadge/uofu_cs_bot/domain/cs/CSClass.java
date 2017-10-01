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
     * Constructs a Computer Science class reference from the given class name.
     * The class name is how the class might be displayed to a user, such as "cs-3500".
     *
     * @param className May be formatted like: "CS-3500" "cs-3500-ta" "CS3500" "3500" "3500-TA" ... etc
     * @throws InvalidClassNameException If the provided class name cannot be parsed.
     */
    public CSClass(String className) throws InvalidClassNameException {

        // New class name that will be formatted for parsing while leaving the original intact.
        String formattedClassName = className;

        // Remove all delimiters.
        formattedClassName = formattedClassName.replaceAll(CSConstants.CLASS_NUMBER_DELIMITER, "");

        // Remove all whitespace.
        formattedClassName = formattedClassName.replaceAll("\\s", "");

        // Remove prefixes.
        if (formattedClassName.toUpperCase().startsWith(CSConstants.CS_PREFIX))
            formattedClassName = formattedClassName.substring(CSConstants.CS_PREFIX.length());

        // Remove suffixes.
        for (CSSuffix suffix : CSSuffix.values()) {
            // Skip the default suffix.
            if (suffix == CSSuffix.NONE)
                continue;

            // Check that the suffix exists in the string, and remove it if it does.
            if (formattedClassName.toUpperCase().endsWith(suffix.getSuffix()))
                formattedClassName = formattedClassName.substring(0, formattedClassName.length() - suffix.getSuffix().length());
        }

        // Try to parse the formattedClassName as an int.
        try {
            this.number = Integer.parseInt(formattedClassName.trim());
        } catch (NumberFormatException ignored) {
            throw new InvalidClassNameException(className);
        }
    }

    /**
     * The number associated with this class (e.g. 3500 for CS-3500).
     */
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

    public static class InvalidClassNameException extends Exception {

        private InvalidClassNameException(String className) {
            super("The provided class name ('" + className + "') is invalid and cannot be parsed.");
        }
    }
}
