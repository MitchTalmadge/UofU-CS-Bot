package com.mitchtalmadge.uofu_cs_bot.service.discord.nickname;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.CSNickname;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.util.DiscordUtils;
import net.dv8tion.jda.core.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * This service is for working with user nicknames which are an integral part of the permission system.
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
            Set<NicknameValidator> nicknameValidators
    ) {
        this.logService = logService;
        this.discordService = discordService;
        this.nicknameValidators = nicknameValidators;
    }

    /**
     * Validates nicknames for all members.
     */
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

        logService.logInfo(getClass(), "Validating nickname for member '" + member.getUser().getName() + "'.");

        if (member.getNickname() == null)
            return;

        for (NicknameValidator nicknameValidator : nicknameValidators) {
            String nickname = nicknameValidator.assignNickname(member);

            // Submit change.
            if (!member.getNickname().equals(nickname)) {
                logService.logInfo(getClass(), "Adjusted nickname from '" + member.getNickname() + "' to '" + nickname + "'.");
                this.discordService.getGuild().getController().setNickname(member, nickname).complete();
            }
        }
    }

    /**
     * Clears the nicknames (leaves the name but removes course numbers) of all members.
     */
    public void clearNicknames() {
        this.discordService.getGuild().getMembers().forEach(this::clearNickname);
    }

    /**
     * Clears the nickname (leaves the name but removes course numbers) of one member.
     *
     * @param member The member.
     */
    public void clearNickname(Member member) {
        if (DiscordUtils.hasEqualOrHigherRole(discordService.getGuild().getSelfMember(), member))
            return;

        logService.logInfo(getClass(), "Clearing nickname for member '" + member.getUser().getName() + "'.");

        if (member.getNickname() == null)
            return;

        // Format nickname with an empty class group, i.e. "John Doe []".
        String nickname = CSNickname.EMPTY.updateNicknameClassGroup(member.getNickname());

        // Submit change.
        if (!member.getNickname().equals(nickname)) {
            logService.logInfo(getClass(), "Adjusted nickname from '" + member.getNickname() + "' to '" + nickname + "'.");
            this.discordService.getGuild().getController().setNickname(member, nickname).complete();
        }
    }

}
