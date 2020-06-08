package at.sps.model;

import at.sps.core.utils.ObjectRebuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Home {

    @Getter
    private UUID playerUUID;

    @Getter
    private String name;

    @Getter
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
        return "Home{" + playerUUID + ", " + name + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getYaw() + ", " + location.getPitch() + ", " + world + "}";
    }

    public static List< Home > read( ResultSet rs ) throws Exception {
        List< Home > buf = new ArrayList<>();

        while( rs.next() ) {
            World w = Bukkit.getWorld( rs.getString( "world" ) );
            Location loc = new ObjectRebuilder< Location >( Location.class, rs )
                    .addArgs( w )
                    .addColumns( "x", "y", "z", "yaw", "pitch" )
                    .build();

            UUID uu = UUID.fromString( rs.getString( "uuid" ) );
            buf.add( new Home( uu, rs.getString( "name" ), loc ) );
        }

        return buf;
    }
}
