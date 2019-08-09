package com.mitchtalmadge.uofu_cs_bot.command.listeners;

import com.mitchtalmadge.uofu_cs_bot.command.Command;
import com.mitchtalmadge.uofu_cs_bot.util.InheritedComponent;

@InheritedComponent
public abstract class CommandListener {

  /**
   * Determines what should be said publicly when a command is received. This function will not be
   * called if the command was sent in a DM.
   *
   * @param command The command that was received.
   * @return What the bot should say in reply. Null for no reply.
   */
  public abstract String getPublicReply(Command command);

  /**
   * Determines what should be said through DM when a command is received.
   *
   * @param command The command that was received.
   * @param publicReplyMade True if a public reply will be delivered. You may not want to send a
   *     private reply if one has already been sent publicly.
   * @return What the bot should say in reply. Null for no reply.
   */
  public abstract String getPrivateReply(Command command, boolean publicReplyMade);
}
