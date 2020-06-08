package at.sps.core;

import at.sps.commands.Essentials;
import at.sps.commands.HomeCmd;
import at.sps.core.shortcmds.SCManager;
import at.sps.core.storage.MariaDB;
import at.sps.model.Home;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.util.List;

public class Main extends JavaPlugin {

  @Getter
  private static MariaDB db;

  @Override
  public void onEnable() {
    // Standard enabling routines
    registerCommands();
    setupResources();

    try {
      ResultSet rs = db.readData( "SELECT * FROM `Home`", null );
      List< Home > homes = Home.read( rs );

      for( Home h : homes )
        System.out.println( h );

    } catch ( Exception e ) {
      e.printStackTrace();
    }

    ConsoleLogger.getInst().logMessage( "&aSystem initialized successfully!" );
  }

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
    SCManager scMan = new SCManager( this );
    scMan.registerContainer( new Essentials() );
    scMan.registerContainer( new HomeCmd() );
  }

  /**
   * Set up all needed resources for external classes
   */
  private void setupResources() {
    db = new MariaDB( "root", "aidoh8Aitah5e", "sps" );
    db.connect();
  }
}
