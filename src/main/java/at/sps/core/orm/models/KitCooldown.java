package at.sps.core.orm.models;

import at.sps.core.orm.MappableModel;
import at.sps.core.orm.MapperColumn;
import at.sps.core.orm.mappers.KitMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class KitCooldown extends MappableModel {

  @Getter @Setter
  @MapperColumn( unique = true, length = "40" )
  private UUID holder;

  @Getter @Setter
  @MapperColumn( unique = true, length = "32" )
  private int kitID;

  @Getter @Setter
  @MapperColumn
  private long lastUse;

  /**
   * Create a new kit cooldown for a specific player targetting a specific kit by it's ID
   * @param holder UUID of the player using the kit
   * @param kitID ID of the kit which cooldown is going to be targetted here
   * @param lastUse Timestamp of the last use from this kit
   */
  public KitCooldown( UUID holder, int kitID, long lastUse ) {
    this.holder = holder;
    this.kitID = kitID;
    this.lastUse = lastUse;
  }

  /**
   * Get the remainder of this cooldown instance
   * @return Remaining time in ms or null if the kit doesn't exist anymore
   */
  public Long getRemainder() {
    Kit target = KitMapper.getInst().getById( kitID );

    // This kit has not been found, thus return null
    if( target == null )
      return null;

    // Calculate elapsed time and get cooldown long
    long cooldown = target.getCooldown();
    long elapsed = System.currentTimeMillis() - lastUse;

    // Return the remainder constraint to 0 towards negative
    return Math.max( 0, cooldown - elapsed );
  }
}
