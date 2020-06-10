package at.sps.core.orm.models;

import at.sps.core.orm.MappableModel;
import at.sps.core.orm.MapperColumn;
import at.sps.core.orm.RebuilderColumns;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.UUID;

public class Home extends MappableModel {

    @Getter @Setter
    @MapperColumn( unique = true, length = "128" )
    private String name;

    @Getter @Setter
    @MapperColumn( name = "uuid", unique = true, length = "40" )
    private UUID playerUUID;

    @Getter @Setter
    @RebuilderColumns( fields = { "x", "y", "z", "yaw", "pitch", "world" } )
    private Location location;

    /**
     * Create a new home based on the holder's uuid, a name and the pre-existing
     * bukkit location object
     * @param playerUUID Home-owner's UUID
     * @param name Name of the home
     * @param location Bukkit location of the home
     */
    public Home( UUID playerUUID, String name, Location location ) {
        this.playerUUID = playerUUID;
        this.name = name;
        this.location = location;
    }

    // NOTE: Only used for debugging purposes!
    @Override
    public String toString() {
        String world = location.getWorld() == null ? "null" : location.getWorld().getName();
        return "Home{" +
                playerUUID + ", " + name + ", " +
                location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " +
                location.getYaw() + ", " + location.getPitch() + ", " + world +
        "}";
    }
}
