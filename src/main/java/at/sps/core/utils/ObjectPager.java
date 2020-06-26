package at.sps.core.utils;

import at.sps.core.Main;
import at.sps.core.conf.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ObjectPager< T > implements Listener {

  private final Player viewer;
  private final String title;
  private final List< T > objects;
  private final List< ItemStack > items;
  private ParamCall< T > clickCall;
  private ParamFuncCB< T, ItemStack > translator;
  private Inventory inv;
  private int page, pages, pageSize;

  /**
   * Open up a new pager for a list of objects, so the viewer can choose, every
   * click will call the listener with that clicked object
   * @param viewer Viewer of the inventory
   * @param title Inventory title
   * @param objects List of objects to page through
   */
  public ObjectPager( Player viewer, String title, List< T > objects ) {
    this.viewer = viewer;
    this.title = title;
    this.objects = objects;
    this.items = new ArrayList<>();

    // Register all events in this object
    Bukkit.getServer().getPluginManager().registerEvents( this, Main.getInst() );
  }

  /**
   * Build the inventory based on the title and objects provided
   */
  private void buildInventory() {
    // Create inventory, 9 by 4, with provided title
    inv = Bukkit.createInventory( null, 9 * 4, this.title );
    pageSize = inv.getSize() - 9;
    pages = ( int ) Math.ceil( items.size() / ( float ) pageSize );

    // Previous page item
    inv.setItem( 28, new ItemBuilder( Material.SKULL_ITEM, 1, 3 )
      .setSkullOwner( "MHF_ArrowLeft" )
      .setName( "§8« §dVorherige Seite §8»" )
      .setLore( "§7Klicken, um auf die vorherige", "§7Seite zu navigieren" )
      .build()
    );

    // Fill up the bottom row with glass slots
    ItemStack glass = new ItemBuilder( Material.STAINED_GLASS_PANE, 1 ).setDurability( 15 ).setName( " " ).build();
    int[] glassSlots = { 27, 29, 30, 32, 33, 35 };
    for( int glassSlot : glassSlots )
      inv.setItem( glassSlot, glass );

    // Set page item
    updatePageItem();

    // Next page item
    inv.setItem( 34, new ItemBuilder( Material.SKULL_ITEM, 1, 3 )
      .setSkullOwner( "MHF_ArrowRight" )
      .setName( "§8« §dNächste Seite §8»" )
      .setLore( "§7Klicken, um auf die nächste", "§7Seite zu navigieren" )
      .build()
    );
  }

  /**
   * Update the page item to match the currently selected page and the max pages available
   */
  private void updatePageItem() {
    // Current page information item, +1 since page needs to be 0-based
    inv.setItem( 31, new ItemBuilder( Material.BOOK, 1 )
      .setName( "§d" + ( page + 1 ) + "§8/§d" + Math.max( 1, pages ) )
      .setLore( "§7Dieses Item zeigt Dir, auf welcher", "§7Seite Du dich befindest." )
      .build()
    );
  }

  /**
   * Translate all objects into itemstacks based on the provided translator func
   */
  private void translateObjects() {
    // Add all objects as itemstacks to the items cache list
    for( T obj : this.objects )
      items.add( this.translator.call( obj ) );
  }

  /**
   * Set a listener for item click events
   * @param clicked The target object that has been clicked on
   * @return Instance for builder pattern
   */
  public ObjectPager< T > setListener( ParamCall< T > clicked ) {
    this.clickCall = clicked;
    return this;
  }

  /**
   * Set a translator function to convert objects into itemstacks
   * @param translator Translator function
   * @return Instance for builder pattern
   */
  public ObjectPager< T > setTranslator( ParamFuncCB< T, ItemStack > translator ) {
    this.translator = translator;
    return this;
  }

  /**
   * Render out the current page while simultaneously cleaning remaining slots
   */
  private void renderPage() {
    updatePageItem();

    for( int i = 0; i < 27; i++ ) {
      int itemInd = ( 27 * page ) + i;
      inv.setItem( i, items.size() > itemInd ? items.get( itemInd ) : null );
    }
  }

  /**
   * Open the inventory for the viewer provided
   */
  public void open() {
    try {
      // Check that translator is available
      if( translator == null )
        throw new IllegalStateException( "No object translator found, this is mandatory!" );

      // Translate items and build inventory
      translateObjects();
      buildInventory();
      renderPage();

      viewer.openInventory( inv );
    } catch ( Exception e ) {
      SLogging.getInst().log( "Error while opening an ObjectPager for the viewer!", LogLevel.ERROR );
      SLogging.getInst().log( e );
    }
  }

  /**
   * Handle inventory clicking (page up/down, object callback)
   */
  @EventHandler
  private void onClick( InventoryClickEvent e ) {
    // Only target this inventory
    if( !e.getInventory().equals( inv ) )
      return;

    // Cancel item move
    e.setCancelled( true );

    // No item has been clicked
    if( e.getCurrentItem() == null )
      return;

    ItemStack is = e.getCurrentItem();

    // If the clicked item is a skull which can hold owner names (3)
    if( is.getType() == Material.SKULL_ITEM && is.getDurability() == 3 ) {
      String owner = ( ( SkullMeta ) is.getItemMeta() ).getOwner();

      // Decide what action to take based on skull head owner's name
      switch( owner ) {
        case "MHF_ArrowLeft":
          prevPage();
          break;

        case "MHF_ArrowRight":
          nextPage();
          break;
      }

      return;
    }

    // Within page size, thus it's a object
    if( e.getSlot() < pageSize ) {
      int ind = page * 27 + e.getSlot();

      // No item at that location
      if( ind > objects.size() - 1 )
        return;

      // Call callback
      T targ = objects.get( ind );
      clickCall.call( targ );
    }
  }

  /**
   * Go to the previous page by decreasing page by 1, then re-render
   */
  private void prevPage() {
    if( page == 0 ) {
      viewer.sendMessage( Messages.BANS_NO_PREV.apply() );
      return;
    }

    page--;
    renderPage();
  }

  /**
   * Go to the next page by increasing page by 1, then re-render
   */
  private void nextPage() {
    if( page == pages - 1 ) {
      viewer.sendMessage( Messages.BANS_NO_NEXT.apply() );
      return;
    }

    page++;
    renderPage();
  }
}
