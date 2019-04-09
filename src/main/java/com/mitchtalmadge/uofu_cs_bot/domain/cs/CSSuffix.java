package com.mitchtalmadge.uofu_cs_bot.domain.cs;

import net.dv8tion.jda.core.Permission;

import java.awt.*;

/**
 * Contains all valid suffixes for CS courses.
 * Suffixes act as modifiers that can determine a user's capabilities.
 */
public enum CSSuffix {

    /**
     * The default (no) suffix. Example: "CS-3500".
     */
    NONE("", null, false, true,
            new Permission[]{
                    Permission.NICKNAME_CHANGE,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION,
            }
    ),

    /**
     * The suffix for TAs.
     */
    TA("TA", Color.decode("0x2ECC71"), true, true,
            new Permission[]{
                    Permission.NICKNAME_CHANGE,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION,
                    // Administrative privileges
                    Permission.MESSAGE_MANAGE,
                    Permission.KICK_MEMBERS,
                    Permission.VOICE_MUTE_OTHERS,
                    Permission.VOICE_DEAF_OTHERS
            }
    ),

    /**
     * The suffix for Professors.
     */
    PROFESSOR("PROF", Color.decode("0xE91E63"), true, true,
            new Permission[]{
                    Permission.NICKNAME_CHANGE,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ADD_REACTION,
                    // Administrative privileges
                    Permission.MESSAGE_MANAGE,
                    Permission.KICK_MEMBERS,
                    Permission.VOICE_MUTE_OTHERS,
                    Permission.VOICE_DEAF_OTHERS
            }
    );

    private final String suffix;
    private final Color roleColor;
    private final boolean roleHoisted;
    private final boolean roleMentionable;
    private final Permission[] permissions;

    /**
     * Creates a CS Suffix entry.
     *
     * @param suffix          The suffix itself. Should only include letters.
     * @param roleColor       The color of the suffix role.
     * @param roleHoisted     True if the suffix role should be displayed separately in the user list.
     * @param roleMentionable True if the suffix role can be mentioned.
     * @param permissions     The permissions for the suffix role.
     */
    CSSuffix(String suffix, Color roleColor, boolean roleHoisted, boolean roleMentionable, Permission[] permissions) {
        this.suffix = suffix.toUpperCase();
        this.roleColor = roleColor;
        this.roleHoisted = roleHoisted;
        this.roleMentionable = roleMentionable;
        this.permissions = permissions;
    }

    /**
     * From the given course name, determines which CSSuffix best matches the suffix.
     *
     * @param courseName The course name to check.
     * @return The CSSuffix instance that matches the course name. NONE is default.
     */
    public static CSSuffix fromCourseName(String courseName) {
        courseName = courseName.trim();

        // Try each suffix.
        for (CSSuffix suffix : values()) {
            // Skip the default suffix.
            if (suffix == CSSuffix.NONE)
                continue;

            if (courseName.toUpperCase().endsWith(suffix.getSuffix()))
                return suffix;
        }

        // If no suffix was found, return default suffix.
        return CSSuffix.NONE;
    }

    /**
     * The suffix itself. Always in uppercase.
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * The color of the suffix role.
     */
    public Color getRoleColor() {
        return roleColor;
    }

    /**
     * Whether the suffix role displays separately in the user list.
     */
    public boolean isRoleHoisted() {
        return roleHoisted;
    }

    /**
     * Whether the suffix role can be mentioned.
     */
    public boolean isRoleMentionable() {
        return roleMentionable;
    }

    /**
     * The permissions of the suffix role.
     */
    public Permission[] getPermissions() {
        return permissions;
    }
}
