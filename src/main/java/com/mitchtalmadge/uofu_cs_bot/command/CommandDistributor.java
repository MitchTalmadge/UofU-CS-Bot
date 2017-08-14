package com.mitchtalmadge.uofu_cs_bot.command;

import com.mitchtalmadge.uofu_cs_bot.command.listeners.CommandListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.Set;

@Service
public class CommandDistributor {

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
            CommandPattern commandPattern = listener.getClass().getAnnotation(CommandPattern.class);
            if (commandPattern == null) {
                System.err.println("Command Distribution Listener '" + listener.getClass().getSimpleName() + "' is missing a Command Pattern.");
                return;
            }

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
            command.getMessageReceivedEvent().getChannel().sendMessage("I didn't understand your command.").queue();
            onCommand(new Command(command.getMessageReceivedEvent(), new String[]{"help"}));
        } else {
            String response = mostSpecificListener.onCommand(command);
            if (response != null) {
                command.getMessageReceivedEvent().getChannel().sendMessage(response).queue();
            }
        }
    }

}
