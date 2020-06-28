package at.sps.core.gui;

import at.sps.core.utils.Pair;
import at.sps.core.utils.ParamCall;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ActionPrompt extends InventoryGUI implements Listener {

  private final Player viewer;
  private ParamCall< Integer > slotCallback;

  /**
   * Create a new gui action prompt which lets the user click on action items
   * while the passed callback from the #open function gets called with the
   * clicked slot to make further functionallity possible
   * @param viewer Viewer of the inventory, a player
   * @param title Title of the gui
   * @param rows Number of rows of the gui
   */
  public ActionPrompt( Player viewer, String title, int rows ) {
    super( title, rows );
    this.viewer = viewer;

    setClickListener( this::onClick );
  }

  /**
   * Fill the prompt's gui slots with glass, this needs to be called first, so the
   * action items then override set glass slots where needed, otherwise they get overridden
   */
  public void fillGlass() {
    ItemStack glass = new ItemBuilder( Material.STAINED_GLASS_PANE ).setDurability( 15 ).setName( " " ).build();
    for( int i = 0; i < inv.getSize(); i++ )
      inv.setItem( i, glass );
  }

  /**
   * Set an action item inside the gui
   * @param slot Slot to set it on
   * @param item ItemStack to set
   */
  public void setItem( int slot, ItemStack item ) {
    inv.setItem( slot, item );
  }

  /**
   * Open the action prompt gui for the provided player
   * @param slotCallback Callback for the clicked slot
   */
  public void open( ParamCall< Integer > slotCallback ) {
    this.slotCallback = slotCallback;
    this.viewer.openInventory( this.inv );
  }

  /**
   * Manage the passing of the clicked slot information to the set callback,
   * cancel if the callback is non existent
   * @param clickInfo Information about the clicked item/slot
   */
  private void onClick( Pair< Integer, ItemStack > clickInfo ) {
    if( this.slotCallback == null )
      return;

    this.slotCallback.call( clickInfo.getKey() );
  }
}
