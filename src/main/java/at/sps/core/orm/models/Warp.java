package at.sps.core.orm.models;

import at.sps.core.orm.MappableModel;
import at.sps.core.orm.MapperColumn;
import at.sps.core.orm.RebuilderColumns;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Warp extends MappableModel {

  @Getter @Setter
  @MapperColumn( unique = true, length = "128" )
  private String name;

  @Getter @Setter
  @RebuilderColumns( fields = { "x", "y", "z", "yaw", "pitch", "world" } )
  private Location location;

  @Getter @Setter
  @MapperColumn
  private long creationDate;

  @Getter @Setter
  @MapperColumn( length = "40" )
  private UUID creator;

  /**
   * Create a new home based on the holder's uuid, a name and the pre-existing
   * bukkit location object
   * @param name Name of the home
   * @param location Bukkit location of the home
   * @param creationDate Timestamp of creation
   * @param creator UUID of the player who created the warp
   */
  public Warp( String name, Location location, long creationDate, UUID creator ) {
    this.name = name;
    this.location = location;
    this.creationDate = creationDate;
    this.creator = creator;
  }

  // NOTE: Only used for debugging purposes!
  @Override
  public String toString() {
    SimpleDateFormat df = new SimpleDateFormat( "" );
    String world = location.getWorld() == null ? "null" : location.getWorld().getName();
    return "Warp{" +
            creator + ", " + name + ", " + df.format( new Date( creationDate ) ) + ", " +
            location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " +
            location.getYaw() + ", " + location.getPitch() + ", " + world +
            "}";
  }
}
