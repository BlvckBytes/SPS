package at.sps.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;

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

  /**
   * Format a remaining time in milliseconds to a human readable time string
   * @param remainder Remaining time in ms
   * @return Time string formatted nicely
   */
  public static String formatRemainder( long remainder ) {
    StringBuilder sb = new StringBuilder();
    Duration dur = Duration.ofMillis( remainder );

    // Get parts of the time
    long seconds = dur.getSeconds() % 60;
    long minutes = dur.toMinutes() % 60;
    long hours = dur.toHours() % 24;
    long days = dur.toDays();

    // Only append parts if their existent (> 0)

    if( days > 0 )
      sb.append( " " ).append( days ).append( "d" );

    if( hours > 0 )
      sb.append( " " ).append( hours ).append( "h" );

    if( minutes > 0 )
      sb.append( " " ).append( minutes ).append( "m" );

    sb.append( " " ).append( seconds ).append( "s" );
    return sb.toString().substring( 1 );
  }
}