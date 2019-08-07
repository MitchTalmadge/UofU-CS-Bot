package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.course.CourseService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.nickname.NicknameService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class NicknameEventListener extends EventListenerAbstract<GuildMemberNickChangeEvent> {

  private final LogService logService;
  private CourseService courseService;
  private NicknameService nicknameService;
  private RoleAssignmentService roleAssignmentService;

  @Autowired
  public NicknameEventListener(
      LogService logService,
      CourseService courseService,
      NicknameService nicknameService,
      RoleAssignmentService roleAssignmentService) {
    this.logService = logService;
    this.courseService = courseService;
    this.nicknameService = nicknameService;
    this.roleAssignmentService = roleAssignmentService;
  }

  @Override
  public void onEvent(GuildMemberNickChangeEvent event) {
    logService.logInfo(
        getClass(),
        "Nickname changed for "
            + event.getMember().getUser().getName()
            + ". Old: "
            + event.getPrevNick()
            + " - New: "
            + event.getNewNick());

    nicknameService.validateNickname(event.getMember());
    courseService.computeEnabledCourses();
    roleAssignmentService.assignRoles(event.getMember());
  }
}
