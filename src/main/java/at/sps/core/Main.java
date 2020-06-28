package at.sps.core;

import at.sps.commands.*;
import at.sps.core.conf.SPSConfig;
import at.sps.core.gui.ChatPrompt;
import at.sps.core.gui.InventoryGUI;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.mappers.*;
import at.sps.core.shortcmds.SCManager;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Set;

public class Main extends JavaPlugin {

  @Getter
  private static MariaDB db;

  @Getter
  private static Main inst;

  private SCManager scM;

  // On plugin load
  @Override
  public void onEnable() {
    inst = this;

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

    // Destroy all existing and still active inventory guis and chat prompts
    InventoryGUI.destroyAll();
    ChatPrompt.destroyAll();

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
    scM.registerContainer( new BanCmds() );
  }

  /**
   * Get a list of known (registered) commands
   * @return Set of strings, each string representing one command
   */
  public Set< String > getCommands() {
    return scM.getKnownCommands();
  }

  /**
   * Set up all needed resources for external classes
   */
  private void setupResources() {
    // Connect to database
    db = new MariaDB( "root", "aidoh8Aitah5e", "sps" );
    db.connect();

    // Create all needed tables for the model mappers
    db.buildTables(
      HomeMapper.getInst(),
      WarpMapper.getInst(),
      KitMapper.getInst(),
      KitCooldownMapper.getInst(),
      BanMapper.getInst()
    );

    // Create and load config file, this is not used anywhere else ATM
    new SPSConfig( new File( getDataFolder(), "config.yml" ) );
  }
}
