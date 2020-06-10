package at.sps.core.orm.mappers;

import at.sps.core.ConsoleLogger;
import at.sps.core.Main;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.ObjectRebuilder;
import at.sps.core.orm.models.Warp;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WarpMapper extends ModelMapper< Warp > {

  private static WarpMapper inst;

  /**
   * The warp-mapper is used to add, delete, re-set and list warps
   * @param database Connection to the SQL database for r/w
   */
  public WarpMapper( MariaDB database ) {
    super( database );
    inst = this;

    // Register world translator to get the world's name, UUID translator to perform toString
    registerTranslator( World.class, val -> ( ( World ) val ).getName() );
    registerTranslator( UUID.class, Object::toString );
  }

  /**
   * Add a new warp to the storage
   * @param warp Warp to store
   * @return ActionResult which tells the status
   */
  public ActionResult addWarp( Warp warp ) {
    return write( warp, false );
  }

  /**
   * Remove an existing warp from the storage
   * @param name The target warp's name
   * @return ActionResult which tells the status
   */
  public ActionResult removeWarp( String name ) {
    return delete( Collections.singletonList( getByName( name ) ) );
  }

  /**
   * Update an existing warp, for re-setting the location f.e.
   * @param warp Warp to update in DB
   * @return ActionResult which tells the status
   */
  public ActionResult updateWarp( Warp warp ) {
    return write( warp, true );
  }

  /**
   * Search a warp by it's name, the name is primary key and can only exist once
   * @param name Name of the target warp
   * @return Warp if exists, null otherwise
   */
  public Warp getByName( String name ) {
    try {
      // Fetch result to get only the target warp with corresponding name
      List< Warp > result = read( database.fetchResult(
        "SELECT * FROM `Warp`" +
        "WHERE `name` = ?",
        name
      ) );

      // There can just be one result since name is the primary key
      return result.size() > 0 ? result.get( 0 ) : null;
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while searching for a warp by it's name!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return null;
    }
  }

  /**
   * List all warps containing the specified searchterm in their name
   * @param searchterm Searchterm for name search
   * @return List of warps with corresponding name
   */
  public List< Warp > listWarps( String searchterm ) {
    try {
      // Fetch result to get only the warps containing the searchterm
      return read( database.fetchResult(
        "SELECT * FROM `Warp`" +
        "WHERE LOWER(`name`) LIKE LOWER(CONCAT('%', ?, '%'))",
        searchterm
      ) );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while searching for warps by name!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return new ArrayList<>();
    }
  }

  /**
   * Read warps from the database
   * @param rs ResultSet from an executed query
   * @return List of generated warps
   */
  @Override
  public List< Warp > read( ResultSet rs ) {
    List< Warp > buf = new ArrayList<>();

    try {
      // Loop result rows
      while( rs.next() ) {
        // Get the world by name
        World w = Bukkit.getWorld( rs.getString( "world" ) );

        // Rebuild location based on the world and coordinates from RS
        Location loc = new ObjectRebuilder< Location >( Location.class, rs )
                .addArgs( w )
                .addColumns( "x", "y", "z", "yaw", "pitch" )
                .build();

        // Fetch uuid and generate warp object into buffer list
        UUID uu = UUID.fromString( rs.getString( "creator" ) );
        long creationDate = rs.getLong( "creationdate" );
        Warp warp = new Warp( rs.getString( "name" ), loc, creationDate, uu );

        // Set ID and add warp to list
        warp.setID( rs.getInt( "ID" ) );
        buf.add( warp );
      }
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while mapping read warps!" );
      ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
    }

    return buf;
  }

  /**
   * Singleton getter of the warp mapper
   * @return WarpMapper object instance
   */
  public static WarpMapper getInst() {
    if( inst == null )
      return new WarpMapper( Main.getDb() );

    return inst;
  }
}
