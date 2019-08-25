package com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification;

import com.mitchtalmadge.uofu_cs_bot.domain.entity.InternalUser;
import com.mitchtalmadge.uofu_cs_bot.domain.entity.repository.InternalUserRepository;
import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssigner;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class VerificationRoleAssigner extends RoleAssigner {

  private InternalUserRepository internalUserRepository;

  @Autowired
  public VerificationRoleAssigner(InternalUserRepository internalUserRepository) {
    this.internalUserRepository = internalUserRepository;
  }

  @Override
  public void updateRoleAssignments(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) {

    // Check if already verified.
    boolean isVerified = false;
    for (Role role : member.getRoles()) {
      if (role.getName().equals(VerificationRoleSynchronizer.VERIFIED_ROLE_NAME)) {
        isVerified = true;
        break;
      }
    }

    // Check if should be verified.
    boolean shouldBeVerified = false;
    InternalUser iUser =
            this.internalUserRepository
                    .findDistinctByDiscordUserId(member.getUser().getIdLong())
                    .orElse(null);
    if(iUser != null && iUser.getVerificationStatus().equals(VerificationStatus.VERIFIED)) {
      shouldBeVerified = true;
    }

    // Update accordingly.
    Role verifiedRole =
        member
            .getGuild()
            .getRolesByName(VerificationRoleSynchronizer.VERIFIED_ROLE_NAME, false)
            .get(0);
    if (!isVerified && shouldBeVerified) {
      rolesToAdd.add(verifiedRole);
    }
  }
}
