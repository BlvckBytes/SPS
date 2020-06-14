package at.sps.core.orm.mappers;

import at.sps.core.Main;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.MariaDB;
import at.sps.core.orm.ModelMapper;
import at.sps.core.orm.models.Kit;
import at.sps.core.utils.LogLevel;
import at.sps.core.utils.SLogging;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitMapper extends ModelMapper< Kit > {

  private static KitMapper inst;

  /**
   * The home mapper is used to load, list, create, modify and delete kits
   * @param database Connection to the SQL database for r/w
   */
  @SuppressWarnings( { "unchecked" } )
  public KitMapper( MariaDB database ) {
    super( database );
    inst = this;

    registerTranslator( "contents", val -> stringifyItems( ( List< ItemStack > ) val ) );
  }

  /**
   * Add a newly created kit
   * @param kit Kit model to add
   * @return ActionStatus as the exit code
   */
  public ActionResult addKit( Kit kit ) { return write( kit, false ); }

  /**
   * Delete a kit by it's name
   * @param name Name of the target kit
   * @return ActionStatus as the exit code
   */
  public ActionResult removeKit( String name ) {
    return delete( Collections.singletonList( getByName( name ) ) );
  }

  /**
   * Update a kit, for example it's items
   * @param kit Kit to change with new values in it
   * @return ActionStatus as the exit code
   */
  public ActionResult updateKit( Kit kit ) {
    return write( kit, true );
  }

  /**
   * Get an existing kit by it's name
   * @param name Name of the kit
   * @return Kit if found, null otherwise
   */
  public Kit getByName( String name ) {
    try {
      // Fetch result to get only the target player's home with corresponding name
      List< Kit > result = read( database.fetchResult(
          "SELECT * FROM `Kit`" +
          "WHERE `title` = ?",
          name
      ) );

      // There can just be one result since UUID & name are the key
      return result.size() > 0 ? result.get( 0 ) : null;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while searching for a kit by it's name!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * Get a kit by it's ID, used for cooldown mapping
   * @param id ID of the target kit
   * @return Kit if exists, null otherwise
   */
  public Kit getById( int id ) {
    try {
      // Fetch result to get only the target player's home with corresponding name
      List< Kit > result = read( database.fetchResult(
          "SELECT * FROM `Kit`" +
          "WHERE `ID` = ?",
          id
      ) );

      // There can just be one result since UUID & name are the key
      return result.size() > 0 ? result.get( 0 ) : null;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while searching for a kit by it's id!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * List all kits existing in database
   * @param searchterm Searchterm for name search
   * @param loadContent Whether or not to laod the actual contents
   * @return List of existing kits
   */
  public List< Kit > listKits( String searchterm, boolean loadContent ) {
    try {
      // Fetch result to get only the searched for kits
      String cols = loadContent ? "*" : "ID, title, description, cooldown, stackCount, '' as contents";
      return read( database.fetchResult(
        "SELECT " + cols + " FROM `Kit`" +
        "WHERE LOWER(`title`) LIKE LOWER(CONCAT('%', ?, '%'))",
        searchterm
      ) );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while listing kits!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return new ArrayList<>();
    }
  }

  /**
   * Read kits from the database
   * @param rs ResultSet from an executed query
   * @return List of generated kits
   */
  @Override
  public List< Kit > read( ResultSet rs ) {
    List< Kit > buf = new ArrayList<>();

    try {
      // Loop result rows
      while( rs.next() ) {

        // Load contents base64 string, create fallback list
        String contents = rs.getString( "contents" );
        List< ItemStack > items = new ArrayList<>();

        // Rebuild kit from database, rebuild items seperately from the string - only if not masked off
        if( !( contents == null || contents.trim().equals( "" ) ) )
          items = rebuildItems( contents );

        Kit kit = new Kit( rs.getString( "title" ), rs.getString( "description" ), rs.getLong( "cooldown" ), items );
        kit.setStackCount( rs.getInt( "stackCount" ) );
        bindID( kit, rs );

        buf.add( kit );
      }
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while mapping read kits!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }

    return buf;
  }

  /**
   * Turn a list of itemstacks into a base64 encoded string to write
   * @param items List of itemstacks
   * @return Base64 encoded string containing item informations
   */
  private String stringifyItems( List< ItemStack > items ) {
    try {
      // Open write streams for string
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream( outputStream );

      // Write size and all individual itemstacks
      dataOutput.writeInt( items.size() );
      for( ItemStack item : items )
        dataOutput.writeObject( item );

      // Close resources and return encoded string
      dataOutput.close();
      return Base64Coder.encodeLines( outputStream.toByteArray() );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while stringifying a kit's items list!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return "";
    }
  }

  /**
   * Rebuild a list of items from an base64 encoded string
   * @param data Base64 string of previously encoded list of itemstacks
   * @return List of itemstacks
   */
  private List< ItemStack > rebuildItems( String data ) {
    List< ItemStack > items = new ArrayList<>();
    try {
      // Open streams from string
      ByteArrayInputStream inputStream = new ByteArrayInputStream( Base64Coder.decodeLines( data ) );
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream( inputStream );

      // Read as many items into the list as the first integer suggests
      int num = dataInput.readInt();
      for( int i = 0; i < num; i++ )
        items.add( ( ItemStack ) dataInput.readObject() );

      // Close resources
      dataInput.close();
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while unpacking a kit's items list!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }

    return items;
  }

  /**
   * Singleton instance getter for the kitmapper
   * @return KitMapper object instance
   */
  public static KitMapper getInst() {
    if( inst == null )
      return new KitMapper( Main.getDb() );

    return inst;
  }
}
