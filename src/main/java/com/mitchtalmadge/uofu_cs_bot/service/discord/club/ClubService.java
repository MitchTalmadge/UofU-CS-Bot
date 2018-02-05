package com.mitchtalmadge.uofu_cs_bot.service.discord.club;

import com.mitchtalmadge.uofu_cs_bot.domain.cs.Club;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keeps track of the enabled CS Clubs from the environment variables.
 */
@Service
public class ClubService {

    /**
     * The environment variable that contains the list of enabled clubs.
     */
    private static final String CS_CLUBS_ENV_VAR = "CLUBS";

    /**
     * Pattern used for splitting the enabled clubs list into individual club names.
     */
    private static final Pattern CLUB_SPLIT_PATTERN = Pattern.compile("(,\\s*)+");

    /**
     * The enabled CS courses for the server.
     */
    private Set<Club> enabledClubs = new HashSet<>();

    @PostConstruct
    private void init() {
        // Get the class number list from the environment variable.
        String clubNameList = System.getenv(CS_CLUBS_ENV_VAR);
        if (clubNameList == null || clubNameList.isEmpty()) {
            throw new IllegalArgumentException("The clubs list environment variable (" + CS_CLUBS_ENV_VAR + ") is missing or empty!");
        }

        // Split the list into individual class numbers.
        String[] clubNames = CLUB_SPLIT_PATTERN.split(clubNameList);

        // Parse each class number.
        for (String clubName : clubNames) {
            enabledClubs.add(new Club(clubName));
        }
    }

    /**
     * @return An unmodifiable set of enabled clubs.
     */
    public Set<Club> getEnabledClubs() {
        return Collections.unmodifiableSet(enabledClubs);
    }

    /**
     * Given a Club name, returns the matching Club instance if one exists.
     *
     * @param clubName The name of the Club. Case-insensitive.
     * @return The Club instance matching the name, or null if one does not exist.
     */
    public Club getClubFromName(String clubName) {

        // Search for Club.
        for (Club club : enabledClubs) {
            // Compare names ignoring case.
            if(club.getName().equalsIgnoreCase(clubName))
                return club;
        }

        // No Club found.
        return null;
    }
}
