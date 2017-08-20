package com.mitchtalmadge.uofu_cs_bot;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling
@EnableAsync
@ComponentScan(includeFilters = @ComponentScan.Filter(InheritedComponent.class))
@SpringBootApplication
public class UofUCSBot {

    public static void main(String... args) {
        SpringApplication.run(UofUCSBot.class, args);
    }

}
