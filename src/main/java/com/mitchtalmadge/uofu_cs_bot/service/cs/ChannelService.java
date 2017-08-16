package com.mitchtalmadge.uofu_cs_bot.service.cs;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages channels on the Discord server.
 */
@Service
public class ChannelService {

    private final LogService logService;

    @Autowired
    public ChannelService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Retrieves all channels of a given type in a specific guild.
     *
     * @param guild The guild.
     * @return A list of all channels in the guild for the given type.
     * @throws IllegalArgumentException If the channel type is not TEXT or VOICE; the only two supported.
     */
    public List<? extends Channel> getAllChannels(Guild guild, ChannelType channelType) throws IllegalArgumentException {
        switch (channelType) {
            case TEXT:
                return guild.getTextChannels();
            case VOICE:
                return guild.getVoiceChannels();
            default:
                throw new IllegalArgumentException("Cannot get all channels of type: " + channelType.name());
        }
    }

    /**
     * Creates a channel on the provided guild with the given name and type.
     *
     * @param guild       The guild to add the channel to.
     * @param channelType The type of channel to create.
     * @param name        The name of the new channel.
     * @throws IllegalArgumentException If the channel type is not TEXT or VOICE; the only two supported.
     */
    public void createChannel(Guild guild, ChannelType channelType, String name) throws IllegalArgumentException {
        switch (channelType) {
            case TEXT:
                guild.getController().createTextChannel(name).queue();
                break;
            case VOICE:
                guild.getController().createVoiceChannel(name).queue();
                break;
            default:
                throw new IllegalArgumentException("Cannot create channel of type: " + channelType.name());
        }
    }

    /**
     * Deletes a channel of any type.
     *
     * @param channel The channel to delete.
     */
    public void deleteChannel(Channel channel) {
        channel.delete().queue();
    }

}
