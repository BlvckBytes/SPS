package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Essentials {

  private final Map< Player, Player > msgPartners;

  /**
   * Essential commands holder, create all maps and lists
   */
  public Essentials() {
    this.msgPartners = new HashMap<>();
  }

  /**
   * Command: msg
   * Used to send messages between players privately
   */
  @ShortCommand( command = "msg", terminalDeny = true )
  private void onMessage( Player sender, String[] args ) {

    // No recipient or no message specified
    if ( args.length <= 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /msg <Empfänger> <Nachricht>" );
      return;
    }

    // Make sure the recipient is online
    Player target = Bukkit.getPlayer( args[ 0 ] );
    if ( target == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDer Spieler '" + args[ 0 ] + "' ist nicht online!" );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 1 );
    sender.sendMessage( GlobalConstants.PREFIX + "§6Du §7» §6" + target.getDisplayName() + "§7: " + msg );
    target.sendMessage( GlobalConstants.PREFIX + "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg );

    // Keep last recipient in buffer
    this.msgPartners.put( sender, target );
    this.msgPartners.put( target, sender );
  }

  /**
   * Command: r
   * Used to reply to the last received message
   */
  @ShortCommand( command = "r", terminalDeny = true )
  private void onReply( Player sender, String[] args ) {

    // No recipient in buffer yet
    Player partner = this.msgPartners.get( sender );
    if ( partner == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDir hat noch niemand geschrieben, dem du antworten kannst!" );
      return;
    }

    // Recipient went offline
    if ( !partner.isOnline() ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDer Spieler '" + partner.getDisplayName() + "' ist in der Zwischenzeit offline gegangen!" );
      return;
    }

    // No message specified
    if ( args.length == 0 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /r <Nachricht>" );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 0 );
    sender.sendMessage( GlobalConstants.PREFIX + "§6Du §7» §6" + partner.getDisplayName() + "§7: " + msg );
    partner.sendMessage( GlobalConstants.PREFIX + "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg );
  }

  /**
   * Command: gm, gamemode
   * Used to change the gamemode of the player
   */
  @ShortCommand( command = "gamemode", aliases = { "gm" } )
  private void changeGameMode( Player sender, String[] args, String label ) {

    // No permission
    if ( !sender.hasPermission( "sps.gm" ) ) {
      sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM );
      return;
    }

    // No args specified
    if ( args.length == 0 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "Benutze: /" + label + " <Modus> [Spieler]" );
      return;
    }

    // Parse mode from arg, fallback is survival (0)
    Integer mode = Utils.tryParseInt( args[ 0 ] );
    GameMode gm = GameMode.getByValue( mode == null ? 0 : mode );

    // Invalid gamemode
    if ( gm == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cUngültiger Modus angegeben (" + args[ 0 ] + ")!" );
      return;
    }

    // Target specified, get player from args and change for that target
    if ( args.length == 2 ) {

      // No permission
      if( !sender.hasPermission( "sps.gm.other" ) ) {
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM );
        return;
      }

      Player target = Bukkit.getPlayer( args[ 1 ] );
      target.setGameMode( gm );

      // Notify target
      target.sendMessage(
        GlobalConstants.PREFIX + "Dein §dGameMode §7wurde von §d" + sender.getDisplayName() +
        " §7auf §d" + gm.name() + " §7geändert!"
      );

      // Notify executor
      sender.sendMessage(
        GlobalConstants.PREFIX + "Du hast den §dGameMode §7von §d" + target.getDisplayName() +
        " §7erfolgreich auf §d" + gm.name() + " §7geändert!"
      );

      return;
    }

    // Change for the executor himself, since no target was specified
    sender.setGameMode( gm );
    // Notify executor
    sender.sendMessage( GlobalConstants.PREFIX + "Du hast deinen §dGameMode §7auf §d" + gm.name() + " §7geändert!" );
  }

  /**
   * Command: fly
   * Change the setAllowFly state of player
   */
  @ShortCommand( command = "fly" )
  private void changeFly( Player sender, String[] args ) {
    if ( !sender.hasPermission("sps.fly") ) {
      sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM.toString() );
      return;
    }

    if ( args.length == 0 ) {
      sender.setAllowFlight( !sender.getAllowFlight() );
    }
    if ( args.length == 1 ) {
      if ( !sender.hasPermission("sps.fly.other") ) {
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM.toString() );
        return;
      }
      Player target = Bukkit.getPlayer( args[0] );
      if( target != null ) {
        target.setAllowFlight( !target.getAllowFlight() );
        sender.sendMessage( GlobalConstants.PREFIX.toString() + "Fly toggled for " + target.getName() );
        return;
      }
      sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NOT_FOUND.toString() );
    }
  }
}
