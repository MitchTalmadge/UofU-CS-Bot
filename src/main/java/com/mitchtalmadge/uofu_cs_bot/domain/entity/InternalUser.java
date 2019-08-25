package com.mitchtalmadge.uofu_cs_bot.domain.entity;

import com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification.VerificationStatus;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "internal_users")
public class InternalUser {

  @Id @GeneratedValue private Long id;

  @Column(name = "discord_user_id", nullable = false)
  private Long discordUserId;

  @Column(name = "unid")
  private String unid;

  @Column(name = "verification_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private VerificationStatus verificationStatus;

  @Column(name = "verification_code")
  private String verificationCode;

  private InternalUser() {}

  public InternalUser(Long discordUserId, String unid, String verificationCode) {
    this.discordUserId = discordUserId;
    this.unid = unid;
    this.verificationStatus = VerificationStatus.CODE_SENT;
    this.verificationCode = verificationCode;
  }

  public Long getId() {
    return id;
  }

  public Long getDiscordUserId() {
    return discordUserId;
  }

  public String getUnid() {
    return unid;
  }

  public VerificationStatus getVerificationStatus() {
    return verificationStatus;
  }

  public String getVerificationCode() {
    return verificationCode;
  }

  public void markVerified() {
    this.verificationStatus = VerificationStatus.VERIFIED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InternalUser that = (InternalUser) o;
    return Objects.equals(id, that.id)
        && Objects.equals(discordUserId, that.discordUserId)
        && Objects.equals(unid, that.unid)
        && verificationStatus == that.verificationStatus
        && Objects.equals(verificationCode, that.verificationCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, discordUserId, unid, verificationStatus, verificationCode);
  }
}
