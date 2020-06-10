package at.sps.core.orm.mappers;

import at.sps.core.ConsoleLogger;
import at.sps.core.Main;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.ObjectRebuilder;
import at.sps.core.orm.ActionResult;
import at.sps.core.utils.ParamFuncCB;
import at.sps.core.utils.Utils;
import at.sps.core.orm.models.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

public class HomeMapper extends ModelMapper< Home > {

    private static HomeMapper inst;

    /**
     * The home-mapper is used to add, delete, search and list player homes
     * @param database Connection to the SQL database for r/w
     */
    private HomeMapper( MariaDB database ) {
        super( database );
        inst = this;

        // Register world translator to get the world's name, UUID translator to perform toString
        registerTranslator( World.class, val -> ( ( World ) val ).getName() );
        registerTranslator( UUID.class, Object::toString );
    }

    /**
     * Add a new home to the storage
     * @param home Home to add to database
     * @return ActionResult which tells the status
     */
    public ActionResult addHome( Home home ) {
        return write( home, false );
    }

    /**
     * Deletes a home from an player-account based on it's name
     * @param playerId Home owner ID
     * @param name Name of the home
     * @return ActionResult which tells the status
     */
    public ActionResult removeHome( UUID playerId, String name ) {
        return delete( Collections.singletonList( getByName( playerId, name ) ) );
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
            return new ArrayList<>();
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
                Home home = new Home( uu, rs.getString( "name" ), loc );

                // Set ID for later manipulation with DB
                home.setID( rs.getInt( "ID" ) );

                buf.add( home );
            }
        } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while mapping read homes!" );
            ConsoleLogger.getInst().logMessage( Utils.stringifyException( e ) );
        }

        return buf;
    }

    /**
     * Singleton getter of the home mapper
     * @return HomeMapper object
     */
    public static HomeMapper getInst() {
        if( inst == null )
            return new HomeMapper( Main.getDb() );

        return inst;
    }
}
