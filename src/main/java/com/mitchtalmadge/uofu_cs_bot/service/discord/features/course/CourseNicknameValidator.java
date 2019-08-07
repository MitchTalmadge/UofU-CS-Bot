package com.mitchtalmadge.uofu_cs_bot.service.discord.features.course;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.service.discord.nickname.NicknameValidator;
import net.dv8tion.jda.core.entities.Member;

/** Validates the nicknames of members based on their courses. */
public class CourseNicknameValidator extends NicknameValidator {

  @Override
  public String assignNickname(Member member) {
    CSNickname csNickname = new CSNickname(member.getNickname());
    return csNickname.updateNicknameClassGroup(member.getNickname());
  }
}
