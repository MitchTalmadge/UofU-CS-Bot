package com.mitchtalmadge.uofu_cs_bot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Represents a chat command sent by a user.
 */
public class Command {

    private final MessageReceivedEvent messageReceivedEvent;
    private final String[] args;

    public Command(MessageReceivedEvent messageReceivedEvent, String[] args) {
        this.messageReceivedEvent = messageReceivedEvent;
        this.args = args;
    }

    public MessageReceivedEvent getMessageReceivedEvent() {
        return messageReceivedEvent;
    }

    public String[] getArgs() {
        return args;
    }
}
