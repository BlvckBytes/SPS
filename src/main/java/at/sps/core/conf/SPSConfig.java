package at.sps.core.conf;

import at.sps.core.ConsoleLogger;
import at.sps.core.utils.Utils;
import com.google.common.base.Charsets;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class SPSConfig extends YamlConfiguration {

  private File file;

  /**
   * Create or load a new configuration based on a yaml file
   * @param file File to specify the location
   */
  public SPSConfig( File file ) {
    try {
      this.file = file;
      load( file );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while loading config file!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }

  /**
   * Write all configuration entries available into yaml
   */
  private void updateEntries() throws IOException {
    // Write all message templates
    boolean wrote = false;
    for( Messages msg : Messages.values() ) {
      String key = "Messages." + msg;

      // This value is already existent in config
      if( isSet( key ) ) {
        msg.setTemplate( getString( key ) );
        continue;
      }

      set( key, msg.getTemplate() );
      wrote = true;
    }

    // If something got added, write changes
    if( wrote )
      save( file );
  }

  /**
   * Create the config file with it's parent directories if not yet existent
   */
  private void mkFile() throws IOException {
    // Create parent dirs if missing, does nothing otherwise anyways
    file.getParentFile().mkdir();

    // If the file does not exist, data get's written the first time
    if( !file.exists() && !file.createNewFile() )
      throw new IOException( "Could not create file or parent directories for config!" );
  }

  /**
   * Write the YAML configuration in UTF-8 format to a file
   * @param file File to save lines to
   */
  public void save( File file ) throws IOException {
    assert file != null;
    mkFile();

    // Write #saveToString into file, which is basically the YAML as string
    try ( Writer writer = new OutputStreamWriter( new FileOutputStream( file ), Charsets.UTF_8 ) ) {
      writer.write( saveToString() );
    }
  }

  /**
   * Load YAML configuration in UTF-8 format from a file
   * @param file File to load lines from
   */
  public void load( File file ) throws IOException, InvalidConfigurationException {
    assert file != null;
    mkFile();

    // Load data from file into memory
    load( new InputStreamReader( new FileInputStream( file ), Charsets.UTF_8 ) );

    // Update configuration entries on every load
    updateEntries();
  }
}