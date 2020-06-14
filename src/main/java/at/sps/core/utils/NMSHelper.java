package at.sps.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NMSHelper {

  /**
   * Get the currently active NMS version
   * @return Version string of package
   */
  public static String getVersion() { return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]; }

  /**
   * Get an NMS class by it's name, automatically appends version and nms prefix
   * @param name Class name
   * @return Class refenrece
   */
  public static Class< ? > getNMSClass( String name ) {
    try {
      return Class.forName( "net.minecraft.server." + getVersion() + "." + name );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while getting an NMS class!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * Send an NMS packet to a target player
   * @param to Player to send it to
   * @param packet Packet to send
   */
  public static void sendPacket( Player to, Object packet ) {
    try {
      Object playerHandle = to.getClass().getMethod( "getHandle", new Class[ 0 ] ).invoke( to );
      Object playerConnection = playerHandle.getClass().getField( "playerConnection" ).get( playerHandle );
      playerConnection.getClass().getMethod( "sendPacket", getNMSClass( "Packet" ) ).invoke( playerConnection, packet );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while sending out an NMS packet!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }
  }

  /**
   * Get an enum value as an object by it's name
   * @param clazz Enum class reference
   * @param value Enum target name
   * @return Enum as object
   */
  public static Object getEnumValue( Class< ? > clazz, String value ) {
    try {
      return clazz.getMethod( "valueOf", String.class ).invoke( null, value );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while getting an enum value!", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }
}
