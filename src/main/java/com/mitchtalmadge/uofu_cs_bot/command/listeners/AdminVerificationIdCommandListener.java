package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

/** Finds someone's uNID from verification records. */
@CommandPattern(value = {"whois"})
public class AdminVerificationIdCommandListener extends CommandListener {

  private DiscordService discordService;
  private VerificationService verificationService;
  private LogService logService;

  @Autowired
  public AdminVerificationIdCommandListener(
      DiscordService discordService,
      VerificationService verificationService,
      LogService logService) {
    this.discordService = discordService;
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

    var input = command.getArgs()[1].toLowerCase();
    var unidMatcher = Pattern.compile("^<?(u\\d{7})>?$").matcher(input);
    if (unidMatcher.matches()) {
      return this.getUnidResults(unidMatcher.group(1));
    }

    var members = command.getMessageReceivedEvent().getMessage().getMentionedMembers();
    if (members.size() == 0) {
      return "No Results";
    }

    return this.getMemberResults(members.get(0));
  }

  private String getUnidResults(String unid) {
    var verified = this.verificationService.getVerifiedMembersByUnid(unid);
    var pendingVerified = this.verificationService.getPendingVerifiedMembersByUnid(unid);

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

    result.append("- Pending Verification:").append('\n');
    if (pendingVerified.size() == 0) {
      result.append("\t(No Results)").append('\n');
    } else {
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

  private String getMemberResults(Member member) {
    var details = this.verificationService.getUnidAndStatusByMember(member);

    var result =
        new StringBuilder("**Details of ")
            .append(member.getAsMention())
            .append(":**")
            .append('\n')
            .append("uNID: `")
            .append(details.getLeft() != null ? details.getLeft() : "Unknown")
            .append("` | Status: ")
            .append(details.getRight().toString());

    return result.toString();
  }
}
