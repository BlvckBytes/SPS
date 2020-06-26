package at.sps.commands;

import at.sps.core.Main;
import at.sps.core.conf.Messages;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.ComplexMessage;
import at.sps.core.utils.ComplexPart;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class EssentialCmds extends CommandBase {

  private final Map< Player, Player > msgPartners;
  private final List< Player > msgSpies;

  /**
   * Essential commands holder, create all maps and lists
   */
  public EssentialCmds() {
    this.msgPartners = new HashMap<>();
    this.msgSpies = new ArrayList<>();
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
    Player target = findPlayer( sender, args[ 0 ] );
    if ( target == null )
      return;

    // Can't write messages to oneself
    if( target.equals( sender ) ) {
      sender.sendMessage( Messages.MSG_SELF.apply() );
      return;
    }

    // Send out messages
    String msg = concatArgs( args, 1 );
    sender.sendMessage( Messages.MSG_OUT.apply( target.getDisplayName(), msg ) );
    target.sendMessage( Messages.MSG_IN.apply( sender.getDisplayName(), msg ) );

    // Send messages to spies
    informSpies( sender, target, msg );

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
    // No message specified
    if ( args.length == 0 ) {
      sender.sendMessage( Messages.USAGE.apply( "/r <Nachricht>" ) );
      return;
    }

    // No recipient in buffer yet
    Player partner = this.msgPartners.get( sender );
    if ( partner == null ) {
      sender.sendMessage( Messages.NO_MSG_PARTNER.apply() );
      return;
    }

    // Recipient went offline in the mean time
    if ( !partner.isOnline() ) {
      sender.sendMessage( Messages.WENT_OFFLINE.apply( partner.getDisplayName() ) );
      msgPartners.remove( sender );
      msgPartners.remove( partner );
      return;
    }

    // Send out messages
    String msg = concatArgs( args, 0 );
    sender.sendMessage( Messages.MSG_OUT.apply( partner.getDisplayName(), msg ) );
    partner.sendMessage( Messages.MSG_IN.apply( sender.getDisplayName(), msg ) );

    // Send messages to spies
    informSpies( sender, partner, msg );
  }

  @ShortCommand( command = "msgspy", terminalDeny = true )
  private void onMsgSpy( Player sender, String[] args ) {
    // No permission
    if( lacksPermission( sender, "sps.msgspy" ) )
      return;

    // Contains already, unsibscribe
    if( msgSpies.contains( sender ) ) {
      sender.sendMessage( Messages.MSG_SPY_UBSUBSCRIBED.apply() );
      msgSpies.remove( sender );
      return;
    }

    // Not in the list, subscribe
    sender.sendMessage( Messages.MSG_SPY_SUBSCRIBED.apply() );
    msgSpies.add( sender );
  }

  /**
   * Inform all players that subscribed to msg-spy-mode that a new
   * private message has been sent
   * @param sender Sending player
   * @param receiver Receiving player
   * @param message Message sent
   */
  private void informSpies( Player sender, Player receiver, String message ) {
    // Send messages to spies
    for( Player spy : msgSpies ) {
      // Don't want to spy your own messages...
      if( spy.equals( sender ) || spy.equals( receiver ) )
        continue;

      spy.sendMessage( Messages.MSG_SPY.apply( sender.getDisplayName(), receiver.getDisplayName(), message ) );
    }
  }

  /**
   * Command: gm, gamemode
   * Usage: /gm|gamemode mode [player]
   * Used to change the gamemode of the player
   */
  @ShortCommand( command = "gamemode", aliases = { "gm" } )
  private void onGameMode( CommandSender sender, String[] args, String label ) {
    // No permission
    if ( lacksPermission( sender, "sps.gm" ) )
      return;

    // Arguments mismatch
    if ( args.length == 0 || args.length > 2 ) {
      sender.sendMessage( Messages.USAGE.apply( "/" + label + " <Modus> [Spieler]" ) );
      return;
    }

    GameMode mode = getGameModeFromInput( args[ 0 ] );

    // This gamemode is unknown to minecraft
    if( mode == null ) {
      sender.sendMessage( Messages.INVALID_GM.apply( args[ 0 ] ) );
      return;
    }

    // Change the gamemode for the executor himself
    if( args.length == 1 ) {
      // Check if he is a player
      Player target = getPlayerSender( sender, label );
      if( target == null )
        return;

      // Set gamemode and notify
      target.setGameMode( mode );
      sender.sendMessage( Messages.GM_CHANGED.apply( mode.name() ) );
      return;
    }

    // No permission
    if( lacksPermission( sender, "sps.gm.other" ) )
      return;

    // Change the gamemode for the target player
    // Get the target player and validate that he's online
    Player target = findPlayer( sender, args[ 1 ] );
    if( target == null )
      return;

    // Change gamemode
    target.setGameMode( mode );

    // Notify players
    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";
    target.sendMessage( Messages.GM_CHANGED_TARGET.apply( executor, mode.name() ) );
    sender.sendMessage( Messages.GM_CHANGED_EXECUTOR.apply( target.getDisplayName(), mode.name() ) );
  }

  /**
   * Get a gamemode by it's ID from the GameMode internal ID-LUT
   * @param input ID or name to search for
   * @return GameMode if existent, null otherwise
   */
  @SuppressWarnings( { "unchecked" } )
  private GameMode getGameModeFromInput( String input ) {
    try {
      // Input has been a number, thus use LUT
      Integer number = tryParseInt( input );
      if( number != null ) {

        // Make target field accessible
        Field mapField = GameMode.class.getDeclaredField( "BY_ID" );
        mapField.setAccessible( true );

        // Get gamemode LUT
        Map< Integer, GameMode > modeMap = ( Map< Integer, GameMode > ) mapField.get( null );

        // GameMode non existent
        if ( !modeMap.containsKey( number ) )
          return null;

        // Return gamemode from LUT
        return modeMap.get( number );
      }

      // Input is a string, thus use valueOf
      try {
        return GameMode.valueOf( input.toUpperCase() );
      } catch ( Exception e ) {
        // Invalid gamemode name
        return null;
      }
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while trying to get a gamemode by it's ID!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * Command: fly
   * Usage: /fly [Player]
   * Toggle the setAllowFly state of a player
   */
  @ShortCommand( command = "fly" )
  private void changeFly( CommandSender sender, String[] args ) {
    // No permission
    if ( lacksPermission( sender, "sps.fly" ) )
      return;

    // Arguments mismatch
    if ( args.length > 1 ) {
      sender.sendMessage( Messages.USAGE.apply( "/fly [Spieler]" ) );
      return;
    }

    // No target specified, toggle flight for executor
    if ( args.length == 0 ) {

      // Validate that the executor is a player
      Player player = getPlayerSender( sender, "fly" );
      if( player == null )
        return;

      // Toggle state
      boolean newState = !player.getAllowFlight();
      player.setAllowFlight( newState );

      // Inform player
      sender.sendMessage( Messages.FLY_CHANGED.apply( ( newState ? "an" : "aus" ) ) );
      return;
    }

    // Target to toggle got specified
    // No permission
    if ( lacksPermission( sender, "sps.fly.other" ) )
      return;

    String executor = sender instanceof Player ? ( ( Player ) sender ).getDisplayName() : "Console";

    // Make sure the target is online
    Player target = findPlayer( sender, args[ 0 ] );
    if( target == null )
      return;

    // Toggle flight for the target
    boolean newState = !target.getAllowFlight();
    target.setAllowFlight( newState );

    // Notify players
    String newStateName = newState ? "an" : "aus";
    target.sendMessage( Messages.FLY_CHANGED_TARGET.apply( executor, newStateName ) );
    sender.sendMessage( Messages.FLY_CHANGED_EXECUTOR.apply( target.getDisplayName(), newStateName ) );
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
