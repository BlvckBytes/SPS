package at.sps.mappers;

import at.sps.core.ConsoleLogger;
import at.sps.core.storage.MariaDB;
import at.sps.core.storage.ModelMapper;
import at.sps.core.storage.ObjectRebuilder;
import at.sps.core.storage.ActionResult;
import at.sps.core.utils.Utils;
import at.sps.model.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

public class HomeMapper implements ModelMapper< Home > {

    private final MariaDB database;

    /**
     * The home-mapper is used to add, delete, search and list player homes
     * @param database Connection to the SQL database for r/w
     */
    public HomeMapper( MariaDB database ) {
        this.database = database;
    }

    /**
     * Add a new home to the storage
     * @param home Home to add to database
     * @return ActionResult which tells the status
     */
    public ActionResult addHome( Home home ) {
        return write( database.getConn(), home, false );
    }

    /**
     * Deletes a home from an player-account based on it's name
     * @param playerId Home owner ID
     * @param name Name of the home
     * @return ActionResult which tells the status
     */
    public ActionResult removeHome( UUID playerId, String name ) {
        return delete( database.getConn(), new Home( playerId, name, null ) );
    }

    /**
     * Get a home by it's name from a player
     * @param playerId Home owner ID
     * @param name Name of the home
     * @return Home if found, null otherwise
     */
    public Home getByName( UUID playerId, String name ) {
        try {
            // Fetch result to get only the target player's home with corresponding name
            List< Home > result = read( database.fetchResult(
                "SELECT * FROM `Home`" +
                "WHERE `uuid` = ? AND `name` = ?",
                playerId.toString(), name
            ) );

            // There can just be one result since UUID & name are the key
            return result.size() > 0 ? result.get( 0 ) : null;
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while searching for a home by it's name!" );
            ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
            return null;
        }
    }

    /**
     * List all homes for a specific player
     * @param playerId Home owner ID
     * @return List of his homes
     */
    public List< Home > listHomes( UUID playerId ) {
        try {
            // Fetch result to get only the target player's homes
            return read( database.fetchResult(
                "SELECT * FROM `Home`" +
                "WHERE `uuid` = ?",
                playerId.toString()
            ) );
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while searching for a home by it's name!" );
            ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
            return null;
        }
    }

    /**
     * Read homes from the database
     * @param rs ResultSet from an executed query
     * @return List of generated homes
     */
    @Override
    public List< Home > read( ResultSet rs ) {
        List< Home > buf = new ArrayList<>();

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
                UUID uu = UUID.fromString( rs.getString( "uuid" ) );
                buf.add( new Home( uu, rs.getString( "name" ), loc ) );
            }
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while mapping read homes!" );
            ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
        }

        return buf;
    }

    /**
     * Write homes into the database
     * @param conn Connection to prepare statement on
     * @param update Whether or not to use the updating feature
     * @param element Element to write or update
     */
    @Override
    public ActionResult write( Connection conn, Home element, boolean update ) {
        // No data provided
        if( element == null )
            return ActionResult.NO_DATA;

        try {
            // Create an array of the element's row values
            Location loc = element.getLocation();
            Object[] data = {
                element.getName(), element.getPlayerUUID().toString(), loc.getX(),
                loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName()
            };

            // Values are: name, uuid, x, y, z, yaw, pitch, world
            String query = "INSERT INTO `Home` VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            // Only add the duplicate key update if desired
            if( update ) {
                query += "ON DUPLICATE KEY UPDATE x = VALUES(x), y = VALUES(y)," +
                "z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)," +
                "world = VALUES(world)";
            }

            database.executeUpdate( query, data );
            return ActionResult.OK;
        } catch ( SQLIntegrityConstraintViolationException e2 ) {
            return ActionResult.ALREADY_EXISTENT;
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while writing homes!" );
            ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
            return ActionResult.INTERNAL_ERROR;
        }
    }

    /**
     * Delete homes from the database
     * @param conn Connection to prepare statement on
     * @param elements Array of elements
     */
    @Override
    public ActionResult delete( Connection conn, Home... elements ) {
        // No items provided
        if( elements.length == 0 )
            return ActionResult.NO_DATA;

        try {
            // * 2 since every element has 2 fields that make up it's key
            Object[] data = new Object[ elements.length * 2 ];
            StringBuilder query = new StringBuilder( "DELETE FROM `Home` WHERE " );

            // Loop elements to delete
            int dataPointer = 0;
            for( int i = 0; i < elements.length; i++ ) {
                // Append or clause for every element after the first
                if( i != 0 )
                    query.append( "OR" );

                // Append item's condition and parameters
                query.append( "( `name` = ? AND `uuid` = ? )" );
                data[ dataPointer++ ] = elements[ i ].getName();
                data[ dataPointer++ ] = elements[ i ].getPlayerUUID().toString();
            }

            // If no rows were affected, data didn't exist
            if( database.executeUpdate( query.toString(), data ) == 0 )
                return ActionResult.NON_EXISTENT;

            // Data has been deleted
            return ActionResult.OK;
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while deleting homes!" );
            ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
            return ActionResult.INTERNAL_ERROR;
        }
    }

    /**
     * Create the home table if it doesn't yet exist
     */
    @Override
    public void buildTable() throws Exception {
        database.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `Home` (" +
            "`name` VARCHAR(128) NOT NULL," +
            "`uuid` VARCHAR(40) NOT NULL," +
            "x DOUBLE NOT NULL," +
            "y DOUBLE NOT NULL," +
            "z DOUBLE NOT NULL," +
            "yaw FLOAT NOT NULL," +
            "pitch FLOAT NOT NULL," +
            "world TEXT NOT NULL," +
            "PRIMARY KEY (`name`, `uuid`) )"
        );
    }
}
