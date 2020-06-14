package at.sps.core.orm;

import at.sps.core.utils.LogLevel;
import at.sps.core.utils.ParamFuncCB;
import at.sps.core.utils.SLogging;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ModelMapper< T extends MappableModel > {

  protected final MariaDB database;
  private final Map< Class< ? >, ParamFuncCB< Object, String > > writeTranslatorsC;
  private final Map< String, ParamFuncCB< Object, String > > writeTranslatorsN;
  private LinkedList< ColInfo > tableFields;

  /**
   * Create a new model mapper, the base construct of every mapper used. Delete is being
   * performed completely on an automatic basis, write may need the help of write translators.
   * Read can get too complicated and is thus forced to be implemented by the model itself.
   * @param database Database instance for queries
   */
  public ModelMapper( MariaDB database ) {
    this.database = database;
    this.writeTranslatorsC = new HashMap<>();
    this.writeTranslatorsN = new HashMap<>();

    // Fetch a list of ColInfo objects for mapping later on, used multiple times
    try {
      tableFields = findFields( getModelClass() );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while trying to flatten a model structure!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }
  }

  /**
   * Read a list of T typed elements from DB
   * @param rs ResultSet from an executed query
   * @return List of elements
   */
  public abstract List< T > read( ResultSet rs );

  /**
   * Writes a list of T typed elements into the database
   * @param element Element to write or update
   * @param update Whether or not to use the updating feature
   * @return ActionResult with the exit state
   */
  public ActionResult write( T element, boolean update ) {
    // No data provided
    if( element == null )
      return ActionResult.NO_DATA;

    try {
      // Create an array of the element's row values, -1 for primary key, it's auto-gen
      Object[] data = new Object[ tableFields.size() - 1 ];

      // Create list of questionmark placeholders for tablefields
      String name = getModelClass().getSimpleName();
      StringBuilder query = new StringBuilder( "INSERT INTO `" + name + "` (" );

      // Append col names and build values placeholders simultaneously
      StringBuilder values = new StringBuilder( " VALUES (" );
      int c = 0;
      for( int i = 0; i < tableFields.size(); i++ ) {
        ColInfo curr = tableFields.get( i );

        // ID is auto gen, never insert that
        if( tableFields.get( i ).getName().equalsIgnoreCase( "id" ) )
          continue;

        values.append( c == 0 ? "" : "," ).append( "?" ).append( c == tableFields.size() - 2 ? ")" : "" );
        query.append( c == 0 ? "" : "," ).append( curr.getName() ).append( c == tableFields.size() - 2 ? ")" : "" );

        // If the value couldn't be found, throw an exception, dev error
        Object d = getFieldValue( element, curr.getTarget() );
        assert d != null;

        // Append to data array
        data[ c ] = tryTranslate( curr.getTarget(), d );
        c++;
      }

      // Append values placeholders
      query.append( values.toString() );

      // Only add the duplicate key update if desired
      if( update ) {
        query.append( " ON DUPLICATE KEY UPDATE " );
        c = 0;
        for( ColInfo ci : tableFields ) {

          // Update doesn't update primary key
          if( ci.getName().equalsIgnoreCase( "id" ) )
            continue;

          // Append field update clause
          query.append( c == 0 ? "" : ", " ).append( ci.getName() ).append( " = VALUES(" ).append( ci.getName() ).append( ")" );
          c++;
        }
      }

      database.executeUpdate( query.toString() + ";", data );
      return ActionResult.OK;
    } catch ( SQLIntegrityConstraintViolationException e2 ) {
      // When constraints get violated, it should always mean that it's a duplicate update or insert, since
      // the only constraint auto-created is the key constraint
      return ActionResult.ALREADY_EXISTENT;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while writing a model to the SQL-DB!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return ActionResult.INTERNAL_ERROR;
    }
  }

  /**
   * Try to translate an object for writing
   * @param field Field that contains this, for name based translation
   * @param input Object to translate
   * @return Translated object if translator existent, input otherwise
   */
  private Object tryTranslate( Field field, Object input ) {
    // Try to find the corresponding translator
    for( Class< ? > key : writeTranslatorsC.keySet() ) {
      if( !key.isAssignableFrom( input.getClass() ) )
        continue;

      // Translate to string
      return writeTranslatorsC.get( key ).call( input );
    }

    // Try to find a field name based translator
    String name = field.getName();
    if( writeTranslatorsN.containsKey( name ) )
      return writeTranslatorsN.get( name ).call( input );

    // No translator found
    return input;
  }

  /**
   * Bind the ID of a resultset to the created object using reflect, since
   * the ID-field only offers a getter for integrity
   * @param object Object to bind ID to
   * @param rs ResultSet to get ID from
   */
  protected void bindID( T object, ResultSet rs ) {
    try {
      // Get ID value from result and field from superclass
      int id = rs.getInt( "ID" );
      Field targetField = object.getClass().getSuperclass().getDeclaredField( "ID" );

      // Bind value onto field
      targetField.setAccessible( true );
      targetField.set( object, id );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while binding the ID on an object!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }
  }

  /**
   * Get a fields value within an object, this searches recursively
   * @param element Element to search in
   * @param field Target field
   * @return Value of field if found, null otherwise
   */
  private Object getFieldValue( Object element, Field field ) {
    try {
      // Loop all fields from the holder and make them accessible
      for( Field f : element.getClass().getDeclaredFields() ) {
        f.setAccessible( true );

        // Field found on current level
        if( f.equals( field ) )
          return f.get( element );

        // Not annotated, ignore since recursions could go wild otherwise
        if( !( f.getDeclaredAnnotation( MapperColumn.class ) != null || f.getDeclaredAnnotation( RebuilderColumns.class ) != null ) )
          continue;

        // Primitive, can't navigate into it
        if( f.getType().isPrimitive() )
          continue;

        // Search on that element's level, return only if retrieved value isn't null
        Object ret = getFieldValue( f.get( element ), field );
        if( ret != null )
          return ret;
      }
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while trying to get a field's value!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }

    // Nothing found
    return null;
  }

  /**
   * Delete a list of T typed elements from the database
   * @param elements Array of elements
   * @return True on query success, false otherwise
   */
  public ActionResult delete( List< T > elements ) {
    // No items provided
    if( elements == null || elements.size() == 0 )
      return ActionResult.NO_DATA;

    try {
      // Get name and unique fields only
      Class< ? > modelClass = getModelClass();
      String name = modelClass.getSimpleName();
      List< ColInfo > keys = tableFields.stream().filter( ColInfo::isUnique ).collect( Collectors.toList() );

      // Build query and corresponding data buffer
      LinkedList< Object > data = new LinkedList<>();
      StringBuilder query = new StringBuilder( "DELETE FROM `" + name + "` WHERE " );

      // Loop elements to delete
      int added = 0;
      for( int i = 0; i < elements.size(); i++ ) {
        T currE = elements.get( i );

        // Don't delete null values, obviously...
        if( currE == null )
          continue;

        // Append or clause for every element after the first
        if( i != 0 )
          query.append( "OR" );

        // Append item's condition and parameters
        query.append( "( `ID` = ?" );

        // Append primary key, then all key values
        data.add( currE.getID() );
        for ( ColInfo key : keys ) {
          query.append( " AND `" ).append( key.getName() ).append( "` = ?" );
          data.add( tryTranslate( key.getTarget(), key.getTarget().get( currE ) ) );
        }

        // Implemented deletion element
        added++;
      }

      // Close query
      query.append( ");" );

      // If no rows were affected, data didn't exist, otherwise at least something got deleted
      if( added == 0 || database.executeUpdate( query.toString(), data.toArray( new Object[ 0 ] ) ) == 0 )
        return ActionResult.NON_EXISTENT;

      // Data has been deleted
      return ActionResult.OK;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while deleting model entries from SQL-DB!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return ActionResult.INTERNAL_ERROR;
    }
  }

  /**
   * Gets called from the database object to build the table needed for
   * this model, builds it automatically based on the annotated fields
   * @throws Exception Error on execution
   */
  public void buildTable() throws Exception {
    // Get fields, model name and begin building query
    String name = getModelClass().getSimpleName();
    StringBuilder query = new StringBuilder( "CREATE TABLE IF NOT EXISTS `" + name + "` (" );

    // Add primary key ID, since that'll be always present
    query.append( "`ID` INT(32) NOT NULL AUTO_INCREMENT, PRIMARY KEY(`ID`)" );

    // Build key constraint simultaneously
    List< String > colNames = new ArrayList<>();
    StringBuilder keys = new StringBuilder( ", CONSTRAINT UC_" + name + " UNIQUE (" );
    int keyC = 0;

    // Loop the table's fields
    for( ColInfo ci : tableFields ) {

      // Skip ID field, since that has been specified above in a more particular way
      if( ci.getName().equalsIgnoreCase( "id" ) )
        continue;

      // Unique -> Add to key constraint
      if( ci.isUnique() ) {
        keys.append( keyC == 0 ? "" : "," ).append( ci.getName() );
        keyC++;
      }

      // Check for duplicate column names
      if( colNames.contains( ci.getName().toLowerCase() ) )
        throw new IllegalStateException( "Found duplicate column names! Beware that the struct gets flattened and names need to be unique!" );

      // Append signature
      query.append( ", " ).append( makeColumn( ci.getTarget(), ci.getName(), ci.getLength() ) );
      colNames.add( ci.getName().toLowerCase() );
    }

    // Append key constrain if exists
    if( keyC > 0 ) {
      keys.append( ")" );
      query.append( keys.toString() );
    }

    // Close query
    query.append( ");" );

    // Execute the query
    database.executeUpdate( query.toString() );
  }

  /**
   * Get the class of the currently implemented model
   * @return Model's class
   */
  protected Class< ? > getModelClass() {
    // Get the model's class (the type which the extending class has passed in type args)
    ParameterizedType superType = ( ( ParameterizedType ) getClass().getGenericSuperclass() );
    return ( Class< ? > ) superType.getActualTypeArguments()[ 0 ];
  }

  /**
   * Make a SQL table column signature from a field and some properties
   * @param f Target field to fetch datatype from
   * @param annoName Name from annotation, leave empty for automatic resolve
   * @param len Length for the datatype, leave empty to set none
   * @return Ready to use column signature
   */
  private String makeColumn( Field f, String annoName, String len ) {
    String cName = annoName.equals( "" ) ? f.getName() : annoName;
    String length = len.equals( "" ) ? "" : "(" + len + ")";

    // If it would be a varchar but no length has been specified, make it text (dynamic)
    String type = getSQLDatatype( f );
    type = len.equals( "" ) && type.equals( "VARCHAR" ) ? "TEXT" : type;

    // Append new column with it's properties
    return "`" + cName + "` " + type + length + " NOT NULL";
  }

  /**
   * Get the SQL datatype from a field's type for query creation
   * @param target Target field to translate type from
   * @return SQL datatype, VARCHAR is fallback
   */
  private String getSQLDatatype( Field target ) {
    Class< ? > t = target.getType();

    if( t == int.class || t == Integer.class )
      return "INT";

    if( t == long.class || t == Long.class )
      return "BIGINT";

    if( t == double.class || t == Double.class )
      return "DOUBLE";

    if( t == float.class || t == Float.class )
      return "FLOAT";

    // Fallback is just text
    return "VARCHAR";
  }

  /**
   * Find all fields specified by annotations for further processing recursively, adds
   * the ID field from @{@link MappableModel} superclass manually
   * @param target Target class to search in
   * @return List of column info objects
   * @throws Exception Error during search
   */
  protected LinkedList< ColInfo > findFields( Class< ? > target ) throws Exception {
    // Find all fields recursively
    LinkedList< ColInfo > fields = findFieldsR( target, null );

    // Add ID field, every model must inherit this
    Field idF = target.getSuperclass().getDeclaredField( "ID" );
    idF.setAccessible( true );
    fields.addFirst( new ColInfo( idF.getName(), getSQLDatatype( idF ), "32", false, idF ) );

    return fields;
  }

  /**
   * Find all fields from the current model
   * @param target Class of the target to search fields in
   * @return List of fields
   */
  private LinkedList< ColInfo > findFieldsR( Class< ? > target, RebuilderColumns parent ) {
    // Create the array of fields, this class' id field + all from model
    LinkedList< ColInfo > fields = new LinkedList<>();

    // List of names the parent specifies, fill up with null values to use set at index for proper order
    List< String > names = parent != null ? Arrays.asList( parent.fields() ) : new ArrayList<>();
    for( int i = 0; i < names.size(); i++ )
      fields.add( null );

    // Add all fields from model, if rebuilder-columns found - dig recursive
    for( int i = 0; i < target.getDeclaredFields().length; i++ ) {
      Field f = target.getDeclaredFields()[ i ];
      f.setAccessible( true );

      // Parent annotation is set
      if( parent != null ) {

        // This field's name is not specified in the parent, skip it
        if( !names.contains( f.getName() ) )
          continue;

        // Get col properties
        int j = names.indexOf( f.getName() );
        String cName = parent.names().length > j ? parent.names()[ j ] : f.getName();
        String cLen = parent.lengths().length > j ? parent.lengths()[ j ] : "";
        boolean cUniq = parent.uniques().length > j && parent.uniques()[ j ];

        // If it would be a varchar but no length has been specified, make it text
        String type = getSQLDatatype( f );
        type = cLen.equals( "" ) && type.equals( "VARCHAR" ) ? "TEXT" : type;

        // Append colinfo list
        fields.set( j, new ColInfo( cName, type, cLen, cUniq, f ) );
        continue;
      }

      // Retrieve both possible annotations
      RebuilderColumns rc = f.getDeclaredAnnotation( RebuilderColumns.class );
      MapperColumn mc = f.getDeclaredAnnotation( MapperColumn.class );

      // Not annotated or wrongly annotated, this field is not of interest
      if( ( mc == null && rc == null ) || ( mc != null && rc != null ) )
        continue;

      // Rebuilder column found, find fields recursively
      if( rc != null ) {
        fields.addAll( findFieldsR( f.getType(), rc ) );
        continue;
      }

      // Just a single field, get properties and add
      String cName = mc.name().equals( "" ) ? f.getName() : mc.name();

      // If it would be a varchar but no length has been specified, make it text
      String type = getSQLDatatype( f );
      type = mc.length().equals( "" ) && type.equals( "VARCHAR" ) ? "TEXT" : type;

      // Append colinfo
      fields.add( new ColInfo( cName, type, mc.length(), mc.unique(), f ) );
    }

    return fields;
  }

  /**
   * Register a data translator for writing this field to DB
   * @param type Type to use this translator on
   * @param func Function for I/O
   */
  protected void registerTranslator( Class< ? > type, ParamFuncCB< Object, String > func ) {
    this.writeTranslatorsC.put( type, func );
  }

  /**
   * Register a data translator for writing this field to DB
   * @param fieldname Name of the target field to translate
   * @param func Function for I/O
   */
  protected void registerTranslator( String fieldname, ParamFuncCB< Object, String > func ) {
    this.writeTranslatorsN.put( fieldname, func );
  }
}
