package at.sps.core.storage;

import at.sps.dao.HomeDao;
import at.sps.model.Home;

import java.util.List;
import java.util.UUID;

public class DBHomeDAO implements HomeDao {

  // ToDo: Implement functionallity

  @Override
  public boolean addHome( Home home ) {
    return false;
  }

  @Override
  public boolean removeHome( UUID playerId, String name ) {
    return false;
  }

  @Override
  public Home getByName( UUID playerId, String name ) {
    return null;
  }

  @Override
  public List< Home > listHomes( UUID playerId ) {
    return null;
  }
}
