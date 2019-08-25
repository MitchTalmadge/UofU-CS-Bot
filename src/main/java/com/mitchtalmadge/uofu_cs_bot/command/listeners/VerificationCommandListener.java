package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationStatus;
import org.springframework.beans.factory.annotation.Autowired;

@CommandPattern(value = {"verify"})
public class VerificationCommandListener extends CommandListener {

  /** The public channel where the verify command works. */
  private static final String VERIFICATION_CHANNEL = System.getenv("VERIFICATION_CHANNEL");

  private static final String EXAMPLE = "**Example:** !verify u1234567";

  private VerificationService verificationService;
  private LogService logService;

  @Autowired
  public VerificationCommandListener(
      VerificationService verificationService, LogService logService) {
    this.verificationService = verificationService;
    this.logService = logService;
  }

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
    VerificationStatus verificationStatus =
        this.verificationService.getVerificationStatus(command.getMember());
    if (verificationStatus.equals(VerificationStatus.VERIFIED)) {
      return "You are already verified! :)";
    }
    if (verificationStatus.equals(VerificationStatus.CODE_SENT)) {
      return "A verification link has already been sent to your u-mail. Please check spam. "
          + "If you can't find it, ask an admin for help!";
    }

    if (command.getArgs().length < 2) {
      return "To verify your account, I need to know your uNID!\n"
          + (command.isPrivateChannel()
              ? "\n"
              : "You are welcome to direct message this bot if you are concerned about privacy.\n\n")
          + EXAMPLE;
    }

    if (command.getArgs().length > 2) {
      return "You supplied too many arguments. In case that was an accident, nothing will happen. "
          + "Please only supply one argument.\n\n"
          + EXAMPLE;
    }

    String unid = command.getArgs()[1].toLowerCase();
    if (unid.length() != 8 || !unid.matches("^u\\d{7}$")) {
      return "The uNID should be a 'u' followed by 7 digits. " + "\n" + EXAMPLE;
    }

    try {
      this.verificationService.beginVerification(command.getMember(), unid);
    } catch (VerificationService.VerificationBeginException e) {
      this.logService.logException(
          getClass(),
          e,
          "Could not begin verification for member " + command.getMember().getEffectiveName());

      return "Uh oh! Something went wrong internally while trying to verify you. Please let an admin know!\n\n"
          + "**Error:** "
          + e.getMessage();
    }

    return "Looks good! **Please check your u-mail for a verification link.** Be sure to look in your spam. "
        + "Thanks for helping to keep the server safe!";
  }
}
