package at.sps.dao;

import at.sps.model.Home;

import java.util.List;
import java.util.UUID;

public interface HomeDao {
    int addHome( Home home );

    int removeHome( UUID playerId, String name );

    Home getByName( String name );

    List<Home> listHomes();
}
