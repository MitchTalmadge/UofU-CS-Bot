package com.mitchtalmadge.uofu_cs_bot;

import com.mitchtalmadge.uofu_cs_bot.service.DiscordService;
import com.mitchtalmadge.uofu_cs_bot.service.cs.EntitySyncService;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.util.PriorityQueue;
import java.util.Queue;

public class FailureAnalyzer implements org.springframework.boot.diagnostics.FailureAnalyzer {

    @Override
    public FailureAnalysis analyze(Throwable failure) {
        // Check if a bean could not be created.
        if (failure instanceof BeanCreationException) {

            // Find the root of the problem by adding the exception and all causes that are also bean creation exceptions.
            Queue<BeanCreationException> exceptionQueue = new PriorityQueue<>();
            exceptionQueue.add((BeanCreationException) failure);

            BeanCreationException currentException;
            while ((currentException = exceptionQueue.poll()) != null) {
                // Check for known beans.
                if (currentException.getBeanName().equalsIgnoreCase(DiscordService.class.getSimpleName())) {
                    // DiscordService
                    return new FailureAnalysis("DiscordService failed to start: " + currentException.getCause().getMessage(), "Check that the discord token is defined and correct.", currentException.getCause());
                } else if (currentException.getBeanName().equalsIgnoreCase(EntitySyncService.class.getSimpleName())) {
                    // ClassSyncService
                    return new FailureAnalysis("ClassSyncService failed to start: " + currentException.getCause().getMessage(), "Check that the classes list is defined and correct.", currentException.getCause());
                } else {
                    // No bean matched, so check if the cause is also a bean creation exception.
                    if (currentException.getCause() instanceof BeanCreationException)
                        exceptionQueue.add((BeanCreationException) currentException.getCause());
                }
            }
        }

        return null;
    }

}
