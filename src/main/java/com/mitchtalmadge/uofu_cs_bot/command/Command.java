package com.mitchtalmadge.uofu_cs_bot.command;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Represents a chat command sent by a user.
 */
public class Command {

  private final MessageReceivedEvent messageReceivedEvent;
  private final String[] args;

  /**
   * Creates a new Command instance from a message received event and an array of arguments.
   *
   * @param messageReceivedEvent The event that triggered this command.
   * @param args                 The arguments of the command. To get this, remove the command prefix and split on
   *                             whitespace.
   */
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

  /**
   * @return The Member that sent this command, determined either by the Guild the command was
   * received in, or the first mutual Guild in the case of a private message.
   */
  public Member getMember() {
    if (messageReceivedEvent.getMember() != null) return messageReceivedEvent.getMember();

    return messageReceivedEvent
            .getAuthor()
            .getMutualGuilds()
            .get(0)
            .getMember(messageReceivedEvent.getAuthor());
  }
}
