package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;

@CommandPattern(value = {"verify"})
public class VerificationCommandListener extends CommandListener {

    private static final String EXAMPLE = "**Example:** !verify u1234567@umail.utah.edu";

    private static final String FORMAT_INSTRUCTIONS = "Please use the uNID version of your u-mail:" +
            "\n:white_check_mark: u#######@umail.utah.edu" +
            "\n:white_check_mark: u#######@utah.edu" +
            "\n**Don't use aliases**: " +
            "\n:octagonal_sign: jane.doe@umail.utah.edu";

    @Override
    public String onCommand(Command command) {
        if (command.getArgs().length < 2) {
            return "To verify your account, I need to know your u-mail address!" +
                    "\n" + EXAMPLE +
                    "\n\n" + FORMAT_INSTRUCTIONS;
        } else if (command.getArgs().length > 2) {
            return "Please only supply one u-mail address!" +
                    "\n" + EXAMPLE +
                    "\n\n" + FORMAT_INSTRUCTIONS;
        }

        String email = command.getArgs()[1].toLowerCase();
        if (!email.endsWith("@umail.utah.edu") && !email.endsWith("@utah.edu")) {
            return "Please make sure your u-mail ends with '@umail.utah.edu' or '@utah.edu'. " +
                    "\n" + EXAMPLE;
        }

        if (!email.startsWith("u")) {
            return FORMAT_INSTRUCTIONS;
        }

        String unid = email.substring(1, email.indexOf('@'));
        if (unid.length() != 7 || !unid.matches("^\\d+$")) {
            return "The uNID in your u-mail should be a 'u' followed by 7 digits. " +
                    "\n" + EXAMPLE;
        }

        //TODO: verification email, check if already sent out or assigned, etc.
        return "WIP";
    }
}
