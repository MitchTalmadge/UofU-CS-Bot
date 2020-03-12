package com.mitchtalmadge.uofu_cs_bot.web;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VerificationController {

  private DiscordService discordService;
  private final VerificationService verificationService;
  private final LogService logService;

  @Autowired
  public VerificationController(
      DiscordService discordService,
      VerificationService verificationService,
      LogService logService) {
    this.discordService = discordService;
    this.verificationService = verificationService;
    this.logService = logService;
  }

  @GetMapping(name = "/verify")
  public String onVerify(@RequestParam Long memberId, @RequestParam String code, Model model) {

    Member member = this.discordService.getGuild().getMemberById(memberId);
    if (member == null) {
      model.addAttribute("error", "Member ID is invalid.");
      return "error";
    }

    code = code.toLowerCase();
    if (code.length() != 5 || !code.matches("^\\d{5}$")) {
      model.addAttribute("error", "Code is invalid.");
      return "error";
    }

    try {
      this.verificationService.completeVerification(member, code);
    } catch (VerificationService.VerificationCodeInvalidException e) {
      model.addAttribute("error", "Code does not match Member ID");
      return "error";
    }

    List<TextChannel> generalChannels =
        this.discordService.getGuild().getTextChannelsByName("general", true);
    String generalChannelId = null;
    if (generalChannels.size() >= 1) {
      generalChannelId = generalChannels.get(0).getId();
    }

    model.addAttribute("guildId", this.discordService.getGuild().getId());
    model.addAttribute("channelId", generalChannelId);
    return "verification-success";
  }
}
