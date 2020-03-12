package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.api.events.GenericEvent;

@InheritedComponent
public abstract class EventListenerAbstract<E extends GenericEvent> {

  public abstract void onEvent(E GenericEvent);
}
