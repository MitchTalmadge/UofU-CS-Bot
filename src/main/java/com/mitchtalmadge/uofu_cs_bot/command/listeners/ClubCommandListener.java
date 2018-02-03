package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;

@CommandPattern(value = {"club"})
public class ClubCommandListener extends CommandListener {

    @Override
    public String onCommand(Command command) {

        // Ensure we have at least three arguments.
        if (command.getArgs().length < 3) {
            return "Missing arguments.\nSyntax: `!club <join|leave> <club name>`\nExample: `!club join acm`";
        }

        // Extract action and club name from arguments.
        String action = command.getArgs()[1];
        String clubName = command.getArgs()[2];

        // Switch on action.
        switch(action.toLowerCase()) {
            case "join": // Join a club
                return "Joining club " + clubName;
            case "leave": // Leave a club
                return "Leaving club " + clubName;
            default: // Invalid action.
                return "Invalid Argument: '" + action + "'\nSyntax: `!club <join|leave> <club name>`\nExample: `!club join acm`";
        }
    }
}
