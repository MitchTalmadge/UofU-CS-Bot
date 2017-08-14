package com.mitchtalmadge.uofu_cs_bot;

import com.mitchtalmadge.uofu_cs_bot.service.CSRoleService;
import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.LogService;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling
@EnableAsync
@ComponentScan(includeFilters = @ComponentScan.Filter(InheritedComponent.class))
@SpringBootApplication
public class UofUCSBot {

    public static void main(String... args) {

        try {
            SpringApplication.run(UofUCSBot.class, args);
        } catch (BeanCreationException e) {
            if (e.getBeanName().equalsIgnoreCase(DiscordService.class.getSimpleName())) {
                System.err.println("ERROR: DiscordService failed to start. Is DISCORD_TOKEN defined and correct?");
                System.exit(-1);
            } else {
                e.printStackTrace();
            }
        }
    }

}
