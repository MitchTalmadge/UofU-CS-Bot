package com.mitchtalmadge.uofu_cs_bot.service.discord.nickname;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.audit.AuditLogKey;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * This service is for working with user nicknames which are an integral part of the permission
 * system.
 */
@Service
public class NicknameService {

  private final LogService logService;
  private final DiscordService discordService;
  private Set<NicknameValidator> nicknameValidators;

  @Autowired
  public NicknameService(
      LogService logService,
      DiscordService discordService,
      Set<NicknameValidator> nicknameValidators) {
    this.logService = logService;
    this.discordService = discordService;
    this.nicknameValidators = nicknameValidators;
  }

  /** Validates nicknames for all members. */
  public void validateNicknames() {
    this.discordService.getGuild().getMembers().forEach(this::validateNickname);
  }

  /**
   * Validates the nickname of one member.
   *
   * @param member The member.
   */
  public void validateNickname(Member member) {
    if (DiscordUtils.hasEqualOrHigherRole(discordService.getGuild().getSelfMember(), member))
      return;

    logService.logDebug(
        getClass(), "Validating nickname for member " + member.getUser().getName() + ".");

    if (member.getNickname() == null) return;

    for (NicknameValidator nicknameValidator : nicknameValidators) {
      String nickname = nicknameValidator.assignNickname(member);

      // Submit change.
      if (!member.getNickname().equals(nickname)) {
        logService.logInfo(
            getClass(),
            "Adjusted nickname for member "
                + member.getUser().getName()
                + " from '"
                + member.getNickname()
                + "' to '"
                + nickname
                + "'.");
        this.discordService
            .getGuild()
            .getController()
            .setNickname(member, nickname)
            .queue(
                (success) -> {},
                (error) ->
                    logService.logException(
                        getClass(),
                        error,
                        "Could not adjust nickname for member "
                            + member.getUser().getName()
                            + "."));
      }
    }
  }

  /** Clears the nicknames (leaves the name but removes course numbers) of all members. */
  public void clearNicknames() {
    this.discordService.getGuild().getMembers().forEach(this::clearNickname);
  }

  /**
   * Clears the nicknames (leaves the name but removes course numbers) of all members who have not
   * updated their nickname within the last N days.
   *
   * @param days The number of days in which the user must have updated their nickname for it to not
   *     be cleared.
   */
  public void clearNicknamesOlderThanDays(int days) {
    this.logService.logInfo(getClass(), "Clearing nicknames older than " + days + " days.");

    AuditLogPaginationAction updateLogs =
        this.discordService.getGuild().getAuditLogs().type(ActionType.MEMBER_UPDATE);

    this.discordService
        .getGuild()
        .getMembers()
        .forEach(
            (member) -> {
              this.logService.logDebug(
                  getClass(), "Checking nickname age for " + member.getEffectiveName());

              boolean recentlyUpdated =
                  updateLogs.stream()
                      .anyMatch(
                          (log) -> {
                            // Make sure the change was targeted on this user.
                            if (!log.getTargetId().equals(member.getUser().getId())) {
                              return false;
                            }

                            // Make sure the change was a nickname change.
                            if (!log.getChanges().containsKey(AuditLogKey.MEMBER_NICK.getKey())) {
                              return false;
                            }

                            // Check age.
                            return log.getCreationTime()
                                .isAfter(OffsetDateTime.now().minusDays(days));
                          });

              if (!recentlyUpdated) {
                this.logService.logDebug(
                    getClass(),
                    "Member "
                        + member.getEffectiveName()
                        + " has an old nick and will be cleared.");
                this.clearNickname(member);
              } else {
                this.logService.logDebug(
                    getClass(),
                    "Member "
                        + member.getEffectiveName()
                        + " updated their nick within last "
                        + days
                        + " days.");
              }
            });
  }

  /**
   * Clears the nickname (leaves the name but removes course numbers) of one member.
   *
   * @param member The member.
   */
  public void clearNickname(Member member) {
    if (DiscordUtils.hasEqualOrHigherRole(discordService.getGuild().getSelfMember(), member)) {
      this.logService.logDebug(
          getClass(), "Not clearing nickname of high-role member " + member.getEffectiveName());
      return;
    }

    logService.logInfo(
        getClass(), "Clearing nickname for member '" + member.getUser().getName() + "'.");

    if (member.getNickname() == null) return;

    // Format nickname with an empty class group, i.e. "John Doe []".
    String nickname = CSNickname.EMPTY.updateNicknameClassGroup(member.getNickname());

    // Submit change.
    if (!member.getNickname().equals(nickname)) {
      logService.logInfo(
          getClass(), "Cleared nickname for member " + member.getUser().getName() + ".");
      this.discordService
          .getGuild()
          .getController()
          .setNickname(member, nickname)
          .queue(
              (success) -> {},
              (error) ->
                  logService.logException(
                      getClass(),
                      error,
                      "Could not clear nickname for member " + member.getUser().getName() + "."));
    }
  }
}
