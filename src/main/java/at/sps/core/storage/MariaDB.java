package at.sps.core.storage;

import at.sps.core.ConsoleLogger;
import at.sps.core.utils.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class MariaDB {

  private Connection conn;
  private final String username, password;

  /**
   * A new MySQL / MariaDB connection instance
   * @param username Username for auth
   * @param password Password for auth
   */
  public MariaDB( String username, String password ) {
    this.username = username;
    this.password = password;
  }

  /**
   * Establish a connection to the database
   */
  public void connect() {
    try {
      Class.forName( "com.mysql.jdbc.Driver" );
      this.conn = DriverManager.getConnection( "jdbc:mysql://localhost:3306", this.username, this.password );

      ConsoleLogger.getInst().logMessage( "&aSuccessfully connected to database!" );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while connecting to database!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
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
  private PreparedStatement prepareQuery( String query, Collection< Object > data ) {
    try {
      PreparedStatement ps = this.conn.prepareStatement( query );

      // Set all questionmark's values in query
      int c = 1;
      for( Object curr : data ) {
        ps.setObject( c, curr );
        c++;
      }

      return ps;
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while preparing database query!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return null;
    }
  }

  /**
   * Insert data into the database using a query and it's data
   * @param query Query to insert
   * @param data Data to use for questionmarks
   * @return True on success, false otherwise
   */
  public boolean insertData( String query, Collection< Object > data ) {
    try {
      PreparedStatement ps = prepareQuery( query, data );
      assert ps != null;

      ps.executeUpdate();
      return true;
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while inserting into database!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return false;
    }
  }
  /**
   * Insert data from the database using a query and it's data
   * @param query Query to insert
   * @param data Data to use for questionmarks
   * @return True on success, false otherwise
   */
  public ResultSet readData( String query, Collection< Object > data ) {
    try {
      PreparedStatement ps = prepareQuery( query, data );
      assert ps != null;

      return ps.executeQuery();
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while reading from database!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
      return null;
    }
  }
}
