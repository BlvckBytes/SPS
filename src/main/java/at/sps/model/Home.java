package at.sps.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.UUID;

public class Home {

    @Getter
    @Setter
    private UUID playerUUID;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
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
