package at.sps.core.utils;

import at.sps.core.conf.Messages;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SLogging {

  private static SLogging inst;

  private SLogging() {
    inst = this;
  }

  /**
   * Log a message with a specific level (which results in a different color)
   * @param message Message to log
   * @param level Level to log at
   */
  public void log( String message, LogLevel level ) {
    Bukkit.getConsoleSender().sendMessage( Messages.PREFIX.getTemplate() + level.getColor() + message );
  }

  /**
   * Directly log an exception's stacktrace, as error level
   * @param e Exception to log
   */
  public void log( Exception e ) {
    try {
      // Create print writer on string writer
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );

      // Write exception and get it as string
      e.printStackTrace( pw );
      String res = sw.toString();

      // Close resources
      sw.close();
      pw.close();

      log( res, LogLevel.ERROR );
    } catch ( Exception ex ) {
      log( "Error while stringifying exception!", LogLevel.ERROR );
      ex.printStackTrace();
    }
  }

  /**
   * Singleton instance getter for the logger
   */
  public static SLogging getInst() {
    if( inst == null )
      return new SLogging();

    return inst;
  }
}
