package com.mitchtalmadge.uofu_cs_bot.event;

import com.mitchtalmadge.uofu_cs_bot.event.listeners.AnyEventListenerAbstract;
import com.mitchtalmadge.uofu_cs_bot.event.listeners.EventListenerAbstract;
import net.dv8tion.jda.core.events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class EventDistributor {

    /**
     * Maps Event Listeners to their parametrized Event types.
     */
    private final Map<Class<? extends Event>, EventListenerAbstract> eventListenerMap = new HashMap<>();

    /**
     * Should receive all events.
     */
    private final Set<AnyEventListenerAbstract> anyEventListeners;

    @Autowired
    public EventDistributor(Set<EventListenerAbstract> eventListeners, Set<AnyEventListenerAbstract> anyEventListeners) {
        this.anyEventListeners = anyEventListeners;

        // Get the generic types and map them to the listeners.
        eventListeners.forEach(eventListener -> {
            //noinspection unchecked
            eventListenerMap.put(
                    (Class<? extends Event>) GenericTypeResolver.resolveTypeArgument(eventListener.getClass(), EventListenerAbstract.class),
                    eventListener
            );
        });
    }

    /**
     * Called when a Discord event takes place.
     * @param event The event that took place.
     */
    public void onEvent(Event event) {
        // Send event to the listeners who accept all events.
        anyEventListeners.forEach(listener -> listener.onEvent(event));

        // Check for a specific listener for the event.
        eventListenerMap.forEach((aClass, listener) -> {
            if(aClass.isAssignableFrom(event.getClass()))
                //noinspection unchecked
                listener.onEvent(event);
        });
    }

}
