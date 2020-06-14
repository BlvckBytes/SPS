package at.sps.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComplexPart {

  private static final Class< ? > componentC, chatMessageC, chatModifierC, chatHoverableC, chatClickableC;
  private static final Class< ? > enumHoverActionC, enumClickActionC;

  // Load NMS classes that will be used on every instance
  static {
    componentC = NMSHelper.getNMSClass( "IChatBaseComponent" );
    chatMessageC = NMSHelper.getNMSClass( "ChatMessage" );
    chatModifierC = NMSHelper.getNMSClass( "ChatModifier" );
    chatHoverableC = NMSHelper.getNMSClass( "ChatHoverable" );
    chatClickableC = NMSHelper.getNMSClass( "ChatClickable" );
    enumHoverActionC = NMSHelper.getNMSClass( "ChatHoverable$EnumHoverAction" );
    enumClickActionC = NMSHelper.getNMSClass( "ChatClickable$EnumClickAction" );
  }

  private final String text, hover, action;
  private final boolean command;

  /**
   * Creates a new part of a complex message, contains text, hover and command on click
   * @param text Text of this part
   * @param hover Hover text, leave empty for none
   * @param action Click command, leave empty for none
   * @param command If true, action is command, if false it is an URL
   */
  public ComplexPart( String text, String hover, String action, boolean command ) {
    this.text = text;
    this.hover = hover;
    this.action = action;
    this.command = command;
  }

  /**
   * Build a partitioned message part object, hover and command are
   * the same, just text differs
   * @param textPart Part of the text that this partition should hold
   * @return Built component as object
   */
  private Object buildPartition( String textPart ) {
    try {
      // Create the component and a new empty chat modifier
      Object component = chatMessageC.getConstructor( String.class, Object[].class ).newInstance( textPart, new Object[ 0 ] );
      Object chatModifier = chatModifierC.getConstructor().newInstance();

      // Hover text has been provided
      if( hover != null && !hover.trim().equals( "" ) ) {
        Object hoverMessage = chatMessageC.getConstructor( String.class, Object[].class ).newInstance( hover, new Object[ 0 ] );
        Object showText = NMSHelper.getEnumValue( enumHoverActionC, "SHOW_TEXT" );
        Object hoverable = chatHoverableC.getConstructor( enumHoverActionC, componentC ).newInstance( showText, hoverMessage );
        chatModifierC.getMethod( "setChatHoverable", chatHoverableC ).invoke( chatModifier, hoverable );
      }

      // Click command has been provided
      if( action != null && !action.trim().equals( "" ) ) {
        Object runCommand = NMSHelper.getEnumValue( enumClickActionC, command ? "RUN_COMMAND" : "OPEN_URL" );
        Object clickable = chatClickableC.getConstructor( enumClickActionC, String.class ).newInstance( runCommand, action );
        chatModifierC.getMethod( "setChatClickable", chatClickableC ).invoke( chatModifier, clickable );
      }

      // Append the created chat modifier to component
      component.getClass().getMethod( "setChatModifier", chatModifierC ).invoke( component, chatModifier );
      return component;
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while creating a component from a message part", LogLevel.ERROR );
      SLogging.getInst().log( e );
      return null;
    }
  }

  /**
   * Create a list of IChatBaseComponent from the data in this object
   * for further processing, parts get determined based on new color declaration, so that
   * every component is only at 1 color and optional text modifications (bold, ...)
   * @return List of IChatBaseComponent created by reflect
   */
  public List< Object > toComponents() {
    List< Object > buf = new ArrayList<>();

    // Group all color prefixes until the next color declaration
    // This is done since one component may only have 1 color, it gets buggy otherwise
    Pattern pt = Pattern.compile( "(§[0-9a-fk-or])+[^§]*" );
    Matcher m = pt.matcher( text );

    // Append all partitions, based on color prefixes
    while( m.find() )
      buf.add( buildPartition( m.group() ) );

    return buf;
  }
}