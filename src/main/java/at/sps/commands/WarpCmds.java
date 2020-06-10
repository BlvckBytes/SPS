package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.mappers.WarpMapper;
import at.sps.core.orm.models.Warp;
import at.sps.core.shortcmds.ShortCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class WarpCmds {

  /**
   * Command: warp
   * Usage: /warp name
   * Used to teleport to an existing warp
   */
  @ShortCommand( command = "warp", terminalDeny = true )
  private void onWarp( Player sender, String[] args ) {
    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /warp <Name>" );
      return;
    }

    // Fetch home from DB
    Warp target = WarpMapper.getInst().getByName( args[ 0 ] );

    // Home non existent
    if( target == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7existiert nicht!" );
      return;
    }

    // Teleport to his home
    sender.teleport( target.getLocation() );
    sender.sendMessage( GlobalConstants.PREFIX + "§7Du wurdest erfolgreich zum warp §d" + target.getName() + " §7teleportiert!" );
  }

  /**
   * Command: setwarp
   * Usage: /setwarp name
   * Used to set a warp with a name at the current location
   */
  @ShortCommand( command = "setwarp", terminalDeny = true )
  private void onSetwarp( Player sender, String[] args ) {
    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /setwarp <Name>" );
      return;
    }

    Warp added = new Warp( args[ 0 ], sender.getLocation(), System.currentTimeMillis(), sender.getUniqueId() );
    ActionResult result = WarpMapper.getInst().addWarp( added );

    switch ( result ) {

      // Home added
      case OK:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + added.getName() + " §7wurde erfolgreich erstellt!" );
        break;

      // Name is already existent
      case ALREADY_EXISTENT:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + added.getName() + " §7existiert bereits!" );
        break;

      // Error
      default:
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.INTERNAL_ERROR + " (" + result + ")" );
        break;
    }
  }

  /**
   * Command: delwarp
   * Usage: /delwarp name
   * Used to delete a previously set warp
   */
  @ShortCommand( command = "delwarp", terminalDeny = true )
  private void onDelwarp( Player sender, String[] args ) {
    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /delwarp <Name>" );
      return;
    }

    // Try to delete the existing home from the database
    ActionResult result = WarpMapper.getInst().removeWarp( args[ 0 ] );

    switch ( result ) {

      // Home deleted
      case OK:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7wurde erfolgreich gelöscht!" );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7existiert nicht!" );
        break;

      // Error
      default:
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.INTERNAL_ERROR + " (" + result + ")" );
        break;
    }
  }

  /**
   * Command: updatewarp
   * Usage: /updatewarp name
   * Used to re-set the location of an existing warp to the current location
   */
  @ShortCommand( command = "updatewarp", terminalDeny = true )
  private void onUpdatewarp( Player sender, String[] args ) {
    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /updatewarp <Name>" );
      return;
    }

    // Try to fetch the existing warp
    Warp target = WarpMapper.getInst().getByName( args[ 0 ] );

    // Warp non existent
    if( target == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7existiert nicht!" );
      return;
    }

    // Set the new player location and update in DB
    target.setLocation( sender.getLocation() );
    ActionResult result = WarpMapper.getInst().updateWarp( target );

    switch ( result ) {

      // Home deleted
      case OK:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7wurde erfolgreich geändert!" );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( GlobalConstants.PREFIX + "§7Der warp §d" + args[ 0 ] + " §7existiert nicht!" );
        break;

      // Error
      default:
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.INTERNAL_ERROR + " (" + result + ")" );
        break;
    }
  }

  /**
   * Command: warps
   * Usage: /warps [searchterm]
   * Used to list all existing warps
   */
  @ShortCommand( command = "warps", terminalDeny = true )
  private void onWarps( Player sender, String[] args ) {

    // Fetch all existing warps from the database
    String searchterm = args.length == 0 ? "" : args[ 0 ];
    List< Warp > warps = WarpMapper.getInst().listWarps( searchterm );

    // Build message
    StringBuilder sb = new StringBuilder( "§7Verfügbare Warps: " );

    // No warps found
    if( warps.size() == 0 ) {
      sender.sendMessage( GlobalConstants.PREFIX + sb.toString() + "§cKeine Warps gesetzt!" );
      return;
    }

    // Build home list
    sb.append( "§d" ).append( warps.get( 0 ).getName() );
    for( int i = 1; i < warps.size(); i++ )
      sb.append( "§7, §d" ).append( warps.get( i ).getName() );

    // Send list of available homes to the player
    sender.sendMessage( GlobalConstants.PREFIX + sb.toString() );
  }
}
