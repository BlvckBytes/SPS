package at.sps.core.utils;

import org.bukkit.ChatColor;

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
}
