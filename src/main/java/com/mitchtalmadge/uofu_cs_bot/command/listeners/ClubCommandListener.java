package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.command.CommandPattern;
import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.club.ClubMembershipService;
import com.mitchtalmadge.uofu_cs_bot.service.discord.features.club.ClubService;
import org.springframework.beans.factory.annotation.Autowired;

@CommandPattern(value = {"club"})
public class ClubCommandListener extends CommandListener {

    private ClubService clubService;
    private ClubMembershipService clubMembershipService;

    @Autowired
    public ClubCommandListener(ClubService clubService, ClubMembershipService clubMembershipService) {
        this.clubService = clubService;
        this.clubMembershipService = clubMembershipService;
    }

    @Override
    public String onCommand(Command command) {

        // Ensure we have at least three arguments.
        if (command.getArgs().length < 3) {
            return "Missing arguments.\nSyntax: `!club <join|leave> <club name>`\nExample: `!club join acm`";
        }

        // Extract action and club name from arguments.
        String action = command.getArgs()[1];
        String clubName = command.getArgs()[2];

        Club club = clubService.getClubFromName(clubName);
        if (club == null) return "The club '" + clubName + "' does not exist!";

        // TODO: !club list

        // Switch on action.
        switch (action.toLowerCase()) {
            case "join": // Join a club
                clubMembershipService.joinClub(command.getMember(), club);
                return "You are now a part of the '" + clubName + "' club!";
            case "leave": // Leave a club
                clubMembershipService.leaveClub(command.getMember(), club);
                return "You are no longer a part of the '" + clubName + "' club.";
            default: // Invalid action.
                return "Invalid Action: '"
                        + action
                        + "'\nSyntax: `!club <join|leave> <club name>`\nExample: `!club join acm`";
        }
    }
}
