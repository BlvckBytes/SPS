package at.sps.core.utils;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

public class ComplexMessage {

  private static final Class< ? > packetChatC, componentC;

  // Load NMS classes that will be used on every instance
  static {
    packetChatC = NMSHelper.getNMSClass( "PacketPlayOutChat" );
    componentC = NMSHelper.getNMSClass( "IChatBaseComponent" );
  }

  private Object rootNode;

  /**
   * Constructor with root node in args
   */
  public ComplexMessage( ComplexPart start ) {
    append( start.toComponents() );
  }

  /**
   * Append a message part to the complex message
   * @param part Message part with all options set
   */
  public void append( ComplexPart part ) {
    append( part.toComponents() );
  }

  /**
   * Append a list of components which resulted from a messagepart, based on colors
   * @param components List of components to add
   */
  public void append( List< Object > components ) {
    for( Object component : components ) {
      try {
        // No root node set yet, so set this part as the head of chain
        if( rootNode == null ) {
          rootNode = component;
          continue;
        }

        // Root node existing, add sibling to it
        Method addM = rootNode.getClass().getMethod( "addSibling", componentC );
        addM.invoke( rootNode, component );
      } catch ( Exception e ) {
        SLogging.getInst().log( "Error while trying to append a part to a complex message!", LogLevel.ERROR );
        SLogging.getInst().log( e );
      }
    }
  }

  /**
   * Send this complex message to a receiver player
   * @param receiver Target player
   */
  public void send( Player receiver ) {
    try {
      Object chatPacket = packetChatC.getConstructor( componentC ).newInstance( rootNode );
      NMSHelper.sendPacket( receiver, chatPacket );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while sending out a complex message!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }
  }
}
