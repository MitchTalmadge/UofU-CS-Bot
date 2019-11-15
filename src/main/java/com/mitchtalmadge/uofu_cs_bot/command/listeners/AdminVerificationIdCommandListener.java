package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationService;
import net.dv8tion.jda.core.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds someone's uNID from verification records. */
@CommandPattern(value = {"whois"})
public class AdminVerificationIdCommandListener extends CommandListener {

  private VerificationService verificationService;
  private LogService logService;

  @Autowired
  public AdminVerificationIdCommandListener(
      VerificationService verificationService, LogService logService) {
    this.verificationService = verificationService;
    this.logService = logService;
  }

  @Override
  public String getPublicReply(Command command) {
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
    if (!command.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
      return null;
    }

    if (command.getArgs().length != 2) {
      return "Only supply one argument.";
    }

    String unid = command.getArgs()[1].toLowerCase();
    Matcher unidMatcher = Pattern.compile("^<?(u\\d{7})>?$").matcher(unid);
    if (unidMatcher.matches()) {
      var verified = this.verificationService.getVerifiedMembersByUnid(unidMatcher.group(1));
      var pendingVerified =
          this.verificationService.getPendingVerifiedMembersByUnid(unidMatcher.group(1));

      var result = new StringBuilder("**Matching Records:**").append('\n');
      result.append("- Verified:").append('\n');
      if (verified.size() == 0) {
        result.append("\t(No Results)").append('\n');
      } else {
        verified.forEach(
            (discordId, member) -> {
              result
                  .append("\t- ID: `")
                  .append(discordId)
                  .append("` | Member: ")
                  .append(member != null ? member.getAsMention() : "Not in Server")
                  .append('\n');
            });
      }

      if (pendingVerified.size() == 0) {
        result.append("\t(No Results)").append('\n');
      } else {
        result.append("- Pending Verification:").append('\n');
        pendingVerified.forEach(
            (discordId, member) -> {
              result
                  .append("\t- ID: `")
                  .append(discordId)
                  .append("` | Member: ")
                  .append(member != null ? member.getAsMention() : "Not in Server")
                  .append('\n');
            });
      }

      return result.toString();
    }

    return "No Results";
  }
}
