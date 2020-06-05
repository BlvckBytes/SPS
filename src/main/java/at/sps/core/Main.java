package at.sps.core;

import at.sps.commands.Essentials;
import at.sps.commands.HomeCmd;
import at.sps.core.shortcmds.SCManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    // Standard enabling routines
    registerCommands();

    ConsoleLogger.getInst().logMessage( "&aSystem initialized successfully!" );
  }

  @Override
  public void onDisable() {
    ConsoleLogger.getInst().logMessage( "&aSystem shutdown!" );
  }

  /**
   * Register all command containers to the short command manager
   */
  private void registerCommands() {
    SCManager scMan = new SCManager( this );
    scMan.registerContainer( new Essentials() );
    scMan.registerContainer( new HomeCmd() );
  }
}
