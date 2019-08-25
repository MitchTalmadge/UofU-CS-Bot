package com.mitchtalmadge.uofu_cs_bot.command;

import com.mitchtalmadge.uofu_cs_bot.command.listeners.CommandListener;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import net.dv8tion.jda.core.entities.PrivateChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.Set;

@Service
public class CommandDistributor {

  /**
   * The Empty Command Pattern is used in the matcher as a "default" in the case that no command
   * listener can be found.
   */
  private static final CommandPattern EMPTY_COMMAND_PATTERN =
      new CommandPattern() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return CommandPattern.class;
        }

        @Override
        public String[] value() {
          return new String[0];
        }

        @Override
        public boolean strict() {
          return true;
        }
      };

  /** All Command Distribution Listeners. */
  private final Set<CommandListener> commandListeners;

  private LogService logService;

  @Autowired
  public CommandDistributor(Set<CommandListener> commandListeners, LogService logService) {
    this.commandListeners = commandListeners;
    this.logService = logService;
  }

  /**
   * Distributes the given command to the proper Command Listener.
   *
   * @param command The command to distribute.
   */
  public void onCommand(Command command) {

    // Try to find the most specific command listener.
    CommandPatternComparator commandPatternComparator = new CommandPatternComparator(command);
    CommandListener mostSpecificListener = null;
    for (CommandListener listener : commandListeners) {

      // Extract CommandPattern annotation from listener class.
      CommandPattern commandPattern = listener.getClass().getAnnotation(CommandPattern.class);
      if (commandPattern == null) {
        this.logService.logError(
            getClass(),
            "Command Distribution Listener '"
                + listener.getClass().getSimpleName()
                + "' is missing a Command Pattern.");
        return;
      }

      // Compare the extracted pattern to the most specific listener, or the empty pattern if no
      // listener matches yet.
      if (mostSpecificListener != null) {
        // Check if this listener is more specific than the already "most specific" listener.
        if (commandPatternComparator.compare(
                mostSpecificListener.getClass().getAnnotation(CommandPattern.class), commandPattern)
            > 0) mostSpecificListener = listener;
      } else {
        // Check if this listener is more specific than an empty pattern.
        if (commandPatternComparator.compare(EMPTY_COMMAND_PATTERN, commandPattern) > 0)
          mostSpecificListener = listener;
      }
    }

    if (mostSpecificListener == null) {
      sendMessageToSource(command, "I didn't understand your command.");

      // Display the result of the help command.
      onCommand(new Command(command.getMessageReceivedEvent(), new String[] {"help"}));
    } else {
      String publicReply = null;
      if (!command.isPrivateChannel()) {
        publicReply = mostSpecificListener.getPublicReply(command);
      }
      String privateReply = mostSpecificListener.getPrivateReply(command, publicReply != null);

      if (publicReply != null) {
        this.sendPublicMessage(command, publicReply);
      }
      if (privateReply != null) {
        this.sendPrivateMessage(command, privateReply);
      }
    }
  }

  /**
   * Sends a message to the same channel that the command came from.
   *
   * @param command The command received.
   * @param message The message to send.
   */
  private void sendMessageToSource(Command command, String message) {
    if (command.isPrivateChannel()) {
      this.sendPrivateMessage(command, message);
    } else {
      this.sendPublicMessage(command, message);
    }
  }

  /**
   * Sends a message to the public channel that the command was received in. If the command was
   * received in a private channel, this function will do nothing.
   *
   * @param command The command received.
   * @param message The message to send.
   */
  private void sendPublicMessage(Command command, String message) {
    if (command.getMessageReceivedEvent().getPrivateChannel() != null) {
      // Channel is not public.
      return;
    }

    command.getMessageReceivedEvent().getChannel().sendMessage(message).queue();
  }

  /**
   * Sends a message to the sender of the command in a private DM channel.
   *
   * @param command The command received.
   * @param message The message to send to the sender.
   */
  private void sendPrivateMessage(Command command, String message) {

    // If the command was sent from a private channel, use the same channel.
    PrivateChannel privateChannel = command.getMessageReceivedEvent().getPrivateChannel();
    if (privateChannel != null) {
      privateChannel.sendMessage(message).queue();
    } else {
      // Otherwise, open a new private channel.
      command
          .getMessageReceivedEvent()
          .getMember()
          .getUser()
          .openPrivateChannel()
          .queue(
              channel ->
                  channel
                      .sendMessage(message)
                      .queue(
                          success -> {},
                          error ->
                              command
                                  .getMessageReceivedEvent()
                                  .getChannel()
                                  .sendMessage(
                                      command.getMember().getAsMention()
                                          + " I could not send you a private message. Please enable direct messaging for this server.")
                                  .queue()));
    }
  }
}
