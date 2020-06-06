package at.sps.dao;

import at.sps.core.ConsoleLogger;
import at.sps.core.Main;
import at.sps.core.utils.Pair;
import at.sps.core.utils.Utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class DaoBase< T > {

  private String database, table;

  /**
   * This is the base of all Daos for SQL databases, can
   * perform r/w based on @{@link DaoField} and @{@link DaoInternalFields} annotations
   * @param database Database for data
   * @param table Table for data
   */
  public DaoBase( String database, String table ) {
    this.database = database;
    this.table = table;
  }

  private Map< String, Object > extractData( T data ) {
    try {
      Field[] fields = data.getClass().getDeclaredFields();
      Map< String, Object > values = new HashMap<>();

      // Loop all found fields
      for( Field cf : fields ) {
        // Get both possible annotations
        DaoField fieldDesc = cf.getAnnotation( DaoField.class );
        DaoInternalFields internalDesc = cf.getAnnotation( DaoInternalFields.class );

        // Single field descripted
        if( fieldDesc != null ) {
          values.put( fieldDesc.storageName(), cf.get( data ) );
          continue;
        }

        // Internal field specified, append those values
        for( String in : internalDesc.fields() ) {
          Object inVal = cf.getClass().getDeclaredField( in ).get( cf.get( data ) );
          values.put( in, inVal );
        }
      }

      return values;
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while extracting from DAO!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return null;
    }
  }

  /**
   * Write the object based on it's annotations into DB
   * @param data Data to write
   * @return True on success, false otherwise
   */
  protected boolean write( T data ) {
    try {
      Map< String, Object > values = extractData( data );
      assert values != null;

      // Build insert query
      StringBuilder query = new StringBuilder( "USE `" + this.database + "`;" );
      query.append( "INSERT INTO `" ).append( this.table ).append( "` VALUES (?" );

      // Append a questionmark for every value, -1 since first is already inserted above
      for( int i = 0; i < values.size() - 1; i++ )
        query.append( ", ?" );

      // Append VALUES closing bracket
      query.append( ")" );

      // Insert data to DB
      return Main.getDb().insertData( query.toString(), values.values() );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while writing in DAO!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return false;
    }
  }

  /**
   * Read objects from database, based on the specified filters
   * @param filter List of Pairs, where a Pair contains the field name and it's target value
   * @return ResultSet to then later create objects from
   */
  protected ResultSet read( List< Pair< String, Object > > filter ) {
    StringBuilder query = new StringBuilder( "USE `" + this.database + "`;" );
    query.append( "SELECT * FROM `" ).append( this.table ).append( "` " );

    // More than one filter got applied
    if( filter.size() > 0 ) {
      String first = filter.get( 0 ).getKey();
      query.append( "WHERE `" ).append( first ).append( "` = ?" );

      // Apply further filters
      for( int i = 1; i < filter.size(); i++ )
        query.append( " AND `" ).append( filter.get( i ).getKey() ).append( "` = ?" );
    }

    // Execute query and return result
    List< Object > data = filter.stream().map( Pair::getValue ).collect( Collectors.toList() );
    return Main.getDb().readData( query.toString(), data );
  }
}
