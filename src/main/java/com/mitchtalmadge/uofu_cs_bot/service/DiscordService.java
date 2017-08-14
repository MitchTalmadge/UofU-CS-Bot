package com.mitchtalmadge.uofu_cs_bot.service;

import com.mitchtalmadge.uofu_cs_bot.event.EventDistributor;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

@Service
public class DiscordService {

    @Value("${DISCORD_TOKEN}")
    private String discordToken;

    /**
     * The JDA (Discord API) instance.
     */
    private JDA jda;

    private final LogService logService;
    private final ConfigurableApplicationContext applicationContext;
    private final EventDistributor eventDistributor;
    private final CSRoleService csRoleService;

    @Autowired
    public DiscordService(LogService logService,
                          ConfigurableApplicationContext applicationContext,
                          EventDistributor eventDistributor,
                          CSRoleService csRoleService) {
        this.logService = logService;
        this.applicationContext = applicationContext;
        this.eventDistributor = eventDistributor;
        this.csRoleService = csRoleService;
    }

    @PostConstruct
    private void init() throws LoginException {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(discordToken)
                    .addEventListener(eventDistributor)
                    .buildBlocking();

            // Update the CS roles of each guild that this bot is connected to. (Which is only one, probably).
            for (Guild guild : jda.getGuilds())
                csRoleService.updateCSRolesForGuild(guild);
        } catch (LoginException e) {
            throw e;
        } catch (InterruptedException e) {
            logService.logException(getClass(), e, "JDA was interrupted while logging in");
        } catch (RateLimitedException e) {
            logService.logException(getClass(), e, "JDA could not login due to rate limiting");
        }
    }

    @PreDestroy
    private void destroy() {
        if (jda != null)
            jda.shutdown();
    }

}
