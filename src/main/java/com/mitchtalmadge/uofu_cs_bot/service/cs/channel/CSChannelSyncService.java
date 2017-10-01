package com.mitchtalmadge.uofu_cs_bot.service.cs.channel;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSClass;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSConstants;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.CSClassService;
import com.mitchtalmadge.uofu_cs_bot.util.CSNamingConventions;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CSChannelSyncService {

    private final LogService logService;
    private final DiscordService discordService;
    private final CSClassService csClassService;

    @Autowired
    public CSChannelSyncService(LogService logService,
                                DiscordService discordService,
                                CSClassService csClassService) {
        this.logService = logService;
        this.discordService = discordService;
        this.csClassService = csClassService;
    }

    @PostConstruct
    private void init() {
        SyncChannelCategories();

        SyncTextChannels();
        SyncVoiceChannels();
    }

    /**
     * Ensures that all required channel categories exist.
     */
    private void SyncChannelCategories() {
        logService.logInfo(getClass(), "Synchronizing Channel Categories...");

        Guild guild = discordService.getGuild();

        // Check for existence of classes channel category.
        List<Category> classesCategories = guild.getCategoriesByName(CSConstants.CS_CHANNEL_CATEGORY, false);
        if (classesCategories.size() == 0) {
            // Create new category.
            guild.getController().createCategory(CSConstants.CS_CHANNEL_CATEGORY).queue();
        }
    }

    /**
     * Ensures that all required text channels exist for the enabled classes.
     */
    private void SyncTextChannels() {
        logService.logInfo(getClass(), "Synchronizing Channels...");

        Guild guild = discordService.getGuild();
        Set<CSClass> enabledClasses = csClassService.getEnabledClasses();

        // This set starts by containing all enabled classes. Classes are removed one-by-one as their channels are found.
        // The remaining classes which have not been removed must be created as new channels.
        Set<CSClass> missingChannels = new HashSet<>();
        missingChannels.addAll(enabledClasses);

        // Find the existing channels and delete invalid channels.
        guild.getTextChannels().forEach(channel -> {
            try {
                // Parse the channel as a class.
                CSClass channelClass = new CSClass(channel.getName());

                // Remove the class if it exists.
                if (missingChannels.contains(channelClass)) {
                    missingChannels.remove(channelClass);
                } else {
                    // Delete the channel as it should not exist.
                    logService.logInfo(getClass(), "Deleting invalid Text Channel: " + channel.getName());
                    channel.delete().queue();
                }
            } catch (CSClass.InvalidClassNameException ignored) {
                // This channel is not a class channel.
            }
        });

        // Add the missing channels.
        missingChannels.forEach(csClass -> {
            String channelName = CSNamingConventions.toChannelName(csClass);
            logService.logInfo(getClass(), "Creating new Text Channel: " + channelName);
            guild.getController().createTextChannel(channelName).queue();
        });
    }

    /**
     * Ensures that all required voice channels exist for the enabled classes.
     */
    private void SyncVoiceChannels() {
        logService.logInfo(getClass(), "Synchronizing Channels...");

        Guild guild = discordService.getGuild();
        Set<CSClass> enabledClasses = csClassService.getEnabledClasses();

        // This set starts by containing all enabled classes. Classes are removed one-by-one as their channels are found.
        // The remaining classes which have not been removed must be created as new channels.
        Set<CSClass> missingChannels = new HashSet<>();
        missingChannels.addAll(enabledClasses);

        // Find the existing channels and delete invalid channels.
        guild.getVoiceChannels().forEach(channel -> {
            try {
                // Parse the channel as a class.
                CSClass channelClass = new CSClass(channel.getName());

                // Remove the class if it exists.
                if (missingChannels.contains(channelClass)) {
                    missingChannels.remove(channelClass);
                } else {
                    // Delete the channel as it should not exist.
                    logService.logInfo(getClass(), "Deleting invalid Voice Channel: " + channel.getName());
                    channel.delete().queue();
                }
            } catch (CSClass.InvalidClassNameException ignored) {
                // This channel is not a class channel.
            }
        });

        // Add the missing channels.
        missingChannels.forEach(csClass -> {
            String channelName = CSNamingConventions.toChannelName(csClass);
            logService.logInfo(getClass(), "Creating new Voice Channel: " + channelName);
            guild.getController().createVoiceChannel(channelName).queue();
        });
    }

}








