package at.sps.commands;

import at.sps.core.conf.Messages;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.mappers.KitCooldownMapper;
import at.sps.core.orm.mappers.KitMapper;
import at.sps.core.orm.models.Kit;
import at.sps.core.orm.models.KitCooldown;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitCmds {

  /**
   * Command: kit
   * Usage: /kit name
   * Used to take a predefined kit
   */
  @ShortCommand( command = "kit", terminalDeny = true )
  private void onKit( Player sender, String[] args ) {
    // No name secified
    if( args.length == 0 ) {
      sender.sendMessage( Messages.USAGE.apply( "/kit <Name>" ) );
      return;
    }

    Kit target = KitMapper.getInst().getByName( args[ 0 ] );

    // Kit non existent
    if( target == null ) {
      sender.sendMessage( Messages.KIT_NON_EXISTENT.apply( args[ 0 ] ) );
      return;
    }

    // Get the kit's cooldown information for the player
    UUID holder = sender.getUniqueId();
    KitCooldown cooldown = KitCooldownMapper.getInst().getForPlayer( holder, target );

    // No cooldown existent, just write curr time to DB and hand out the kit
    if( cooldown == null || cooldown.getRemainder() == null ) {
      KitCooldownMapper.getInst().saveCooldown( new KitCooldown( holder, target.getID(), System.currentTimeMillis() ) );
    }

    // Cooldown exists, check if the player is allowed to get the kit yet
    else {
      // There is still time remaining, break here
      if( cooldown.getRemainder() > 0 ) {
        String timeString = Utils.formatRemainder( cooldown.getRemainder() );
        sender.sendMessage( Messages.KIT_COOLDOWN.apply( timeString, target.getTitle() ) );
        return;
      }

      // Since the kit will now be given, re-set the new timestamp
      cooldown.setLastUse( System.currentTimeMillis() );
      KitCooldownMapper.getInst().saveCooldown( cooldown );
    }

    // Loop contents
    List< ItemStack > contents = target.getContents();
    int dropped = 0;
    for( ItemStack recv : contents ) {

      // Calculate how many items would get deleted if just added the items
      int remainder = Utils.canReceiveItems( sender, recv );

      // Nothing would be lost, so just add
      if( remainder == 0 )
        sender.getInventory().addItem( recv );

      // Only add what fits, drop the remaining amount in the air
      else {
        int fit = recv.getAmount() - remainder;

        // Add fitting amount
        recv.setAmount( fit );
        sender.getInventory().addItem( recv );

        // Drop remainder at his head location
        recv.setAmount( remainder );
        sender.getWorld().dropItemNaturally( sender.getEyeLocation(), recv );
        dropped += remainder;
      }
    }

    sender.sendMessage( Messages.KIT_RECEIVED.apply( target.getTitle() ) );

    if( dropped > 0 )
      sender.sendMessage( Messages.KIT_DROPPED.apply( dropped ) );
  }

  /**
   * Command: kitcreate
   * Usage: /kitcreate name cooldown description
   * Used to create new kits with the items currently in inv
   */
  @ShortCommand( command = "kitcreate", terminalDeny = true )
  private void onKitCreate( Player sender, String[] args ) {
    if( args.length < 3 ) {
      sender.sendMessage( Messages.USAGE.apply( "/kitcreate <Name> <Cooldown in s> <Beschreibung>" ) );
      return;
    }

    // Check if name is available
    Kit kExisting = KitMapper.getInst().getByName( args[ 0 ] );
    if( kExisting != null ) {
      sender.sendMessage( Messages.KIT_EXISTING.apply( kExisting.getTitle() ) );
      return;
    }

    // Check that kit contents are available
    List< ItemStack > contents = getCreationContents( sender.getInventory() );
    if( contents.size() == 0 ) {
      sender.sendMessage( Messages.KIT_NO_ITEMS.apply() );
      return;
    }

    // Parse and validate cooldown
    Long cooldown = Utils.tryParseLong( args[ 1 ] );
    if( cooldown == null || cooldown < 0 ) {
      sender.sendMessage( Messages.KIT_WRONG_COOLDOWN.apply() );
      return;
    }

    // Build description string
    StringBuilder desc = new StringBuilder( args[ 2 ] );
    for( int i = 3; i < args.length; i++ )
      desc.append( " " ).append( args[ i ] );

    // Add to database
    Kit kAdd = new Kit( args[ 0 ], desc.toString(), cooldown * 1000L, contents );
    ActionResult result = KitMapper.getInst().addKit( kAdd );
    switch ( result ) {

      // Kit added
      case OK:
        sender.sendMessage( Messages.KIT_CREATED.apply( kAdd.getTitle(), kAdd.getStackCount() ) );
        break;

      // Name is already existent
      case ALREADY_EXISTENT:
        sender.sendMessage( Messages.KIT_EXISTING.apply( kAdd.getTitle() ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "KITCR->" + result ) );
        break;
    }
  }

  /**
   * Command: kitdelete
   * Usage: /kitdelete name
   * Used to delete existing kits
   */
  @ShortCommand( command = "kitdelete", terminalDeny = true )
  private void onKitDelete( Player sender, String[] args ) {

    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( Messages.USAGE.apply( "/kitdelete <Name>" ) );
      return;
    }

    ActionResult result = KitMapper.getInst().removeKit( args[ 0 ] );
    switch ( result ) {
      // Kit deleted
      case OK:
        sender.sendMessage( Messages.KIT_DELETED.apply( args[ 0 ] ) );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( Messages.KIT_NON_EXISTENT.apply( args[ 0 ] ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "KITDL->" + result ) );
        break;
    }
  }

  /**
   * Command: kitupdate
   * Usage: /kitupdate name
   * Used to update a kit's content list
   */
  @ShortCommand( command = "kitupdate", terminalDeny = true )
  private void onKitUpdate( Player sender, String[] args ) {

    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( Messages.USAGE.apply( "/kitupdate <Name>" ) );
      return;
    }

    // Check that kit is existing
    Kit target = KitMapper.getInst().getByName( args[ 0 ] );
    if( target == null ) {
      sender.sendMessage( Messages.KIT_NON_EXISTENT.apply( args[ 0 ] ) );
      return;
    }

    // Check that kit contents are available
    List< ItemStack > contents = getCreationContents( sender.getInventory() );
    if( contents.size() == 0 ) {
      sender.sendMessage( Messages.KIT_NO_ITEMS.apply() );
      return;
    }

    // Update kit contents and write
    target.setContents( contents );
    ActionResult res = KitMapper.getInst().updateKit( target );
    switch ( res ) {
      // Kit deleted
      case OK:
        sender.sendMessage( Messages.KIT_UPDATED.apply( args[ 0 ] ) );
        break;

      // Name is not existent
      case NON_EXISTENT:
        sender.sendMessage( Messages.KIT_NON_EXISTENT.apply( args[ 0 ] ) );
        break;

      // Error
      default:
        sender.sendMessage( Messages.INTERNAL_ERR.apply( "KITCH->" + res ) );
        break;
    }
  }

  /**
   * Command: kits
   * Usage: /kits
   * Used to list all available kits
   */
  @ShortCommand( command = "kits", terminalDeny = true )
  private void onKits( Player sender, String[] args ) {

    // Build search query if provided
    StringBuilder search = new StringBuilder();
    for( int i = 0; i < args.length; i++ )
      search.append( i > 0 ? " " : "" ).append( args[ i ] );

    // List all kits without actual contents for efficiency
    List< Kit > kits = KitMapper.getInst().listKits( search.toString(), false );

    // No kits created
    if( kits.size() == 0 ) {
      sender.sendMessage( Messages.KIT_LIST.apply() + Messages.KIT_NONE.getTemplate() );
      return;
    }

    // Create complex message builder, first part is list prefix
    ComplexMessage msg = new ComplexMessage( new ComplexPart( Messages.KIT_LIST.apply(), "", "", true ) );

    // Build list of kits
    for( int i = 0; i < kits.size(); i++ ) {
      // Append delimiter on everything but the first element
      if( i != 0 )
        msg.append( new ComplexPart( Messages.KIT_DELIMITER.getTemplate(), "", "", true ) );

      // Get current kit for properties and build hover message
      Kit currKit = kits.get( i );
      String hover = Messages.KIT_LIST_HOVER.applyPrefixless( currKit.getStackCount(), currKit.getDescription() );

      // Build current entry
      String text = Messages.KIT_COLOR.getTemplate() + currKit.getTitle();
      msg.append( new ComplexPart( text, hover, "/kit " + currKit.getTitle(), true ) );
    }

    // Send message to the executor
    msg.send( sender );
  }

  /**
   * Get a list of itemstacks which will be the kit's contents, ignore null or air
   * @param inv Inventory to pull items from
   * @return List of items
   */
  private List< ItemStack > getCreationContents( Inventory inv ) {
    List< ItemStack > contents = new ArrayList<>();

    // Loop inv contents
    for( ItemStack item : inv.getContents() ) {
      // Don't add nulls or air
      if( item == null || item.getType() == Material.AIR )
        continue;

      contents.add( item );
    }

    return contents;
  }
}
