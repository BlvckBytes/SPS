package at.sps.commands;

import at.sps.core.conf.Messages;
import at.sps.core.orm.mappers.BanMapper;
import at.sps.core.orm.models.Ban;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.ObjectPager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BanCmds extends CommandBase {

  @ShortCommand( command = "bans", terminalDeny = true )
  private void onBans( Player sender, String[] args ) {
    // No permission
    if( lacksPermission( sender, "sps.bans" ) )
      return;

    // No name provided
    if( args.length != 1 ) {
      sender.sendMessage( Messages.USAGE.apply( "/bans <Spieler>" ) );
      return;
    }

    // Make sure the recipient is online
    Player target = findPlayer( sender, args[ 0 ] );
    if ( target == null )
      return;

    // Open a new pager on those bans for the executor
    List< Ban > bans = BanMapper.getInst().listBans( target.getUniqueId() );
    UUID u = sender.getUniqueId();

    for( int i = 0; i < 20; i++ ) {
      bans.add( new Ban( u, u, "Permaban 1" ) );
      bans.add( new Ban( u, u, "Permaban 2" ) );
      bans.add( new Ban( u, u, "Tempban 1", System.currentTimeMillis() + ( 60000 * 60 * 72 ) ) );
      bans.add( new Ban( u, u, "Tempban 2", System.currentTimeMillis() + ( 60000 * 60 * 30 ) ) );
      bans.add( new Ban( u, u, "Permaban 3" ) );
      bans.add( new Ban( u, u, "Tempban 3 - done", System.currentTimeMillis() - 10000 ) );
    }

    new ObjectPager<>( sender, "§8" + target.getName(), bans )
        .setListener( info -> onBanClick( sender, info ) )
        .setTranslator( this::translateBan )
        .open();
  }

  /**
   * Event for when a player clicks on a ban, in the ban-pager
   * @param executor Player who clicked
   * @param ban Ban that has been clicked
   */
  private void onBanClick( Player executor, Ban ban ) {
    executor.sendMessage( "You clicked reason: " + ban.getReason() );
  }

  /**
   * Translate a ban object into an itemstack for the pager
   * @param ban Ban to translate
   * @return Translated item stack
   */
  private ItemStack translateBan( Ban ban ) {
    // Generate wool item stack
    ItemStack item = new ItemStack( Material.WOOL );
    ItemMeta meta = item.getItemMeta();

    // Set wool color based on ban state
    if( ban.isPermanent() ) {
      item.setDurability( ( short ) 14 );
      meta.setDisplayName( "§4Permanenter Bann" );
    } else {
      item.setDurability( ( short ) ( ban.isActive() ? 1 : 13 ) );
      meta.setDisplayName( ( ban.isActive() ? "§6" : "§2" ) + "Temporärer Bann" );
    }

    // Create lore with all information about this ban
    SimpleDateFormat stampF = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );
    OfflinePlayer creator = Bukkit.getOfflinePlayer( ban.getCreator() );
    meta.setLore( Arrays.asList(
      "§7Grund: §d" + ban.getReason(),
      "§7Ersteller: §d" + ( creator != null ? creator.getName() : "unbekannt" ),
      "§7Erstellt am: §d" + stampF.format( ban.getCreationDate() ),
      "§7Dauer: " + ( ban.isPermanent() ? "§cPermanent" : "§dbis " + stampF.format( ban.getExpireDate() ) ),
      "§7Aktiv: " + ( ban.isActive() ? "§cja" : "§2nein" ),
      "§7IPs: §c" + ( ban.getIpAddresses().size() > 0 ? String.join( ",", ban.getIpAddresses() ) : "§2keine" )
    ) );

    // Return creation
    item.setItemMeta( meta );
    return item;
  }
}
