package com.mitchtalmadge.uofu_cs_bot.command;

import com.mitchtalmadge.uofu_cs_bot.command.listeners.CommandListener;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PrivateChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.Set;

@Service
public class CommandDistributor {

    /**
     * The Empty Command Pattern is used in the matcher as a "default" in the case that no command listener can be found.
     */
    private static final CommandPattern EMPTY_COMMAND_PATTERN = new CommandPattern() {
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

    /**
     * All Command Distribution Listeners.
     */
    private final Set<CommandListener> commandListeners;

    @Autowired
    public CommandDistributor(Set<CommandListener> commandListeners) {
        this.commandListeners = commandListeners;
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
                System.err.println("Command Distribution Listener '" + listener.getClass().getSimpleName() + "' is missing a Command Pattern.");
                return;
            }

            // Compare the extracted pattern to the most specific listener, or the empty pattern if no listener matches yet.
            if (mostSpecificListener != null) {
                // Check if this listener is more specific than the already "most specific" listener.
                if (commandPatternComparator.compare(mostSpecificListener.getClass().getAnnotation(CommandPattern.class), commandPattern) > 0)
                    mostSpecificListener = listener;
            } else {
                // Check if this listener is more specific than an empty pattern.
                if (commandPatternComparator.compare(EMPTY_COMMAND_PATTERN, commandPattern) > 0)
                    mostSpecificListener = listener;
            }
        }

        // Send the command to the proper listener, or send an error message if no listener matched the command.
        if (mostSpecificListener == null) {
            sendPrivateMessage(command, "I didn't understand your command.");
            onCommand(new Command(command.getMessageReceivedEvent(), new String[]{"help"}));
        } else {
            String response = mostSpecificListener.onCommand(command);
            if (response != null) {
                sendPrivateMessage(command, response);
            }
        }
    }

    /**
     * Sends a private message to the sender of the command.
     *
     * @param command The command received.
     * @param message The message to send to the sender.
     */
    private void sendPrivateMessage(Command command, String message) {

        // If the command was sent from a private channel, use the same channel.
        PrivateChannel privateChannel = command.getMessageReceivedEvent().getPrivateChannel();
        if (privateChannel != null)
            privateChannel.sendMessage(message).queue();
        else // Otherwise, open a new private channel.
            command.getMessageReceivedEvent().getMember().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
    }

}
