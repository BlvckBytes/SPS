package at.sps.core.storage;

import at.sps.core.ConsoleLogger;
import at.sps.core.utils.Pair;
import at.sps.core.utils.Utils;
import at.sps.dao.DaoBase;
import at.sps.dao.HomeDao;
import at.sps.model.Home;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DBHomeDAO extends DaoBase< Home > implements HomeDao {

  public DBHomeDAO() {
    super( "sps", "home" );
  }

  @Override
  public boolean addHome( Home home ) {
    return write( home );
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
    List< Pair< String, Object > > filters = new ArrayList<>();
    filters.add( new Pair<>( "uuid", playerId.toString() ) );
    return fromResult( read( filters ) );
  }

  private List< Home > fromResult( ResultSet rs ) {
    List< Home > buf = new ArrayList<>();

    try {
      while( rs.next() ) {

      }
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while building Home from data in DAO!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }

    return buf;
  }
}
