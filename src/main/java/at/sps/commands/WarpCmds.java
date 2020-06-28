package at.sps.commands;

import at.sps.core.conf.Messages;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.mappers.WarpMapper;
import at.sps.core.orm.models.Warp;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.gui.ComplexMessage;
import at.sps.core.gui.ComplexPart;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WarpCmds {

  private final SimpleDateFormat warpDateFormat;

  public WarpCmds() {
    warpDateFormat = new SimpleDateFormat( "dd.MM.yyyy" );
  }

  /**
   * Command: warp
   * Usage: /warp name
   * Used to teleport to an existing warp
   */
  @ShortCommand( command = "warp", terminalDeny = true )
  private void onWarp( Player sender, String[] args ) {
    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( Messages.USAGE.apply( "/warp <Name>" ) );
      return;
    }

    // Fetch home from DB
    Warp target = WarpMapper.getInst().getByName( args[ 0 ] );

    // Home non existent
    if( target == null ) {
      sender.sendMessage( Messages.WARP_NON_EXISTING.apply( args[ 0 ] ) );
      return;
    }

    // Teleport to his home
    sender.teleport( target.getLocation() );
    sender.sendMessage( Messages.WARP_TELEPORT.apply( target.getName() ) );
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
      sender.sendMessage( Messages.USAGE.apply( "/setwarp <Name>" ) );
      return;
    }

    Warp added = new Warp( args[ 0 ], sender.getLocation(), System.currentTimeMillis(), sender.getUniqueId() );
    ActionResult result = WarpMapper.getInst().addWarp( added );

    switch ( result ) {
      // Warp added
      case OK:
        sender.sendMessage( Messages.WARP_CREATED.apply( added.getName() ) );
        break;

      // Name is already existent
      case ALREADY_EXISTENT:
        sender.sendMessage( Messages.WARP_EXISTING.apply( added.getName() ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "WARPCR->" + result ) );
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
      sender.sendMessage( Messages.USAGE.apply( "/delwarp <Name>" ) );
      return;
    }

    // Try to delete the existing home from the database
    ActionResult result = WarpMapper.getInst().removeWarp( args[ 0 ] );

    switch ( result ) {
      // Warp deleted
      case OK:
        sender.sendMessage( Messages.WARP_DELETED.apply( args[ 0 ] ) );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( Messages.WARP_NON_EXISTING.apply( args[ 0 ] ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "WARPDL->" + result ) );
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
      sender.sendMessage( Messages.USAGE.apply( "/updatewarp <Name>" ) );
      return;
    }

    // Try to fetch the existing warp
    Warp target = WarpMapper.getInst().getByName( args[ 0 ] );

    // Warp non existent
    if( target == null ) {
      sender.sendMessage( Messages.WARP_NON_EXISTING.apply( args[ 0 ] ) );
      return;
    }

    // Set the new player location and update in DB
    target.setLocation( sender.getLocation() );
    ActionResult result = WarpMapper.getInst().updateWarp( target );

    switch ( result ) {
      // Warp updated
      case OK:
        sender.sendMessage( Messages.WARP_UPDATED.apply( args[ 0 ] ) );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( Messages.WARP_NON_EXISTING.apply( args[ 0 ] ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "WARPUPD->" + result ) );
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

    // No warps found
    if( warps.size() == 0 ) {
      sender.sendMessage( Messages.WARP_LIST.apply() + Messages.WARP_NONE.getTemplate() );
      return;
    }

    // Build message
    ComplexMessage msg = new ComplexMessage( new ComplexPart( Messages.WARP_LIST.apply(), "", "", true ) );

    // Build home list
    for( int i = 0; i < warps.size(); i++ ) {
      // Append delimiter on everything but the first element
      if( i != 0 )
        msg.append( new ComplexPart( Messages.WARP_DELIMITER.getTemplate(), "", "", true ) );

      Warp warp = warps.get( i );
      Location loc = warp.getLocation();
      String locCords = "(" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
      String date = warpDateFormat.format( new Date( warp.getCreationDate() ) );
      String hover = Messages.WARP_LIST_HOVER.applyPrefixless( date, locCords );

      // Append current warp entry
      msg.append( new ComplexPart( Messages.WARP_COLOR.getTemplate() + warp.getName(), hover, "/warp " + warp.getName(), true ) );
    }

    // Send list of available homes to the player
    msg.send( sender );
  }
}
