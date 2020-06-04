package at.sps.core;

import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;

public class ConsoleLogger {

  private static ConsoleLogger inst;

  /**
   * A logger which writes to the spigot console and supports
   * native minecraft color codes (&[a-f]|[1-9])
   */
  private ConsoleLogger() {
    inst = this;
  }

  /**
   * Log a prefixed message to the server console
   * @param message Message to log
   */
  public void logMessage( String message ) {
    // Send a message to the console sender (console instance) with
    // translated color codes for easier use
    Bukkit.getConsoleSender().sendMessage(
      GlobalConstants.PREFIX +
      Utils.translateColors( message )
    );
  }

  /**
   * Singleton instance getter for the globally usable
   * console logger
   * @return ConsoleLogger object instance
   */
  public static ConsoleLogger getInst() {
    if( inst == null )
      return new ConsoleLogger();

    return inst;
  }
}
