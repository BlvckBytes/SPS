package at.sps.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
      return "Error while stringifying an exception!";
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
   * @param value The string to parse
   * @return The parsed int or null if an error occurred
   */
  public static Integer tryParseInt( String value ) {
    try {
      return Integer.parseInt( value );
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Try to parse a string to an integer
   * @param value The string to parse
   * @return The parsed long or null if an error occured
   */
  public static Long tryParseLong( String value ) {
    try {
      return Long.parseLong( value );
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Check whether or not the player can receive the provided itemstack
   * or his inventory has no more space to hold it
   * @param p Player to check the inventory of
   * @param stack Stack wanted to get added
   * @return The amount of items that would remain
   */
  public static int canReceiveItems( Player p, ItemStack stack ) {
    // Loop all items in the inventory, keep a track of the remaining items
    int remainders = stack.getAmount();
    for( ItemStack it : p.getInventory().getContents() ) {
      // Completely emty slot, this can take anything
      if( it == null || it.getType() == Material.AIR )
        return 0;

      // Type mismatch, don't need to check if that stack can be higher
      if( it.getType() != stack.getType() )
        continue;

      // Decrease remaining items by the amount this stack could be increased to
      remainders -= it.getMaxStackSize() - it.getAmount();
    }

    // Return the remainder, constraint against 0, no negative numbers
    return Math.max( remainders, 0 );
  }
}