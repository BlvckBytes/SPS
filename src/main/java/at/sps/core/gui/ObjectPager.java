package at.sps.core.gui;

import at.sps.core.conf.Messages;
import at.sps.core.utils.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ObjectPager< T > extends InventoryGUI implements Listener {

  private final Player viewer;
  private final List< T > objects;
  private final List< ItemStack > items;
  private ParamCall< T > clickCall;
  private ParamFuncCB< T, ItemStack > translator;
  private int page, pages, pageSize;

  /**
   * Open up a new pager for a list of objects, so the viewer can choose, every
   * click will call the listener with that clicked object
   * @param viewer Viewer of the inventory
   * @param title Inventory title
   * @param rows Number of effective rows
   * @param objects List of objects to page through
   */
  public ObjectPager( Player viewer, String title, int rows, List< T > objects ) {
    // Rows + 1 since there is 1 control row
    super( title, rows + 1 );

    this.viewer = viewer;
    this.objects = objects;
    this.items = new ArrayList<>();

    setClickListener( pair -> onClick( pair.getValue(), pair.getKey() ) );
  }

  /**
   * Build the inventory based on the objects provided
   */
  private void buildInventory() {
    pageSize = inv.getSize() - 9;
    pages = ( int ) Math.ceil( items.size() / ( float ) pageSize );

    // Previous page item
    inv.setItem( 28, new ItemBuilder( Material.SKULL_ITEM, 1, 3 )
      .setSkullOwner( "MHF_ArrowLeft" )
      .setName( Messages.PAGER_PREV_TITLE.getTemplate() )
      .setLore( Messages.PAGER_PREV_LORE.getTemplateML() )
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
      .setName( Messages.PAGER_NEXT_TITLE.getTemplate() )
      .setLore( Messages.PAGER_NEXT_LORE.getTemplateML() )
      .build()
    );
  }

  /**
   * Update the page item to match the currently selected page and the max pages available
   */
  private void updatePageItem() {
    // Current page information item, +1 since page needs to be 0-based
    inv.setItem( 31, new ItemBuilder( Material.BOOK, 1 )
      .setName( Messages.PAGER_PAGE_TITLE.applyPrefixless( page + 1, Math.max( 1, pages ) ) )
      .setLore( Messages.PAGER_PAGE_LORE.getTemplateML() )
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
   * Handle item clicking, basically paging and object callbacks
   * @param is ItemStack that has been clicked
   * @param sl Slot that has been clicked on
   */
  private void onClick( ItemStack is, int sl ) {
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
    if( sl < pageSize ) {
      int ind = page * 27 + sl;

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
