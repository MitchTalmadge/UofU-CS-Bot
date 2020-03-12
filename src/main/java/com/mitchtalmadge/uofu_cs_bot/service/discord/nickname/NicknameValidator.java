package com.mitchtalmadge.uofu_cs_bot.service.discord.nickname;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.api.entities.Member;

@InheritedComponent
public abstract class NicknameValidator {

  public abstract String assignNickname(Member member);
}
