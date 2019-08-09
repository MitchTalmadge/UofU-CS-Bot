package com.mitchtalmadge.uofu_cs_bot.domain.entity;

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

  public InternalUser(Long discordUserId, String unid) {
    this.discordUserId = discordUserId;
    this.unid = unid;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InternalUser that = (InternalUser) o;
    return id.equals(that.id)
        && Objects.equals(discordUserId, that.discordUserId)
        && Objects.equals(unid, that.unid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, discordUserId, unid);
  }
}
