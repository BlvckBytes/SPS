package at.sps.core.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public interface ModelMapper< T > {

  /**
   * Read a list of T typed elements from DB
   * @param rs ResultSet from an executed query
   * @return List of elements
   */
  List< T > read( ResultSet rs );

  /**
   * Writes a list of T typed elements into the database
   * @param conn Connection to prepare statement on
   * @param element Element to write or update
   * @param update Whether or not to use the updating feature
   * @return True on query success, false otherwise
   */
  ActionResult write( Connection conn, T element, boolean update );

  /**
   * Delete a list of T typed elements from the database
   * @param conn Connection to prepare statement on
   * @param elements Array of elements
   * @return True on query success, false otherwise
   */
  ActionResult delete( Connection conn, T... elements );

  /**
   * Gets called from the database object to build the table
   * needed for this model
   * @throws Exception Error on execution
   */
  void buildTable() throws Exception;
}
