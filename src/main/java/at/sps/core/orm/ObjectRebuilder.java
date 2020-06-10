package at.sps.core.orm;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ObjectRebuilder< T > {

  private final LinkedList< Object > args;
  private final ResultSet rs;
  private final Class< ? > clazz;

  /**
   * Rebuild an object based on a list of arguments merged from
   * custom passed ones and resultset columns, specified by
   * {@link #addColumns(String...)} and {@link #addArgs(Object...)} in
   * the exact order of the target constructor. The best matching constructor will
   * be searched (null values are also thought of here), which will then be
   * invoked. If 0 or > 1 results for constructors return, this has to abort
   * @param clazz Class type to build (can't get generic type T as class :/)
   * @param rs ResultSet from database to get columns from
   */
  public ObjectRebuilder( Class< ? > clazz, ResultSet rs ) {
    this.args = new LinkedList<>();
    this.rs = rs;
    this.clazz = clazz;
  }
  /**
   *
   * Add custom objects as arguments, have to be in the
   * right order, also with {@link #addColumns(String...)}
   * @param args List of objects to add
   */
  public ObjectRebuilder< T > addArgs( Object... args ) {
    this.args.addAll( Arrays.asList( args ) );
    return this;
  }

  /**
   * Add columns from the result set as arguments, have to be in
   * the right order, also with {@link #addArgs(Object...)}
   * @param args Names of columns to add
   */
  public ObjectRebuilder< T > addColumns( String... args ) {
    try {
      for( String arg : args )
        this.args.add( this.rs.getObject( arg ) );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return this;
  }

  /**
   * Build the object based on the provided arguments and the best
   * matching constructor, if one exists
   * @return Built object on success, null otherwise
   */
  @SuppressWarnings( "unchecked" )
  public T build() {
    try {
      // Try to find matching constructor
      Constructor< ? > con = findConstructor();
      if( con == null )
        throw new Exception( "Could not find valid constructor for " + clazz.getSimpleName() + "!" );

      // Make object with arguments and return it
      Object[] argsArr = new Object[ args.size() ];
      argsArr = args.toArray( argsArr );
      return ( T ) con.newInstance( argsArr );
    } catch ( Exception e ) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Finds the matching constructor for the target class based on
   * the provided parameters and internal criteria. When more than one
   * constructors would match, an error is thrown
   * @return Constructor if found, null otherwise
   */
  private Constructor< ? > findConstructor() {
    try {
      // Create class array from object arguments
      Class< ? >[] types = new Class< ? >[ args.size() ];
      for( int i = 0; i < args.size(); i++ )
        types[ i ] = args.get( i ) == null ? null : args.get( i ).getClass();

      // Loop all constructors for the target class
      List< Constructor< ? > > valids = new ArrayList<>();
      for( Constructor< ? > cnstr : clazz.getConstructors() ) {

        // Not same length of parameters as args provided
        if( cnstr.getParameterCount() != types.length )
          continue;

        // Check for matching based on criteria
        boolean isMatching = true;
        for( int i = 0; i < types.length; i++ ) {

          // Criteria 1: Null is only allowed where no primitives are found
          if( types[ i ] == null ) {
            if( cnstr.getParameters()[ i ].getClass().isPrimitive() ) {
              isMatching = false;
              break;
            }
          }

          // Criteria 2: Arguments at same index must be assignable from one another
          else if( types[ i ].isAssignableFrom( cnstr.getParameters()[ i ].getType() ) ) {
            isMatching = false;
            break;
          }
        }

        // If it matches, add to buffer list
        if( isMatching )
          valids.add( cnstr );
      }

      // When more than one constructor match (should literally never be the case), abort
      if( valids.size() > 1 )
        throw new Exception( "Found more than one valid constructor, cannot decide!" );

      // Return found constructor or null of none found
      return valids.size() > 0 ? valids.get( 0 ) : null;
    } catch ( Exception e ) {
      e.printStackTrace();
      return null;
    }
  }
}
