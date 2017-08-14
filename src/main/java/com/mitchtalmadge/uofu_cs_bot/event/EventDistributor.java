package com.mitchtalmadge.uofu_cs_bot.event;

import com.mitchtalmadge.uofu_cs_bot.event.listeners.EventListener;
import net.dv8tion.jda.core.events.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class EventDistributor implements net.dv8tion.jda.core.hooks.EventListener {

    /**
     * Maps Event Listeners to their generic Event types.
     */
    private final Map<Class<? extends Event>, EventListener> eventListenerMap = new HashMap<>();

    @Autowired
    public EventDistributor(Set<EventListener> eventListeners) {
        // Get the generic types and map them to the listeners.
        eventListeners.forEach(eventListener -> {
            //noinspection unchecked
            eventListenerMap.put(
                    (Class<? extends Event>) ((ParameterizedType) eventListener.getClass().getGenericSuperclass()).getActualTypeArguments()[0],
                    eventListener
            );
        });
    }

    @Override
    public void onEvent(Event event) {

        // Look for a listener for the event.
        if (eventListenerMap.containsKey(event.getClass())) {
            //noinspection unchecked
            eventListenerMap.get(event.getClass()).onEvent(event);
        }

    }

}
