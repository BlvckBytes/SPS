package at.sps.dao;

import at.sps.model.Home;

import java.util.List;
import java.util.UUID;

public interface HomeDao {

    /**
     * Add a new home to the storage
     * @param home Home to add to database
     * @return True if worked, false otherwise (already existent)
     */
    boolean addHome( Home home );

    /**
     * Deletes a home from an player-account based on it's name
     * @param playerId Home owner ID
     * @param name Name of the home
     * @return True if worked, false otherwise (non-existent)
     */
    boolean removeHome( UUID playerId, String name );

    /**
     * Get a home by it's name from a player
     * @param playerId Home owner ID
     * @param name Name of the home
     * @return Home if found, null otherwise
     */
    Home getByName( UUID playerId, String name );

    /**
     * List all homes for a specific player
     * @param playerId Home owner ID
     * @return List of his homes
     */
    List<Home> listHomes( UUID playerId );
}
