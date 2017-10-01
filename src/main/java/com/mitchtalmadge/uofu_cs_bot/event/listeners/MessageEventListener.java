package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.CommandDistributor;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageEventListener extends EventListenerAbstract<MessageReceivedEvent> {

    private static final String COMMAND_PREFIX = ",";

    private final CommandDistributor commandDistributor;

    @Autowired
    public MessageEventListener(CommandDistributor commandDistributor) {
        this.commandDistributor = commandDistributor;
    }

    @Override
    public void onEvent(MessageReceivedEvent event) {

    }

}
