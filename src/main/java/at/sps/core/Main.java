package at.sps.core;

import at.sps.commands.EssentialCmds;
import at.sps.commands.HomeCmds;
import at.sps.commands.KitCmds;
import at.sps.commands.WarpCmds;
import at.sps.core.conf.SPSConfig;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.mappers.HomeMapper;
import at.sps.core.orm.mappers.KitCooldownMapper;
import at.sps.core.orm.mappers.KitMapper;
import at.sps.core.orm.mappers.WarpMapper;
import at.sps.core.shortcmds.SCManager;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

    SLogging.getInst().log( "System initialized successfully!", LogLevel.INFO );
  }

  // On plugin unload
  @Override
  public void onDisable() {
    // Disconnect DB if existent
    if( db != null )
      db.disconnect();

    SLogging.getInst().log( "System shutdown successfully!", LogLevel.INFO );
  }

  /**
   * Register all command containers to the short command manager
   */
  private void registerCommands() {
    scM = new SCManager( this );
    scM.registerContainer( new EssentialCmds() );
    scM.registerContainer( new HomeCmds() );
    scM.registerContainer( new WarpCmds() );
    scM.registerContainer( new KitCmds() );
  }

  /**
   * Set up all needed resources for external classes
   */
  private void setupResources() {
    // Connect to database
    db = new MariaDB( "root", "aidoh8Aitah5e", "sps" );
    db.connect();

    // Create all needed tables
    db.buildTables( HomeMapper.getInst(), WarpMapper.getInst(), KitMapper.getInst(), KitCooldownMapper.getInst() );

    // Create and load config file, this is not used anywhere else ATM
    new SPSConfig( new File( getDataFolder(), "config.yml" ) );
  }
}
