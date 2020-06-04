package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.shortcmds.ShortCommand;
import org.bukkit.command.CommandSender;

public class Essentials {

  @ShortCommand( command = "debug" )
  private void onDebug( CommandSender sender, String[] args ) {
    StringBuilder sb = new StringBuilder();

    // Concat all provided params as #toString()
    for( String arg : args )
      sb.append( ";" ).append( arg );

    // Drop leading semicolon
    sb.delete( 0, 1 );

    // Send to player
    sender.sendMessage( GlobalConstants.PREFIX + "/debug, args: '" + sb + "'" );
  }

  @ShortCommand( command = "debug2", terminalDeny = true )
  private void onDebug2( CommandSender sender, String[] args ) {
    StringBuilder sb = new StringBuilder();

    // Concat all provided params as #toString()
    for( String arg : args )
      sb.append( ";" ).append( arg );

    // Drop leading semicolon
    sb.delete( 0, 1 );

    // Send to player
    sender.sendMessage( GlobalConstants.PREFIX + "/debug2, args: '" + sb + "'" );
  }
}
