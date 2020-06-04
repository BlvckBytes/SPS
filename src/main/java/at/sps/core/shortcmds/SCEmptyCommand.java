package at.sps.core.shortcmds;

import at.sps.core.utils.ParamCall;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class SCEmptyCommand extends BukkitCommand {

  private final ParamCall< String[] > invocation;

  /**
   * Create an "empty" bukkit-command for registration so that
   * tabbing works, everything else will be catched with the
   * commandpreprocessevent later on anyways, for players.
   * The console invocation will work through the callback
   *
   * @param command Command to register
   * @param invocation Callback for console invocation
   */
  public SCEmptyCommand( String command, ParamCall< String[] > invocation ) {
    super( command );
    this.invocation = invocation;
  }

  @Override
  public boolean execute( CommandSender sender, String label, String[] args ) {
    // Only listen to the console sender
    if( sender instanceof Player )
      return true;

    // Call the invocation callback with provided arguments
    invocation.call( args );
    return true;
  }
}
