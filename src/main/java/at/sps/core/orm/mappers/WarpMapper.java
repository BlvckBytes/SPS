package at.sps.core.orm.mappers;

import at.sps.core.ConsoleLogger;
import at.sps.core.Main;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.ObjectRebuilder;
import at.sps.core.utils.Utils;
import at.sps.core.orm.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
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
  }

  public ActionResult addWarp( Warp warp ) {
    return write( warp, false );
  }

  public ActionResult deleteWarp( Warp warp ) {
    return delete( Collections.singletonList( warp ) );
  }

  public ActionResult updateWarp( Warp warp ) {
    return write( warp, true );
  }

  /**
   * Search a home by it's name, the name is primary key and can only exist once
   * @param name Name of the target home
   * @return Home if exists, null otherwise
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
        "WHERE LOWER(`name`) LIKE LOWER('%?%')",
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
   * @return List of generated homes
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

        // Fetch uuid and generate Home object into buffer list
        UUID uu = UUID.fromString( rs.getString( "creator" ) );
        long creationDate = rs.getLong( "creationdate" );
        buf.add( new Warp( rs.getString( "name" ), loc, creationDate, uu ) );
      }
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while mapping read warps!" );
      ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
    }

    return buf;
  }

  /**
   * Write a warp into the database
   * @param update Whether or not to use the updating feature
   * @param element Element to write or update
   */
  @Override
  public ActionResult write( Warp element, boolean update ) {
    // No data provided
    if( element == null )
      return ActionResult.NO_DATA;

    try {
      // Create an array of the element's row values
      Location loc = element.getLocation();
      Object[] data = {
        element.getName(), element.getCreator().toString(), element.getCreationDate(), loc.getX(),
        loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName()
      };

      // TODO: int insertOrUpdate( String[] keys, Object[] data, boolean update, String... columns ) throws Exception
      // Example: insertOrUpdate( new String[]{ "name" }, data, "x", "y", "z", "yaw", "pitch", "world" );

      // Values are: name, uuid, x, y, z, yaw, pitch, world
      String query = "INSERT INTO `Warp` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

      // Only add the duplicate key update if desired
      if( update ) {
        query += "ON DUPLICATE KEY UPDATE creator = VALUES(creator), creationdate = VALUES(creationdate)," +
                "x = VALUES(x), y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)," +
                "world = VALUES(world)";
      }

      database.executeUpdate( query, data );
      return ActionResult.OK;
    } catch ( SQLIntegrityConstraintViolationException e2 ) {
      return ActionResult.ALREADY_EXISTENT;
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while writing warp!" );
      ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
      return ActionResult.INTERNAL_ERROR;
    }
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
