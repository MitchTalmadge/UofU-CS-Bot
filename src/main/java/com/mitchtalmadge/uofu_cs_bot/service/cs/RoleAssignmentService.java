package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * A service for managing CS roles.
 */
@Service
public class RoleAssignmentService {

    private final LogService logService;

    @Autowired
    public RoleAssignmentService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Computes the CS roles that each member in a guild belongs to.
     *
     * @param guild The guild whose members' roles should be updated.
     */
    public void updateRoleAssignments(Guild guild) {
        // Update each member of the guild.
        for (Member member : guild.getMembers())
            updateRoleAssignments(member);
    }

    /**
     * Computes the CS roles that the member belongs to.
     *
     * @param member The member whose roles should be updated.
     */
    public void updateRoleAssignments(Member member) {
        logService.logInfo(getClass(), "Updating CS roles for " + member.getUser().getName());

        // Get the current class numbers
        Set<Integer> classNumbers = extractClassNumbers(member.getNickname());

        // Find the roles to remove and add.
        Set<Role> rolesToRemove = getRolesToRemove(member, classNumbers);
        Set<Role> rolesToAdd = getRolesToAdd(member, classNumbers);

        // Modify the roles of the member.
        modifyMemberRoles(member, rolesToAdd, rolesToRemove);
    }

    /**
     * From the given nickname, extracts the class numbers that the member is in.
     *
     * @param nickname The nickname.
     * @return An array containing the class numbers the member belongs to.
     */
    private Set<Integer> extractClassNumbers(String nickname) {
        Set<Integer> classNumbers = new HashSet<>();

        // Nickname was removed.
        if (nickname == null)
            return classNumbers;

        // Try to find class numbers.
        Matcher classNumbersSuffixMatcher = Constants.NICKNAME_CLASS_SUFFIX_PATTERN.matcher(nickname);
        boolean matchFound = classNumbersSuffixMatcher.find();

        // No class numbers found.
        if (!matchFound)
            return classNumbers;

        // Retrieve found class numbers.
        String classNumbersSuffix = classNumbersSuffixMatcher.group(1);
        String[] splitClassNumbersSuffix = classNumbersSuffix.split(Constants.CLASS_SPLIT_REGEX);
        for (String classNumber : splitClassNumbersSuffix) {
            try {
                classNumbers.add(Integer.parseInt(classNumber));
            } catch (NumberFormatException e) {
                logService.logException(getClass(), e, "Could not parse the class number: " + classNumber + " from nickname " + nickname);
            }
        }

        return classNumbers;
    }

    /**
     * Finds the roles which are no longer valid for a member based on their nickname.
     *
     * @param member       The member.
     * @param classNumbers The class numbers the member is assigned to.
     * @return The roles to remove.
     */
    private Set<Role> getRolesToRemove(Member member, Set<Integer> classNumbers) {
        List<Role> currentRoles = member.getRoles();

        // Find the roles which need to be removed.
        Set<Role> rolesToRemove = new HashSet<>();
        for (Role role : currentRoles) {
            // Get the class number of the role.
            int classNumber = getClassNumberFromRole(role);

            // If there is no class number, this is not a class number role.
            if (classNumber == -1)
                continue;

            // If the class number does not match one we belong to, add it to the roles to remove.
            if (!classNumbers.contains(classNumber))
                rolesToRemove.add(role);
        }

        return rolesToRemove;
    }

    /**
     * Finds the new roles that the member is now part of based on their nickname.
     *
     * @param member       The member.
     * @param classNumbers The class numbers the member is assigned to.
     * @return The new roles to be added to the member.
     */
    private Set<Role> getRolesToAdd(Member member, Set<Integer> classNumbers) {
        List<Role> currentRoles = member.getRoles();

        // Extract the name of each role, stored in lowercase form.
        Set<String> roleNames = new HashSet<>();
        for (Role role : currentRoles) {
            roleNames.add(role.getName().toLowerCase());
        }

        // Find the roles which need to be added.
        Set<String> namesOfRolesToAdd = new HashSet<>();
        for (Integer classNumber : classNumbers) {
            // The name of the role for this class number.
            String roleName = Constants.CS_PREFIX + classNumber;

            // Check that the user already has the role.
            if (roleNames.contains(roleName))
                continue;

            // User does not already have the role; add it.
            namesOfRolesToAdd.add(roleName);
        }

        // Try to get the roles from the names and return them.
        try {
            return getRolesFromNames(member.getGuild(), namesOfRolesToAdd);
        } catch (IllegalArgumentException e) {
            logService.logException(getClass(), e, "Could not add one or more roles to " + member.getUser().getName());
            return new HashSet<>();
        }
    }


    /**
     * Gets the class number associated with a given role.
     *
     * @param role The role.
     * @return The class number of the role, or -1 if it has no class number.
     */
    private int getClassNumberFromRole(Role role) {
        String roleName = role.getName();

        // Not a class number role.
        if (!roleName.toLowerCase().startsWith(Constants.CS_PREFIX.toLowerCase()))
            return -1;

        // Extract class number from the role by taking substring from the prefix.
        String classNumber = roleName.substring(Constants.CS_PREFIX.length());

        // Try to parse the class number.
        try {
            return Integer.parseInt(classNumber);
        } catch (NumberFormatException e) {
            // Not a class number.
            return -1;
        }
    }

    /**
     * Gets the role instances from the names of the roles for a guild.
     *
     * @param guild     The guild.
     * @param roleNames The names of the roles.
     * @return The roles from their names.
     */
    private Set<Role> getRolesFromNames(Guild guild, Set<String> roleNames) {
        // Get the roles from the role names.
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            // Find the roles with the given name.
            List<Role> rolesByName = guild.getRolesByName(roleName, true);

            // The role does not exist.
            if (rolesByName.isEmpty()) {
                logService.logError(getClass(), "The role " + roleName + " does not exist!");
                continue;
            }

            // Warn if there is too many roles found.
            if (rolesByName.size() > 1)
                logService.logError(getClass(), "There is more than one role with the name " + roleName);

            // Take the first role found.
            roles.add(rolesByName.get(0));
        }

        return roles;
    }

    /**
     * Modifies the roles of a guild member.
     *
     * @param member        The member whose roles to remove.
     * @param rolesToAdd    The roles to add to the member.
     * @param rolesToRemove The roles to remove from the member.
     */
    private void modifyMemberRoles(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) {
        // Log the removed roles.
        if (rolesToRemove.size() > 0) {
            Set<String> removeRoleNames = rolesToRemove.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Removing roles " + removeRoleNames + " from member " + member.getUser().getName());
        }

        // Log the added roles.
        if (rolesToAdd.size() > 0) {
            Set<String> addRoleNames = rolesToAdd.stream().map(Role::getName).collect(Collectors.toSet());
            logService.logInfo(getClass(), "Adding roles " + addRoleNames + " to member " + member.getUser().getName());
        }

        // Modify the roles.
        member.getGuild().getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
    }

}
