package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.Main;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EssentialCmds {

  private final Map< Player, Player > msgPartners;

  /**
   * Essential commands holder, create all maps and lists
   */
  public EssentialCmds() {
    this.msgPartners = new HashMap<>();
  }

  /**
   * Command: msg
   * Usage: /msg player message
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
   * Usage: /r message
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
   * Usage: /gm|gamemode mode [player]
   * Used to change the gamemode of the player
   */
  @ShortCommand( command = "gamemode", aliases = { "gm" } )
  private void changeGameMode( CommandSender sender, String[] args, String label ) {

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

    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";

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
        GlobalConstants.PREFIX + "Dein §dGameMode §7wurde von §d" + executor +
        " §7auf §d" + gm.name() + " §7geändert!"
      );

      // Notify executor
      sender.sendMessage(
        GlobalConstants.PREFIX + "Du hast den §dGameMode §7von §d" + target.getDisplayName() +
        " §7erfolgreich auf §d" + gm.name() + " §7geändert!"
      );

      return;
    }

    // No player, can't change gm of console...
    if( executor.equals( "Console" ) ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§c" + GlobalConstants.PLAYER_ONLY.toString() );
      return;
    }

    // Change for the executor himself, since no target was specified
    ( ( Player ) sender ).setGameMode( gm );
    // Notify executor
    sender.sendMessage( GlobalConstants.PREFIX + "Du hast deinen §dGameMode §7auf §d" + gm.name() + " §7geändert!" );
  }

  /**
   * Command: fly
   * Usage: /fly [Player]
   * Toggle the setAllowFly state of a player
   */
  @ShortCommand( command = "fly" )
  private void changeFly( CommandSender sender, String[] args ) {

    // No permission
    if ( !sender.hasPermission( "sps.fly" ) ) {
      sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM.toString() );
      return;
    }

    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";

    // No target specified, toggle flight for executor
    if ( args.length == 0 ) {

      // No player, can't toggle fly of console...
      if( executor.equals( "Console" ) ) {
        sender.sendMessage( GlobalConstants.PREFIX + "§c" + GlobalConstants.PLAYER_ONLY.toString() );
        return;
      }

      // Toggle state
      Player exec = ( Player ) sender;
      boolean newState = !exec.getAllowFlight();
      exec.setAllowFlight( newState );
      sender.sendMessage( GlobalConstants.PREFIX + "Du hast deinen §dFlymode §7auf §d" + ( newState ? "an" : "aus" ) + " §7geändert!" );
      return;
    }

    // Target specified
    if ( args.length == 1 ) {
      // No permission
      if ( !sender.hasPermission( "sps.fly.other" ) ) {
        sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM );
        return;
      }

      // Check if target is online
      Player target = Bukkit.getPlayer( args[ 0 ] );
      if( target == null ) {
        sender.sendMessage( GlobalConstants.PREFIX + "§cDer Spieler '" + args[ 0 ] + "' ist nicht online!" );
        return;
      }

      // Toggle flight
      boolean newState = !target.getAllowFlight();
      String newStateName = newState ? "an" : "aus";
      target.setAllowFlight( newState );

      // Notify target
      target.sendMessage(
        GlobalConstants.PREFIX + "Dein §dFlugmodus §7wurde von §d" + executor +
        " §7auf §d" + newStateName + " §7geändert!"
      );

      // Notify executor
      sender.sendMessage(
        GlobalConstants.PREFIX + "Du hast den §dFlugmodus §7von §d" + target.getDisplayName() +
        " §7erfolgreich auf §d" + newStateName + " §7geändert!"
      );
    }
  }

  /**
   * Command: sps
   * Usage: /sps
   * Used to list all known commands of the SPS
   */
  @ShortCommand( command = "sps" )
  private void onSPS( CommandSender sender, String[] args ) {
    Set< String > commands = Main.getScM().getKnownCommands();
    StringBuilder message = new StringBuilder( GlobalConstants.PREFIX + "§7Registrierte Befehle: " );

    // Build list of commands inline
    int c = 0;
    for( String command : commands ) {
      message.append( c == 0 ? "" : "§7, " ).append( "§d/" ).append( command );
      c++;
    }

    sender.sendMessage( message.toString() );
  }
}
