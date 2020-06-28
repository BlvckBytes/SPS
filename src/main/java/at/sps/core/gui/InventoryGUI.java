package at.sps.core.gui;

import at.sps.core.Main;
import at.sps.core.utils.Pair;
import at.sps.core.utils.ParamCall;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryGUI implements Listener {

  // Keep a static track of all instances to destroy them on reload
  private static final List< InventoryGUI > instances;
  static {
    instances = new ArrayList<>();
  }

  @Setter
  private ParamCall< Pair< Integer, ItemStack > > clickListener;
  protected Inventory inv;

  /**
   * Create a new inventory gui, which allows to create an inventory with a
   * known size and title aswell as a callback for slot and item information,
   * this handles default checks like inv match and item not null
   * @param title Title of the inventory
   * @param rows Number of rows
   */
  public InventoryGUI( String title, int rows ) {
    instances.add( this );

    // Register events and create inventory from provided informations
    this.inv = Bukkit.createInventory( null, 9 * rows, title );
    Bukkit.getServer().getPluginManager().registerEvents( this, Main.getInst() );
  }

  /**
   * Destroy this instance of a GUI by closing the inv for all viewers and
   * terminating the onclick-callback, also unregister bukkit events
   */
  public void destroy() {
    // Unregister all events from this instance
    HandlerList.unregisterAll( this );

    // Close the inventory for all viewers
    for( int i = inv.getViewers().size() - 1; i >= 0; i-- )
      inv.getViewers().get( i ).closeInventory();

    // Terminate callback and remove from instance tracker
    clickListener = null;
    instances.remove( this );
  }

  /**
   * Handle closing the inventory (destroying the instance)
   */
  @EventHandler
  @SuppressWarnings( { "unused" } )
  public void onClose( InventoryCloseEvent e ) {
    // Not the target inventory
    if( !e.getInventory().equals( this.inv ) )
      return;

    // Destroy gui instance
    destroy();
  }

  /**
   * Handle clicking inside this inventory gui
   */
  @EventHandler
  @SuppressWarnings( { "unused" } )
  public void onClick( InventoryClickEvent e ) {
    // Not the target inventory
    if( !e.getInventory().equals( this.inv ) )
      return;

    // Cancel item manipulation
    e.setCancelled( true );

    // Get the clicked item and make sure it exists
    ItemStack item = e.getCurrentItem();
    if( item == null )
      return;

    // Call the callback if it exists
    if( clickListener != null )
      clickListener.call( new Pair<>( e.getSlot(), item ) );
  }

  /**
   * Destroy all active instances
   */
  public static void destroyAll() {
    for( int i = instances.size() - 1; i >= 0; i-- )
      instances.get( i ).destroy();
  }
}
