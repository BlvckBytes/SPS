package at.sps.core;

import at.sps.commands.EssentialCmds;
import at.sps.commands.HomeCmds;
import at.sps.commands.WarpCmds;
import at.sps.core.orm.mappers.WarpMapper;
import at.sps.core.shortcmds.SCManager;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.mappers.HomeMapper;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

  @Getter
  private static MariaDB db;

  @Getter
  private static SCManager scM;

  // On plugin load
  @Override
  public void onEnable() {
    // Standard enabling routines
    registerCommands();
    setupResources();

    ConsoleLogger.getInst().logMessage( "&aSystem initialized successfully!" );
  }

  // On plugin unload
  @Override
  public void onDisable() {
    // Disconnect DB if existent
    if( db != null )
      db.disconnect();

    ConsoleLogger.getInst().logMessage( "&aSystem shutdown!" );
  }

  /**
   * Register all command containers to the short command manager
   */
  private void registerCommands() {
    scM = new SCManager( this );
    scM.registerContainer( new EssentialCmds() );
    scM.registerContainer( new HomeCmds() );
    scM.registerContainer( new WarpCmds() );
  }

  /**
   * Set up all needed resources for external classes
   */
  private void setupResources() {
    // Connect to database
    db = new MariaDB( "root", "aidoh8Aitah5e", "sps" );
    db.connect();

    // Create all needed tables
    db.buildTables( HomeMapper.getInst(), WarpMapper.getInst() );
  }
}
