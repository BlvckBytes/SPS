package at.sps.core.utils;

import at.sps.core.ConsoleLogger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.ResultSet;

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

  /**
   * Map field values from one object to another one, by their names
   * @param from Data source object
   * @param to Data destination object
   * @param names Names to map, either x if both are the same, or x1;x2 for differing fields
   *              where x1 is the source and x2 is the destination field name. Of course, fields
   *              need to have the same datatype
   */
  public static void mapFields( Object from, Object to, String[] names ) {
    try {
      // Loop all fields
      for( String fName : names ) {
        String[] nameData = fName.split( ";" );

        // Get source and target fields
        Field sourceF = from.getClass().getDeclaredField( nameData[ 0 ] );
        Field destF = from.getClass().getDeclaredField( nameData.length == 1 ? nameData[ 0 ] : nameData[ 1 ] );

        // Make both fields accesible
        destF.setAccessible( true );
        sourceF.setAccessible( true );

        // Mirror value
        destF.set( to, sourceF.get( from ) );
      }
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while trying to map fields automatically!" );
      ConsoleLogger.getInst().logMessage( "&c" + stringifyException( e ) );
    }
  }

  public static Object[] extractFields( Object[] arr, ResultSet data, String[] fields ) {
    return arr;
  }
}
