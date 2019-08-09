package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;

@CommandPattern(value = {"verify"})
public class VerificationCommandListener extends CommandListener {

  /** The public channel where the verify command works. */
  private static final String VERIFICATION_CHANNEL = System.getenv("VERIFICATION_CHANNEL");

  private static final String EXAMPLE = "**Example:** !verify u1234567@umail.utah.edu";
  private static final String FORMAT_INSTRUCTIONS =
      "Please use the uNID version of your u-mail:"
          + "\n:white_check_mark: u#######@umail.utah.edu"
          + "\n:white_check_mark: u#######@utah.edu"
          + "\n**Don't use aliases**: "
          + "\n:octagonal_sign: jane.doe@umail.utah.edu";

  @Override
  public String getPublicReply(Command command) {
    if (VERIFICATION_CHANNEL == null
        || !command.getMessageReceivedEvent().getChannel().getName().equals(VERIFICATION_CHANNEL)) {
      return "This command only works in the verification channel or via private message.";
    }

    return getReply(command);
  }

  @Override
  public String getPrivateReply(Command command, boolean publicReplyMade) {
    if (publicReplyMade) {
      return null;
    }

    return getReply(command);
  }

  private String getReply(Command command) {
    if (command.getArgs().length < 2) {
      return "To verify your account, I need to know your u-mail address!\n"
          + (command.isPrivateChannel()
              ? "\n"
              : "You are welcome to send a direct message to this bot if you are concerned about privacy.\n\n")
          + EXAMPLE
          + "\n\n"
          + FORMAT_INSTRUCTIONS;
    }

    if (command.getArgs().length > 2) {
      return "Please only supply one u-mail address!"
          + "\n"
          + EXAMPLE
          + "\n\n"
          + FORMAT_INSTRUCTIONS;
    }

    String email = command.getArgs()[1].toLowerCase();
    if (!email.endsWith("@umail.utah.edu") && !email.endsWith("@utah.edu")) {
      return "Please make sure your u-mail ends with '@umail.utah.edu' or '@utah.edu'. "
          + "\n"
          + EXAMPLE;
    }

    if (!email.startsWith("u")) {
      return FORMAT_INSTRUCTIONS;
    }

    String unid = email.substring(0, email.indexOf('@'));
    if (unid.length() != 8 || !unid.matches("^u\\d{7}$")) {
      return "The uNID in your u-mail should be a 'u' followed by 7 digits. " + "\n" + EXAMPLE;
    }

    // TODO: verification email, check if already sent out or assigned, etc.
    return "WIP";
  }
}
