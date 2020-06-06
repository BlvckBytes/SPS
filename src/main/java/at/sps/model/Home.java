package at.sps.model;

import at.sps.dao.DaoField;
import at.sps.dao.DaoInternalFields;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

public class Home {

    @Getter
    @DaoField( storageName = "uuid" )
    private final UUID playerUUID;

    @Getter
    @DaoField
    private final String name;

    @Getter
    @DaoInternalFields( fields = { "x", "y", "z", "yaw", "pitch" } )
    private final Location location;

    @Getter
    @DaoField
    private final String world;

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

        // Internal only fields, used for the Dao
        this.world = location.getWorld().getName();
    }
}
