package com.mitchtalmadge.uofu_cs_bot.service;

import com.mitchtalmadge.uofu_cs_bot.event.EventDistributor;
import com.mitchtalmadge.uofu_cs_bot.service.cs.EntitySyncService;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;
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
    private final EntitySyncService entitySyncService;

    @Autowired
    public DiscordService(LogService logService,
                          ConfigurableApplicationContext applicationContext,
                          EventDistributor eventDistributor,
                          EntitySyncService entitySyncService) {
        this.logService = logService;
        this.applicationContext = applicationContext;
        this.eventDistributor = eventDistributor;
        this.entitySyncService = entitySyncService;
    }

    @PostConstruct
    private void init() throws LoginException {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(discordToken)
                    .addEventListener((EventListener) eventDistributor::onEvent)
                    .buildBlocking();

            // Startup procedures.
            for (Guild guild : jda.getGuilds()) {
                // Sync all the class channels, roles, etc.
                entitySyncService.syncEntities(guild);
            }

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

    /**
     * @return The JDA Instance that is being used to connect to Discord.
     */
    public JDA getJDA() {
        return jda;
    }
}
