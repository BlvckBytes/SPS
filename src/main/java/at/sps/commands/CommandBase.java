package at.sps.commands;

import at.sps.core.conf.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandBase {

  /**
   * Standard routine to check if a sender has a certin permission, if that
   * is not the case, an error message will be sent out
   * @param cs Command sender to check on
   * @param permission Permission required for this operation
   * @return False if he has it, true otherwise
   */
  protected boolean lacksPermission( CommandSender cs, String permission ) {
    // Doesn't have the permission, return false and send no perm message
    if( !cs.hasPermission( permission ) ) {
      cs.sendMessage( Messages.NO_PERM.apply( "sps.fly" ) );
      return true;
    }

    // Has permission
    return false;
  }

  /**
   * Standard routine to check whether or not the sender is a player, if
   * that is not the case, an error message will be sent out
   * @param cs Command sender to check on
   * @param command Command for the player only message
   * @return True if is player, false otherwise
   */
  protected Player getPlayerSender( CommandSender cs, String command ) {
    // Sender is no player, return false and send player only message
    if( !( cs instanceof Player ) ) {
      cs.sendMessage( Messages.PLAYER_ONLY.apply( command ) );
      return null;
    }

    // Sender is player, return true
    return ( Player ) cs;
  }

  /**
   * Try to parse a string to an integer
   * @param value The string to parse
   * @return The parsed int or null if an error occurred
   */
  protected Integer tryParseInt( String value ) {
    try {
      return Integer.parseInt( value );
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Find a player that's currently online on the server
   * @param sender CommandSender for errors
   * @param name Name of the target player
   * @return Player if found, null otherwise
   */
  protected Player findPlayer( CommandSender sender, String name ) {
    Player target = Bukkit.getPlayer( name );

    // Player not existing, just return null
    if( target == null || !target.isOnline() ) {
      sender.sendMessage( Messages.PLAYER_NOT_ONLINE.apply( name ) );
      return null;
    }

    // The player is online, thus return
    return target;
  }

  /**
   * Find a player that exists on the server, not necessarily online
   * @param sender CommandSender for errors
   * @param name Name of the target player
   * @return OfflinePlayer if found, null otherwise
   */
  @SuppressWarnings( "deprecation" )
  protected OfflinePlayer findOfflinePlayer( CommandSender sender, String name ) {
    OfflinePlayer target = Bukkit.getOfflinePlayer( name );

    // This player does not exist on the server
    if( target == null ) {
      sender.sendMessage( Messages.PLAYER_NOT_EXISTING.apply( name ) );
      return null;
    }

    // Player exists,
    return target;
  }

  /**
   * Concat an array of arguments for message parameters
   * @param args Array of args
   * @param offset Offset to begin concatenating
   * @return Space delimted string
   */
  protected String concatArgs( String[] args, int offset ) {
    StringBuilder message = new StringBuilder( args[ offset ] );

    for( int i = offset + 1; i < args.length; i++ )
      message.append( " " ).append( args[ i ] );

    return message.toString();
  }
}
