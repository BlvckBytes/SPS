package at.sps.model;

import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

public class Home {

    @Getter
    private final UUID playerUUID;

    @Getter
    private final String name;

    @Getter
    private final Location location;

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
}
