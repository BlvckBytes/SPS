package at.sps.commands;

import at.sps.core.Main;
import at.sps.core.conf.Messages;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.ComplexMessage;
import at.sps.core.utils.ComplexPart;
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
      sender.sendMessage( Messages.USAGE.apply( "/msg <Empfänger> <Nachricht>" ) );
      return;
    }

    // Make sure the recipient is online
    Player target = Bukkit.getPlayer( args[ 0 ] );
    if ( target == null ) {
      sender.sendMessage( Messages.NOT_ONLINE.apply( args[ 0 ] ) );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 1 );
    sender.sendMessage( Messages.MSG_OUT.apply( target.getDisplayName(), msg ) );
    sender.sendMessage( Messages.MSG_IN.apply( sender.getDisplayName(), msg ) );

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
      sender.sendMessage( Messages.NO_MSG_PARTNER.apply() );
      return;
    }

    // Recipient went offline
    if ( !partner.isOnline() ) {
      sender.sendMessage( Messages.WENT_OFFLINE.apply( partner.getDisplayName() ) );
      msgPartners.remove( sender );
      msgPartners.remove( partner );
      return;
    }

    // No message specified
    if ( args.length == 0 ) {
      sender.sendMessage( Messages.USAGE.apply( "/r <Nachricht>" ) );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 0 );
    sender.sendMessage( Messages.MSG_OUT.apply( partner.getDisplayName(), msg ) );
    sender.sendMessage( Messages.MSG_IN.apply( sender.getDisplayName(), msg ) );
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
      sender.sendMessage( Messages.NO_PERM.apply( "sps.gm" ) );
      return;
    }

    // No args specified
    if ( args.length == 0 ) {
      sender.sendMessage( Messages.USAGE.apply( "/" + label + " <Modus> [Spieler]" ) );
      return;
    }

    // Parse mode from arg, fallback is survival (0)
    Integer mode = Utils.tryParseInt( args[ 0 ] );
    GameMode gm = GameMode.getByValue( mode == null ? 0 : mode );

    // Invalid gamemode
    if ( gm == null ) {
      sender.sendMessage( Messages.INVALID_GM.apply( args[ 0 ] ) );
      return;
    }

    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";

    // Target specified, get player from args and change for that target
    if ( args.length == 2 ) {

      // No permission
      if( !sender.hasPermission( "sps.gm.other" ) ) {
        sender.sendMessage( Messages.NO_PERM.apply( "sps.gm.other" ) );
        return;
      }

      Player target = Bukkit.getPlayer( args[ 1 ] );
      target.setGameMode( gm );

      // Notify players
      target.sendMessage( Messages.GM_CHANGED_TARGET.apply( executor, gm.name() ) );
      sender.sendMessage( Messages.GM_CHANGED_EXECUTOR.apply( target.getDisplayName(), gm.name() ) );
      return;
    }

    // No player, can't change gm of console...
    if( executor.equals( "Console" ) ) {
      sender.sendMessage( Messages.PLAYER_ONLY.apply( label ) );
      return;
    }

    // Change for the executor himself, since no target was specified, also notify
    ( ( Player ) sender ).setGameMode( gm );
    sender.sendMessage( Messages.GM_CHANGED.apply( gm.name() ) );
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
      sender.sendMessage( Messages.NO_PERM.apply( "sps.fly" ) );
      return;
    }

    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";

    // No target specified, toggle flight for executor
    if ( args.length == 0 ) {

      // No player, can't toggle fly of console...
      if( executor.equals( "Console" ) ) {
        sender.sendMessage( Messages.PLAYER_ONLY.apply( "fly" ) );
        return;
      }

      // Toggle state and notify
      Player exec = ( Player ) sender;
      boolean newState = !exec.getAllowFlight();
      exec.setAllowFlight( newState );
      sender.sendMessage( Messages.FLY_CHANGED.apply( ( newState ? "an" : "aus" ) ) );
      return;
    }

    // Target specified
    if ( args.length == 1 ) {
      // No permission
      if ( !sender.hasPermission( "sps.fly.other" ) ) {
        sender.sendMessage( Messages.NO_PERM.apply( "sps.fly.other" ) );
        return;
      }

      // Check if target is online
      Player target = Bukkit.getPlayer( args[ 0 ] );
      if( target == null ) {
        sender.sendMessage( Messages.NOT_ONLINE.apply( args[ 0 ] ) );
        return;
      }

      // Toggle flight
      boolean newState = !target.getAllowFlight();
      String newStateName = newState ? "an" : "aus";
      target.setAllowFlight( newState );

      // Notify players
      target.sendMessage( Messages.FLY_CHANGED_TARGET.apply( executor, newStateName ) );
      sender.sendMessage( Messages.FLY_CHANGED_EXECUTOR.apply( target.getDisplayName(), newStateName ) );
    }
  }

  /**
   * Comand: vote
   * Usage: /vote
   * Used to display vote links
   */
  @ShortCommand( command = "vote", terminalDeny = true )
  private void onVote( Player sender, String[] args ) {
    sender.sendMessage( Messages.PANEL_SPACER.applyPrefixless( "Vote" ) );
    sender.sendMessage( "§7> Du kannst §djeden Tag §7für uns Voten, um als Dankeschön einen §dReward §7zu erhalten." );
    sender.sendMessage( "§7> Benutze dafür einfach einen der folgenden §dlinks§7:" );

    String[] links = { "google.at", "tonymacx86.com" };
    for( String link : links ) {
      ComplexMessage linkMsg = new ComplexMessage( new ComplexPart( "§7> ", "", "", false ) );
      linkMsg.append( new ComplexPart( "§d" + link, "§7Klick hier, um den Link zu §döffnen§7!", "https://" + link, false ) );
      linkMsg.send( sender );
    }

    sender.sendMessage( Messages.PANEL_SPACER.applyPrefixless( "Vote" ) );
  }

  /**
   * Command: sps
   * Usage: /sps
   * Used to list all known commands of the SPS
   */
  @ShortCommand( command = "sps" )
  private void onSPS( CommandSender sender, String[] args ) {
    Set< String > commands = Main.getScM().getKnownCommands();
    StringBuilder list = new StringBuilder();

    // Build list of commands inline
    int c = 0;
    for( String command : commands ) {
      list.append( c == 0 ? "" : Messages.SPS_DELIMITER.getTemplate() ).append( "/" ).append( command );
      c++;
    }

    sender.sendMessage( Messages.SPS_LIST.apply( list.toString() ) );
  }
}
