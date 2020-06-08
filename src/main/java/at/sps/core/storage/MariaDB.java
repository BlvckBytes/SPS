package at.sps.core.storage;

import at.sps.core.ConsoleLogger;
import at.sps.core.utils.Utils;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MariaDB {

  @Getter
  private Connection conn;
  private final String username, password, database;

  /**
   * A new MySQL / MariaDB connection instance
   * @param username Username for auth
   * @param password Password for auth
   */
  public MariaDB( String username, String password, String database ) {
    this.username = username;
    this.password = password;
    this.database = database;
  }

  /**
   * Establish a connection to the database
   */
  public void connect() {
    try {
      // Load driver and build conn-string
      Class.forName( "com.mysql.jdbc.Driver" );
      String url = "jdbc:mysql://localhost:3306/" + this.database +
              "?useUnicode=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

      // Open connection
      this.conn = DriverManager.getConnection( url, this.username, this.password );
      ConsoleLogger.getInst().logMessage( "&aSuccessfully connected to database!" );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while connecting to database!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }

  /**
   * Build all tables for the specified mappers if they don't yet exist
   * @param mappers Array of mappers to request table creation from
   */
  public void buildTables( ModelMapper< ? >... mappers ) {
    // Build tables of all mappers
    for( ModelMapper< ? > mapper : mappers ) {
      try {
        mapper.buildTable();
      } catch ( Exception e ) {
        String name = mapper.getClass().getSimpleName();
        ConsoleLogger.getInst().logMessage( "&cCould not create table for " + name + "!" );
        ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      }
    }
  }

  /**
   * Disconnect from the database
   */
  public void disconnect() {
    try {
      // Not connected
      if( conn == null || conn.isClosed() )
        return;

      conn.close();
      ConsoleLogger.getInst().logMessage( "&aSuccessfully disconnected from database!" );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while disconnecting from database!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }

  /**
   * Create a prepared statement from a string query with it's data
   * @param query Query to insert
   * @param data Data to use for questionmarks
   * @return PreparedStatement on success, null otherwise
   */
  private PreparedStatement prepareQuery( String query, Object... data ) throws Exception {
    PreparedStatement ps = this.conn.prepareStatement( query );

    // Set all questionmark's values in query
    if( data != null ) {
      int c = 1;
      for ( Object curr : data ) {
        ps.setObject( c, curr );
        c++;
      }
    }

    return ps;
  }

  /**
   * Insert data into the database using a query and it's data
   * @param query Query to insert
   * @param data Data to use for questionmarks
   * @return Number of affected rows
   */
  public int executeUpdate( String query, Object... data ) throws Exception {
    PreparedStatement ps = prepareQuery( query, data );
    assert ps != null;

    return ps.executeUpdate();
  }
  /**
   * Insert data from the database using a query and it's data
   * @param query Query to insert
   * @param data Data to use for questionmarks
   * @return True on success, false otherwise
   */
  public ResultSet fetchResult( String query, Object... data ) throws Exception {
    PreparedStatement ps = prepareQuery( query, data );
    assert ps != null;

    return ps.executeQuery();
  }
}
