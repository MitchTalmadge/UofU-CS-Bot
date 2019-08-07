package com.mitchtalmadge.uofu_cs_bot.event.listeners;

import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;
import net.dv8tion.jda.core.events.Event;

@InheritedComponent
public abstract class EventListenerAbstract<E extends Event> {

  public abstract void onEvent(E event);
}
