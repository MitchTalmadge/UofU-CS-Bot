package com.mitchtalmadge.uofu_cs_bot.domain.cs;

/**
 * Represents a class number within a nickname.
 */
public class NickClassNumber {

    private final int classNumber;
    private final boolean isTeachersAide;

    /**
     * Constructs an instance with the given class number.
     *
     * @param classNumber    The class number.
     * @param isTeachersAide Whether or not the user is a TA for this class.
     */
    public NickClassNumber(int classNumber, boolean isTeachersAide) {
        this.classNumber = classNumber;
        this.isTeachersAide = isTeachersAide;
    }

    /**
     * The number of the class, such as "3500".
     */
    public int getClassNumber() {
        return classNumber;
    }

    /**
     * Whether or not the user is a TA for this class.
     */
    public boolean isTeachersAide() {
        return isTeachersAide;
    }
}
