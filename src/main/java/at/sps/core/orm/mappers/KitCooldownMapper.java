package at.sps.core.orm.mappers;

import at.sps.core.Main;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.models.Kit;
import at.sps.core.orm.models.KitCooldown;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitCooldownMapper extends ModelMapper< KitCooldown > {

  private static KitCooldownMapper inst;

  /**
   * The kit cooldown mapper is used to keep track of cooldowns per
   * player of existing kits
   */
  public KitCooldownMapper( MariaDB database ) {
    super( database );
    inst = this;

    registerTranslator( UUID.class, Object::toString );
  }

  /**
   * Insert or update the given kit cooldown
   * @param cooldown Cooldown object
   */
  public void saveCooldown( KitCooldown cooldown ) {
    write( cooldown, true );
  }

  /**
   * Get the cooldown for a player and the specified kit
   * @param uuid UUID of the executing player
   * @param target Target kit to get the cooldown from
   * @return KitCooldown if exists, null otherwise
   */
  public KitCooldown getForPlayer( UUID uuid, Kit target ) {
    try {
      // Fetch result to get only the target player's cooldown for the specific kit
      List< KitCooldown > result = read( database.fetchResult(
          "SELECT * FROM `KitCooldown`" +
          "WHERE `holder` = ? AND `kitID` = ?",
          uuid.toString(), target.getID()
      ) );

      // There can just be one result since UUID & ID are the key
      return result.size() > 0 ? result.get( 0 ) : null;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while searching for a kitcooldown by it's uuid and kitID!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * Read kitcooldowns from the database
   * @param rs ResultSet from an executed query
   * @return List of generated kits
   */
  @Override
  public List< KitCooldown > read( ResultSet rs ) {
    List< KitCooldown > buf = new ArrayList<>();

    try {
      // Loop result rows
      while( rs.next() ) {
        UUID holder = UUID.fromString( rs.getString( "holder" ) );
        KitCooldown cooldown = new KitCooldown( holder, rs.getInt( "kitID" ), rs.getLong( "lastUse" ) );
        bindID( cooldown, rs );

        buf.add( cooldown );
      }
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while mapping read kitcooldowns!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }

    return buf;
  }

  /**
   * Singleton instance getter for kit cooldown mapper
   */
  public static KitCooldownMapper getInst() {
    if( inst == null )
      return new KitCooldownMapper( Main.getDb() );

    return inst;
  }
}
