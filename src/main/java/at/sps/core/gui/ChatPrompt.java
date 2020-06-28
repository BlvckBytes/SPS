package at.sps.core.gui;

import at.sps.core.Main;
import at.sps.core.conf.Messages;
import at.sps.core.utils.ParamCall;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatPrompt implements Listener {

  // Keep a static track of all instances to destroy them on reload
  private static final List< ChatPrompt > instances;
  static {
    instances = new ArrayList<>();
  }

  private final String prompt;
  private final Player viewer;
  private ParamCall< String > answer;

  /**
   * Create a new chat prompt to retrieve information through the chat text input
   * @param viewer Viewer to intercept chat of
   * @param prompt Prompt to print on opening
   */
  public ChatPrompt( Player viewer, String prompt ) {
    instances.add( this );

    this.prompt = prompt;
    this.viewer = viewer;

    // Register events
    Bukkit.getPluginManager().registerEvents( this, Main.getInst() );
  }

  /**
   * Open this chat prompt for the provided viewer and start listening
   * @param answer Callback for user answers, can be used multiple times
   */
  public void open( ParamCall< String > answer ) {
    viewer.sendMessage( Messages.PREFIX.getTemplate() + prompt );
    this.answer = answer;
  }

  /**
   * Exposing the inner destroy func with the force flag set as false
   */
  public void destroy() {
    destroy( false );
  }

  /**
   * Stop this prompt from grabbing the user's input and shut
   * down it's internal listeners and callbacks
   */
  private void destroy( boolean force ) {
    // Unregister all events from this instance
    HandlerList.unregisterAll( this );

    // Terminate callback and remove from instance tracker
    answer = null;
    instances.remove( this );

    // Notify the viewer if it has been forcefully destroyed
    if( force )
      this.viewer.sendMessage( Messages.PREFIX.getTemplate() + "§cDie Aufforderung wurde wegen eines reloades abgebrochen!" );
  }

  /**
   * Handle the chat for the viewer, receiving nothing while inside prompt and
   * sending nothing by intercepting and calling the provided callback
   */
  @EventHandler
  @SuppressWarnings( { "unused" } )
  public void onChat( AsyncPlayerChatEvent e ) {
    // Stop message from coming through to others
    if( e.getPlayer().equals( this.viewer ) ) {
      e.setCancelled( true );

      // Call the callback with this message
      answer.call( e.getMessage() );
    }

    // Prompt has been opened, thus stop all incoming messages to the viewer
    if( this.answer != null )
      e.getRecipients().remove( this.viewer );
  }

  /**
   * Destroy all active instances
   */
  public static void destroyAll() {
    for( int i = instances.size() - 1; i >= 0; i-- )
      instances.get( i ).destroy( true );
  }
}
