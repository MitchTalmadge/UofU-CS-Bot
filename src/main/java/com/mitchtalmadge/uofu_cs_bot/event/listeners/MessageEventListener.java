package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.CommandDistributor;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageEventListener extends EventListener<MessageReceivedEvent> {

    private static final String COMMAND_PREFIX = ",";

    private final CommandDistributor commandDistributor;

    @Autowired
    public MessageEventListener(CommandDistributor commandDistributor) {
        this.commandDistributor = commandDistributor;
    }

    @Override
    public void onEvent(MessageReceivedEvent event) {
        // Ignore messages from ourself.
        if (event.getAuthor().equals(event.getJDA().getSelfUser()))
            return;

        // TODO: Chat commands
        return;

        /*switch (event.getChannelType()) {
            case TEXT:
                // Check for Command Prefix
                if (event.getMessage().getRawContent().startsWith(COMMAND_PREFIX)) {
                    // Split the message into arguments
                    String[] args = event.getMessage().getRawContent().substring(COMMAND_PREFIX.length()).toLowerCase().split("\\s");

                    // Create a command instance
                    Command command = new Command(event, args);
                    commandDistributor.onCommand(command);
                }
        }*/
    }

}
