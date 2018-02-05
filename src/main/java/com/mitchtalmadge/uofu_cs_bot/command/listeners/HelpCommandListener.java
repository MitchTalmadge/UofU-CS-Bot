package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;

@CommandPattern({"help"})
public class HelpCommandListener extends CommandListener {

    @Override
    public String onCommand(Command command) {

        if (command.getArgs().length > 1) {

            switch (command.getArgs()[1].toLowerCase()) {
                case "club":
                case "clubs":
                    return "Club Commands:\n" +
                            "```\n" +
                            "!club <join|leave> <club name> - Join or leave a club.\n" +
                            "   Example: !club join acm - Join the acm club.\n" +
                            "```";
            }

        }

        return "Valid Commands:\n" +
                "```\n" +
                "!help [subtopic] - Brings up this menu.\n" +
                "!club <join|leave> <club name> - Join or leave a club.\n" +
                "```";
    }

}
