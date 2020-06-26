package at.sps.core.orm.mappers;

import at.sps.core.Main;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.models.Ban;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BanMapper extends ModelMapper< Ban > {

  private static BanMapper inst;

  /**
   * The ban-mapper is used to create, delete update and list bans
   * @param database Connection to the SQL database for r/w
   */
  @SuppressWarnings( { "unchecked" } )
  private BanMapper( MariaDB database ) {
    super( database );
    inst = this;

    // Register a translator for the IP-List to convert it into CSV
    registerTranslator( "ipAddresses", val -> String.join( ";", ( List< String > ) val ) );
  }

  /**
   * List all bans for a specific player
   * @param target Target UUID to look for
   * @return List of bans
   */
  public List< Ban > listBans( UUID target ) {
    try {
      // Fetch result to get only the target player's bans
      return read( database.fetchResult(
        "SELECT * FROM `Ban`" +
        "WHERE `holder` = ?",
        target.toString()
      ) );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while listing bans!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return new ArrayList<>();
    }
  }

  /**
   * Read bans from the database
   * @param rs ResultSet from an executed query
   * @return List of generated bans
   */
  @Override
  public List< Ban > read( ResultSet rs ) {
    List< Ban > buf = new ArrayList<>();

    try {
      // Loop result rows
      while( rs.next() ) {
        // Fetch data and generate Ban object into buffer list
        UUID holder = UUID.fromString( rs.getString( "holder" ) );
        UUID creator = UUID.fromString( rs.getString( "creator" ) );
        List< String > ips = Arrays.asList( rs.getString( "ipAddresses" ).split( ";" ) );
        Ban ban = new Ban( holder, creator, rs.getString( "reason" ), rs.getLong( "creationDate" ), rs.getLong( "expireDate" ), rs.getBoolean( "permanent" ), ips );
        bindID( ban, rs );

        buf.add( ban );
      }
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while mapping read bans!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }

    return buf;
  }

  /**
   * Singleton instance getter of the ban mapper
   * @return BanMapper instance
   */
  public static BanMapper getInst() {
    if( inst == null )
      return new BanMapper( Main.getDb() );

    return inst;
  }
}
