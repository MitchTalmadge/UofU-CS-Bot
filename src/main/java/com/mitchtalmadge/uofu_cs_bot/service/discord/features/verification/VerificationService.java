package com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification;

import com.mitchtalmadge.uofu_cs_bot.domain.entity.InternalUser;
import com.mitchtalmadge.uofu_cs_bot.domain.entity.repository.InternalUserRepository;
import com.mitchtalmadge.uofu_cs_bot.service.EmailService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssignmentService;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerificationService {

  private DiscordService discordService;
  private InternalUserRepository internalUserRepository;
  private RoleAssignmentService roleAssignmentService;
  private EmailService emailService;
  private LogService logService;

  private static final List<String> BLACKLISTED_UNIDS =
          Arrays.asList(System.getenv("BLACKLISTED_UNIDS").toLowerCase().split(",?\\s+"));

  @Autowired
  public VerificationService(
      DiscordService discordService,
      InternalUserRepository internalUserRepository,
      RoleAssignmentService roleAssignmentService,
      EmailService emailService,
      LogService logService) {
    this.discordService = discordService;
    this.internalUserRepository = internalUserRepository;
    this.roleAssignmentService = roleAssignmentService;
    this.emailService = emailService;
    this.logService = logService;
  }

  /**
   * Determines the current verification status of a guild member.
   *
   * @param member The member.
   * @return The current status.
   */
  public VerificationStatus getVerificationStatus(Member member) {
    InternalUser iUser =
        this.internalUserRepository
            .findDistinctByDiscordUserId(member.getUser().getIdLong())
            .orElse(null);
    if (iUser == null) {
      return VerificationStatus.UNVERIFIED;
    }

    return iUser.getVerificationStatus();
  }

  /**
   * Finds all members by unid of a certain status.
   *
   * @param unid The uNID needle.
   * @return A map of Discord user IDs to Member instances (null if not a member).
   */
  private Map<Long, Member> getVerifiedMembersByUnidAndStatus(
      String unid, VerificationStatus status) {
    Iterable<InternalUser> iUsers = this.internalUserRepository.findAllByUnid(unid);
    Map<Long, Member> members = new HashMap<>();
    iUsers.forEach(
        iUser -> {
          if (iUser.getVerificationStatus().equals(status)) {
            var discordId = iUser.getDiscordUserId();
            var member = this.discordService.getGuild().getMemberById(discordId);
            members.put(discordId, member);
          }
        });

    return members;
  }

  /**
   * Finds all members by unid who are verified.
   *
   * @param unid The uNID needle.
   * @return A map of Discord user IDs to Member instances (null if not a member).
   */
  public Map<Long, Member> getVerifiedMembersByUnid(String unid) {
    return getVerifiedMembersByUnidAndStatus(unid, VerificationStatus.VERIFIED);
  }

  /**
   * Finds all members by unid who are pending verification.
   *
   * @param unid The uNID needle.
   * @return A map of Discord user IDs to Member instances (null if not a member).
   */
  public Map<Long, Member> getPendingVerifiedMembersByUnid(String unid) {
    return getVerifiedMembersByUnidAndStatus(unid, VerificationStatus.CODE_SENT);
  }

  /**
   * Gets the uNID and verification status of a member if they exist.
   *
   * @param member The member.
   * @return The member's uNID (or null if they are not verified) and verification status.
   */
  public Pair<String, VerificationStatus> getUnidAndStatusByMember(Member member) {
    var iUser =
        this.internalUserRepository.findDistinctByDiscordUserId(member.getUser().getIdLong());
    return iUser
        .map(internalUser -> Pair.of(internalUser.getUnid(), internalUser.getVerificationStatus()))
        .orElse(Pair.of(null, VerificationStatus.UNVERIFIED));
  }

  /**
   * Completes verification by comparing the provided code to the real code, and marking the guild
   * member as verified is they are equal.
   *
   * @param member The member of the guild being verified.
   * @param verificationCode The user provided verification code.
   * @throws VerificationCodeInvalidException If the code is invalid or the member has not started
   *     verification.
   */
  public void completeVerification(Member member, String verificationCode)
      throws VerificationCodeInvalidException {
    this.logService.logInfo(
        getClass(),
        "Completing Verification for Member "
            + member.getEffectiveName()
            + " w/ supplied code "
            + verificationCode);
    InternalUser iUser =
        this.internalUserRepository
            .findDistinctByDiscordUserId(member.getUser().getIdLong())
            .orElse(null);
    if (iUser == null) {
      this.logService.logInfo(getClass(), "Verification was not started.");
      throw new VerificationCodeInvalidException();
    }

    if (!verificationCode.equals(iUser.getVerificationCode())) {
      this.logService.logInfo(
          getClass(),
          "Verification code did not match actual code: " + iUser.getVerificationCode());
      throw new VerificationCodeInvalidException();
    }

    iUser.markVerified();
    this.internalUserRepository.save(iUser);
    this.roleAssignmentService.assignRoles(member);
    this.logService.logInfo(getClass(), "Verification successful.");
  }

  /**
   * As long as the member has not already started verification, generates a verification code for
   * the member and emails it to their u-mail address.
   *
   * @param member The member of the guild.
   * @param unid The uNID of the member.
   * @throws VerificationBeginException If the member has already started verification, or the email
   *     fails to send.
   */
  public void beginVerification(Member member, String unid) throws VerificationBeginException {
    this.logService.logInfo(
        getClass(),
        "Beginning verification for Member " + member.getEffectiveName() + " w/ uNID " + unid);

    if(BLACKLISTED_UNIDS.contains(unid)) {
      throw new VerificationBeginException("uNID " + unid + " is blacklisted.");
    }

    InternalUser iUser =
        this.internalUserRepository
            .findDistinctByDiscordUserId(member.getUser().getIdLong())
            .orElse(null);
    if (iUser != null) {
      if (iUser.getVerificationStatus().equals(VerificationStatus.VERIFIED)) {
        this.logService.logInfo(getClass(), "Already verified.");
        throw new VerificationBeginException(
            "Member " + member.getEffectiveName() + " is already verified.");
      } else {
        this.logService.logInfo(getClass(), "Already started verification.");
        throw new VerificationBeginException(
            "Member " + member.getEffectiveName() + " has already started verification.");
      }
    }

    iUser = new InternalUser(member.getUser().getIdLong(), unid, this.generateVerificationCode());
    iUser = this.internalUserRepository.save(iUser);
    this.logService.logDebug(
        getClass(), "InternalUser created for verification. Code: " + iUser.getVerificationCode());

    try {
      String verifyUrl =
          UriComponentsBuilder.fromHttpUrl(System.getenv("EXTERNAL_URL_ROOT"))
              .path("/verify")
              .queryParam("memberId", member.getUser().getIdLong())
              .queryParam("code", iUser.getVerificationCode())
              .toUriString();
      this.emailService.sendEmail(
          unid + "@umail.utah.edu",
          "<CS @ The U /> Discord Verification",
          "Hi there!\n\n"
              + "If you requested verification for the <CS @ The U /> Discord server, you will find a link below. "
              + "If not, please delete this email and have a great day :)\n\n"
              + verifyUrl);
    } catch (MailException e) {
      this.internalUserRepository.delete(iUser);
      throw new VerificationBeginException("Could not send verification email.", e);
    }
  }

  private String generateVerificationCode() {
    SecureRandom random = new SecureRandom();
    return String.format("%05d", random.nextInt(100_000));
  }

  public static class VerificationCodeInvalidException extends Exception {}

  public static class VerificationBeginException extends Exception {
    VerificationBeginException(String message) {
      super(message);
    }

    VerificationBeginException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
