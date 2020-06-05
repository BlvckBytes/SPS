package at.sps.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

  /**
   * Transform all native color prefixes (&) to the spigot
   * protocol specific prefixes (§)
   * @param input Input string with native colors
   * @return Output string with protocol colors
   */
  public static String translateColors( String input ) {
    return ChatColor.translateAlternateColorCodes( '&', input );
  }

  /**
   * Generate the stacktrace string from an exception
   * @param e Exception to stringify
   * @return Stacktrace string
   */
  public static String stringifyException( Exception e ) {
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

      return res;
    } catch ( Exception ex ) {
      ex.printStackTrace();
      return "ERROR";
    }
  }

  /**
   * Concat an array of arguments for message parameters
   * @param args Array of args
   * @param offset Offset to begin concatenating
   * @return Space delimted string
   */
  public static String concatArgs( String[] args, int offset ) {
    StringBuilder message = new StringBuilder( args[ offset ] );

    for( int i = offset + 1; i < args.length; i++ )
      message.append( " " ).append( args[ i ] );

    return message.toString();
  }

  /**
   * Try to parse a string to an integer
   * @param value the string to parse
   * @return The parsed int or null if an error occurred
   */
  public static Integer tryParseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch ( Exception ex ) {
      return null;
    }
  }
}
