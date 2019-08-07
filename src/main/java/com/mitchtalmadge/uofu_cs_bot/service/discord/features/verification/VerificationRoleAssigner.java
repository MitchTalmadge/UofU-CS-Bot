package com.mitchtalmadge.uofu_cs_bot.service.discord.features.verification;

import com.mitchtalmadge.uofu_cs_bot.service.discord.role.RoleAssigner;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.Set;

public class VerificationRoleAssigner extends RoleAssigner {

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
    for (Role role : member.getRoles()) {
      if (role.getName().startsWith("cs-")) {
        shouldBeVerified = true;
        break;
      }
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
