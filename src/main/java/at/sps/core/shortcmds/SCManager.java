package at.sps.core.shortcmds;

import at.sps.core.ConsoleLogger;
import at.sps.core.GlobalConstants;
import at.sps.core.utils.Pair;
import at.sps.core.utils.ParamCall;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SCManager implements Listener {

  private final Map< String, Pair< ShortCommand, Method > > commands;
  private final Map< String, Object > containers;

  /**
   * Create a new short command manager which acts as a central point for
   * registering new containers aswell as command preprocess-interception
   * @param plugin Main class which extends from JavaPlugin
   */
  public SCManager( JavaPlugin plugin ) {
    this.commands = new HashMap<>();
    this.containers = new HashMap<>();

    // Register the events in this class for command interception
    plugin.getServer().getPluginManager().registerEvents( this, plugin );
  }

  /**
   * Used to intercept command preprocessing, so the empty fake commands
   * won't get executed, but rather the annotated method will
   */
  @EventHandler
  private void onPreprocess( PlayerCommandPreprocessEvent ev ) {
    Player sender = ev.getPlayer();
    String[] data = ev.getMessage().substring( 1 ).split( " " );
    String command = data[ 0 ].toLowerCase();

    // This command is not handled in any way by this manager, since it doesn't exist
    if( !commands.containsKey( command ) )
      return;
    // Block all managed commands from going further in the pipeline
    ev.setCancelled( true );

    Pair< ShortCommand, Method > callHandle = commands.get( command );

    // Check if the player has the permission to execute this
    String perm = callHandle.getKey().permission();
    if( !perm.equals( "" ) && !sender.hasPermission( perm ) ) {
      sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.NO_PERM );
      return;
    }

    try {
      // Invoke the annotated command method with sender and args
      Object container = containers.get( command );

      String[] args = data.length > 1 ? Arrays.copyOfRange( data, 1, data.length ) : new String[]{};
      callHandle.getValue().invoke( container, sender, args );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while trying to invoke an annotated SC method!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }

  /**
   * Register a container of short commands, where annotated methods
   * get loaded and activated automatically
   * @param container Instance of container
   */
  public void registerContainer( Object container ) {
    try {
      // Loop all methods this class contains, also get their target annotation
      for( final Method meth : container.getClass().getDeclaredMethods() ) {
        ShortCommand targAnno = meth.getDeclaredAnnotation( ShortCommand.class );

        // This method is not annotated with the target annotation, skip it
        if( targAnno == null )
          continue;

        // Parameters were defined in a corrupt way, skip this method
        // Only proper signature: (CommandSender sender, String[] args)
        Class< ? >[] parT = meth.getParameterTypes();
        boolean isProperReceiver = ( parT[ 0 ] == CommandSender.class || parT[ 0 ] == Player.class ) && parT[ 1 ] == String[].class;
        if( meth.getParameterCount() != 2 || !isProperReceiver ) {
          ConsoleLogger.getInst().logMessage( "Corrupted method signature for ShortCommand at " + meth.getName() + "!" );
          return;
        }

        String cmd = targAnno.command().toLowerCase();
        Pair< ShortCommand, Method > callHandle = new Pair<>( targAnno, meth );

        // Make that method accessible for calling
        meth.setAccessible( true );

        // Store command with it's call-handle and container into LUTs
        this.commands.put( cmd, callHandle );
        this.containers.put( cmd, container );

        // Register tabbing functionallity
        registerTab( cmd, args -> {
          // Terminal invocation is denied
          if( targAnno.terminalDeny() ) {
            ConsoleLogger.getInst().logMessage( "&c" + GlobalConstants.PLAYER_ONLY.toString() );
            return;
          }

          // Invoke command with args and no sender, as stated in shortcommand annotation
          try {
            meth.invoke( container, Bukkit.getConsoleSender(), args );
          } catch ( Exception e ) {
            ConsoleLogger.getInst().logMessage( "&cError while trying to invoke an annotated SC method!" );
            ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
          }
        } );
      }
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while registering a SC container object!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }

  /**
   * Register a new empty command in the command-map to make tabbing work, this
   * is a more proper registration. Then, actual processing will be done using the
   * preprocess event for players and by the callback for the console sender
   *
   * @param command Command's name
   * @param consoleCall Callback for command line invocation, passes args
   */
  private void registerTab( String command, ParamCall< String[] > consoleCall ) {
    SCEmptyCommand cmd = new SCEmptyCommand( command, consoleCall );

    // Register command in bukkit's command-map using reflect
    try {
      final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField( "commandMap" );
      bukkitCommandMap.setAccessible( true );
      CommandMap commandMap = ( CommandMap ) bukkitCommandMap.get( Bukkit.getServer() );
      commandMap.register( command, cmd );
    } catch ( Exception e ) {
      ConsoleLogger.getInst().logMessage( "&cError while registering SC tab functionallity!" );
      ConsoleLogger.getInst().logMessage( "&c" + Utils.stringifyException( e ) );
    }
  }
}
