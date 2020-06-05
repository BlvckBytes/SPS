package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Essentials {

  private Map< Player, Player > msgPartners;

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
    if( args.length <= 1 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /msg <Empfänger> <Nachricht>" );
      return;
    }

    // Make sure the recipient is online
    Player target = Bukkit.getPlayer( args[ 0 ] );
    if( target == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDer Spieler '" + args[ 0 ] + "' ist nicht online!" );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 1 );
    sender.sendMessage( "§6Du §7» §6" + target.getDisplayName() + "§7: " + msg );
    target.sendMessage( "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg );

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
    if( partner == null ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDir hat noch niemand geschrieben, dem du antworten kannst!" );
      return;
    }

    // Recipient went offline
    if( !partner.isOnline() ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cDer Spieler '" + partner.getDisplayName() + "' ist in der Zwischenzeit offline gegangen!" );
      return;
    }

    // No message specified
    if( args.length == 0 ) {
      sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /r <message>" );
      return;
    }

    // Send out messages
    String msg = Utils.concatArgs( args, 0 );
    sender.sendMessage( "§6Du §7» §6" + partner.getDisplayName() + "§7: " + msg );
    partner.sendMessage( "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg );
  }
}
