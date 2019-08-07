package com.mitchtalmadge.uofu_cs_bot.service.discord;

import org.springframework.stereotype.Service;

/** Accepts requests for server synchronization and relays them to the synchronizer when needed. */
@Service
public class DiscordSynchronizationRequestSurrogate {

  /**
   * Whenever synchronization is requested, this field will be set to true. <br>
   * On the next scheduled synchronization, this field is checked to determine if synchronization
   * should take place. <br>
   * Once finished, this field is set to false again.
   */
  private boolean synchronizationRequested = false;

  /** Requests that the server be synchronized when convenient. */
  public void requestSynchronization() {
    synchronizationRequested = true;
  }

  /** Clears any request to synchronize the server. */
  public void clearSynchronizationRequests() {
    this.synchronizationRequested = false;
  }

  /**
   * Determines if synchronization has been requested.
   *
   * @return True if synchronization is requested.
   */
  public boolean isSynchronizationRequested() {
    return synchronizationRequested;
  }
}
