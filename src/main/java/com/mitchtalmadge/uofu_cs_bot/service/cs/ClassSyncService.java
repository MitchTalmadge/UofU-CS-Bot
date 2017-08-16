package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Watches the list of classes in the environment vars and synchronizes channels and roles to match it,
 * creating and deleting them as necessary.
 */
@Service
public class ClassSyncService {

    /**
     * Which types of channels are managed (created, deleted, etc) by the class numbers.
     */
    private static final ChannelType[] MANAGED_CHANNEL_TYPES = {ChannelType.TEXT, ChannelType.VOICE};

    private final LogService logService;
    private final DiscordService discordService;
    private final ChannelService channelService;

    @Autowired
    public ClassSyncService(LogService logService,
                            DiscordService discordService,
                            ChannelService channelService) {
        this.logService = logService;
        this.discordService = discordService;
        this.channelService = channelService;
    }

    @PostConstruct
    public void init() {
        syncClasses();
    }

    /**
     * Updates all the channels, roles, etc. for the classes in the environment variable.
     */
    public void syncClasses() {
        // Ensure env var exists
        String classes = System.getenv(Constants.CS_CLASS_ENV_VAR);
        if (classes == null || classes.isEmpty()) {
            throw new IllegalArgumentException("The '" + Constants.CS_CLASS_ENV_VAR + "' environment variable is missing or empty!");
        }

        logService.logInfo(getClass(), "Synchronizing CS classes: " + classes);

        // Get class numbers from the env var.
        int[] classNumbers;
        try {
            classNumbers = getClassNumbersFromClassesList(classes);
        } catch (IllegalArgumentException e) {
            logService.logException(getClass(), e, "Could not extract class numbers from env var");
            return;
        }

        // Perform this sync on all guilds.
        List<Guild> guilds = discordService.getJDA().getGuilds();
        for (Guild guild : guilds) {

            // Perform this sync on all managed channel types.
            for (ChannelType channelType : MANAGED_CHANNEL_TYPES) {
                logService.logInfo(getClass(), "Syncing " + channelType.name() + " channels on Guild " + guild.getName());

                // Delete the invalid channels.
                deleteInvalidChannels(guild, channelType, classNumbers);

                // Add the missing channels.
                addMissingChannels(guild, channelType, classNumbers);
            }
        }

        logService.logInfo(getClass(), "Synchronization of CS classes complete.");
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
     * From the current class numbers and channels, determines which channels need to be deleted.
     *
     * @param classNumbers The class numbers.
     * @param channels     The current channels.
     * @return A set containing channels which should be deleted from the given list.
     */
    private <C extends Channel> Set<C> determineChannelsToDelete(int[] classNumbers, Collection<C> channels) {
        // Filter down the list of channels.
        return channels.stream().filter(c -> {
            // Ignore channels which do not start with the CS prefix.
            if (!c.getName().toLowerCase().startsWith(Constants.CS_PREFIX.toLowerCase()))
                return false;

            // Ignore channels which do not end in a parsable number.
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(c.getName().substring(Constants.CS_PREFIX.length()));
            } catch (NumberFormatException e) {
                return false;
            }

            // Ignore channels which exist in the array of class numbers.
            for (int classNumber : classNumbers) {
                if (c.getName().equalsIgnoreCase(Constants.CS_PREFIX + classNumber))
                    return false;
            }

            // Lastly, delete the channel.
            return true;
        }).collect(Collectors.toSet());
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
        List<? extends Channel> channels = channelService.getAllChannels(guild, channelType);

        // Check each number to see if a channel for it exists.
        for (int classNumber : classNumbers) {
            // Find any channel that matches the class number, and continue if one is found.
            if (channels.stream().anyMatch(c -> c.getName().equalsIgnoreCase(Constants.CS_PREFIX + classNumber)))
                continue;

            // No channel found; create one.
            String newChannelName = (Constants.CS_PREFIX + classNumber).toLowerCase();
            logService.logInfo(getClass(), "Creating channel: " + newChannelName);
            channelService.createChannel(guild, channelType, newChannelName);
        }
    }
}
