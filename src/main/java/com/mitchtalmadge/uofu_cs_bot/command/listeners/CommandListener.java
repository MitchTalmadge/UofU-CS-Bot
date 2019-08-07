package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;

@InheritedComponent
public abstract class CommandListener {

  /**
   * Called when a command is received.
   *
   * @param command The command that was received.
   * @return What the bot should say in reply. Null for no reply.
   */
  public abstract String onCommand(Command command);
}
