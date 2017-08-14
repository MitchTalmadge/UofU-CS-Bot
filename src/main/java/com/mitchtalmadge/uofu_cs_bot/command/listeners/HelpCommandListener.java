package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;

@CommandPattern({"help"})
public class HelpCommandListener extends CommandListener {

    @Override
    public String onCommand(Command command) {

        String helpContent = null;

        if (command.getArgs().length > 1) {
            //Sub help topics
        }

        //TODO: Help message command
        String message = "this is the help menu";

        return message;
    }

}
