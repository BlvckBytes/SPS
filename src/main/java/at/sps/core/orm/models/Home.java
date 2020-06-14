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

    @Getter @Setter
    @MapperColumn
    private long creationDate;

    /**
     * Create a new home based on the holder's uuid, a name and the pre-existing
     * bukkit location object
     * @param playerUUID Home-owner's UUID
     * @param name Name of the home
     * @param location Bukkit location of the home
     * @param creationDate Time stamp of creation
     */
    public Home( UUID playerUUID, String name, Location location, long creationDate ) {
        this.playerUUID = playerUUID;
        this.name = name;
        this.location = location;
        this.creationDate = creationDate;
    }
}
