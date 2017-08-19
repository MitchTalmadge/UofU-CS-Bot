package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Synchronizes server entities, such as channels and roles, with the list of valid class numbers
 * set in the environment variables.
 */
@Service
public class EntitySyncService {

    /**
     * Which types of channels are managed (created, deleted, etc) by the class numbers.
     */
    private static final ChannelType[] MANAGED_CHANNEL_TYPES = {ChannelType.TEXT, ChannelType.VOICE};

    private final LogService logService;
    private final ChannelService channelService;
    private final RoleService roleService;
    private final EntityOrganizationService entityOrganizationService;

    @Autowired
    public EntitySyncService(LogService logService,
                             ChannelService channelService,
                             RoleService roleService,
                             EntityOrganizationService entityOrganizationService) {
        this.logService = logService;
        this.channelService = channelService;
        this.roleService = roleService;
        this.entityOrganizationService = entityOrganizationService;
    }

    /**
     * Creates and deletes the necessary entities, including roles and channels, based on the classes environment variable.
     * <p>
     * Afterwards, calls on EntityOrganizationService to organize the entities, which have likely been altered by this method.
     */
    public void syncEntities(Guild guild) {
        // Ensure env var exists
        String classes = System.getenv(Constants.CS_CLASS_ENV_VAR);
        if (classes == null || classes.isEmpty()) {
            throw new IllegalArgumentException("The '" + Constants.CS_CLASS_ENV_VAR + "' environment variable is missing or empty!");
        }

        logService.logInfo(getClass(), "Synchronizing CS Classes for Guild '" + guild.getName() + "': " + classes);

        // Get class numbers from the env var.
        int[] classNumbers;
        try {
            classNumbers = getClassNumbersFromClassesList(classes);
        } catch (IllegalArgumentException e) {
            logService.logException(getClass(), e, "Could not extract class numbers from env var");
            return;
        }

        // Perform this sync on all managed channel types.
        for (ChannelType channelType : MANAGED_CHANNEL_TYPES) {
            logService.logInfo(getClass(), "Syncing " + channelType.name() + " Channels");

            // Delete the invalid channels.
            deleteInvalidChannels(guild, channelType, classNumbers);

            // Add the missing channels.
            addMissingChannels(guild, channelType, classNumbers);
        }

        // Sync roles
        logService.logInfo(getClass(), "Syncing Roles");

        // Delete the invalid roles.
        deleteInvalidRoles(guild, classNumbers);

        // Add the missing roles.
        addMissingRoles(guild, classNumbers);

        logService.logInfo(getClass(), "Synchronization Complete.");

        // Request organization of the guild, to account for updated roles and channels.
        entityOrganizationService.requestOrganization(guild);
    }

    /**
     * From the list of class numbers (comma separated), extracts and parses the individual numbers.
     *
     * @param classes The list of class numbers.
     * @return An array containing all the class numbers.
     * @throws IllegalArgumentException If one of the numbers is not parsable.
     */
    private int[] getClassNumbersFromClassesList(String classes) throws IllegalArgumentException {
        // Split the classes up and turn them into integers.
        String[] splitClasses = classes.split(Constants.CLASS_SPLIT_REGEX);
        int[] classNumbers = new int[splitClasses.length];
        for (int i = 0; i < splitClasses.length; i++) {
            try {
                classNumbers[i] = Integer.parseInt(splitClasses[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("This is not a valid class number: " + splitClasses[i]);
            }
        }

        return classNumbers;
    }

    /**
     * From the guild and array of class numbers, determines which channels must be deleted, and does so.
     *
     * @param guild        The guild to delete channels from.
     * @param channelType  The type of channels to delete.
     * @param classNumbers The valid class numbers.
     */
    private void deleteInvalidChannels(Guild guild, ChannelType channelType, int[] classNumbers) {
        determineChannelsToDelete(classNumbers, channelService.getAllChannels(guild, channelType))
                .forEach(c -> {
                    logService.logInfo(getClass(), "Deleting channel: " + c.getName());
                    channelService.deleteChannel(c);
                });
    }

    /**
     * From the guild and array of class numbers, determines which roles must be deleted, and does so.
     *
     * @param guild        The guild to delete roles from.
     * @param classNumbers The valid class numbers.
     */
    private void deleteInvalidRoles(Guild guild, int[] classNumbers) {
        determineRolesToDelete(classNumbers, roleService.getAllRoles(guild))
                .forEach(r -> {
                    logService.logInfo(getClass(), "Deleting role: " + r.getName());
                    roleService.deleteRole(r);
                });
    }

    /**
     * From the current class numbers and channels, determines which channels need to be deleted.
     *
     * @param classNumbers The class numbers.
     * @param channels     The current channels.
     * @return A set containing channels which should be deleted from the given list.
     */
    private <C extends Channel> Set<C> determineChannelsToDelete(int[] classNumbers, Collection<C> channels) {
        // Filter down the list of channels.
        return channels.stream().filter(c -> isOrphanedByName(classNumbers, c.getName())).collect(Collectors.toSet());
    }

    /**
     * From the current class numbers and roles, determines which roles need to be deleted.
     *
     * @param classNumbers The class numbers.
     * @param roles        The current roles.
     * @return A set containing roles which should be deleted from the given list.
     */
    private Set<Role> determineRolesToDelete(int[] classNumbers, Collection<Role> roles) {
        // Filter down the list of roles.
        return roles.stream().filter(r -> isOrphanedByName(classNumbers, r.getName())).collect(Collectors.toSet());
    }

    /**
     * Determines if the name of the channel, role, or whatever else is:
     * 1. A properly formatted CS name (such as cs-1410)
     * 2. Orphaned -- meaning it does not appear in the array of class numbers.
     * <p>
     * This is used to determine if the owner is old and should be deleted.
     *
     * @param classNumbers The class numbers.
     * @param name         The name of the channel, role, or something else.
     * @return True if the item is orphaned and should be removed.
     */
    private boolean isOrphanedByName(int[] classNumbers, String name) {
        // Ignore those which do not start with the CS prefix.
        if (!name.toLowerCase().startsWith(Constants.CS_PREFIX.toLowerCase()))
            return false;

        // Ignore those which do not end in a parsable number.
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(name.substring(Constants.CS_PREFIX.length()));
        } catch (NumberFormatException e) {
            return false;
        }

        // Ignore those which exist in the array of class numbers.
        for (int classNumber : classNumbers) {
            if (name.equalsIgnoreCase(Constants.CS_PREFIX + classNumber))
                return false;
        }

        // All checks passed, meaning this is an orphan.
        return true;
    }

    /**
     * From the guild and array of class numbers, determines which channels must be created, and does so.
     * Permissions are not assigned at this point; they are purely new channels.
     *
     * @param guild        The guild to create channels on.
     * @param channelType  The type of channels to create.
     * @param classNumbers The valid class numbers.
     */
    private void addMissingChannels(Guild guild, ChannelType channelType, int[] classNumbers) {
        List<Channel> channels = channelService.getAllChannels(guild, channelType);

        // Check each number to see if a channel for it exists.
        for (int classNumber : classNumbers) {
            // Find any channel that matches the class number, and continue if one is found.
            if (channels.stream().anyMatch(c -> c.getName().equalsIgnoreCase(Constants.CS_PREFIX + classNumber)))
                continue;

            // No channel found; create one.
            String newChannelName = (Constants.CS_PREFIX + classNumber).toLowerCase();
            logService.logInfo(getClass(), "Creating Channel: " + newChannelName);
            channelService.createChannel(guild, channelType, newChannelName);
        }
    }

    /**
     * From the guild and array of class numbers, determines which roles must be created, and does so.
     *
     * @param guild        The guild to create roles on.
     * @param classNumbers The valid class numbers.
     */
    private void addMissingRoles(Guild guild, int[] classNumbers) {
        List<Role> roles = roleService.getAllRoles(guild);

        // Check each number to see if a role for it exists.
        for (int classNumber : classNumbers) {
            // Find any role that matches the class number, and continue if one is found.
            if (roles.stream().anyMatch(r -> r.getName().equalsIgnoreCase(Constants.CS_PREFIX + classNumber)))
                continue;

            // No role found; create one.
            String newRoleName = (Constants.CS_PREFIX + classNumber).toLowerCase();
            logService.logInfo(getClass(), "Creating Role: " + newRoleName);
            roleService.createRole(guild, newRoleName, Constants.CS_ROLE_COLOR, Constants.CS_ROLE_HOISTED, Constants.CS_ROLE_MENTIONABLE, Constants.CS_ROLE_PERMISSIONS);
        }
    }
}
