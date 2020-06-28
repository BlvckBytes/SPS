package at.sps.commands;

import at.sps.core.conf.Messages;
import at.sps.core.gui.ActionPrompt;
import at.sps.core.gui.ChatPrompt;
import at.sps.core.gui.ItemBuilder;
import at.sps.core.gui.ObjectPager;
import at.sps.core.orm.mappers.BanMapper;
import at.sps.core.orm.models.Ban;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
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
      Ban b = new Ban( u, u, "Permaban 2" );
      b.revoke( u, "This is a revoke test" );
      bans.add( b );
      bans.add( new Ban( u, u, "Tempban 1", System.currentTimeMillis() + ( 60000 * 60 * 72 ) ) );
      bans.add( new Ban( u, u, "Tempban 2", System.currentTimeMillis() + ( 60000 * 60 * 30 ) ) );
      bans.add( new Ban( u, u, "Permaban 3" ) );
      bans.add( new Ban( u, u, "Tempban 3 - done", System.currentTimeMillis() - 10000 ) );
    }

    new ObjectPager<>( sender, "§8" + target.getName(), 3, bans )
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
    ActionPrompt ap = new ActionPrompt( executor, "§8Bandetails", 3 );
    ap.fillGlass();
    ap.setItem( 10, new ItemBuilder( Material.ANVIL )
        .setName( "§8« §dAufheben §8»" )
        .setLore( "§7Hebe diesen Bann wieder auf,", "§7insofern er noch aktiv ist" )
        .build()
    );
    ap.setItem( 13, new ItemBuilder( Material.PAPER )
        .setName( "§8« §dIPs verwalten §8»" )
        .setLore( "§7Füge IPs hinzu oder lösche", "§7existierende Adressen" )
        .build()
    );
    ap.setItem( 16, new ItemBuilder( Material.BARRIER )
        .setName( "§8« §cLöschen §8»" )
        .setLore( "§7Lösche diesen Bann und seine Daten", "§7für immer aus der Datenbank" )
        .build()
    );
    ap.open( slot -> {
      // Close action prompt
      ap.destroy();

      // Spawn chat prompt
      ChatPrompt cp = new ChatPrompt( executor, "§7Bitte gib eine §dZahl §7in den Chat ein:" );
      cp.open( answer -> {

        // Validate number
        Long ansNum = Utils.tryParseLong( answer );
        if( ansNum == null ) {
          executor.sendMessage( Messages.PREFIX.getTemplate() + "§c'" + answer + "' ist keine Zahl!" );
          return;
        }

        // Destroy the prompt and print success
        cp.destroy();
        executor.sendMessage( Messages.PREFIX.getTemplate() + "§7Du hast die Zahl §a" + ansNum + " §7eingegeben!" );
      } );
    } );
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
      item.setDurability( ( short ) ( ban.isActive() ? 14 : 13 ) );
      meta.setDisplayName( ban.isActive() ? Messages.BANS_ITEM_PERM_TITLE.getTemplate() : Messages.BANS_ITEM_OVER_PERM_TITLE.getTemplate() );
    } else {
      item.setDurability( ( short ) ( ban.isActive() ? 1 : 13 ) );
      meta.setDisplayName( ban.isActive() ? Messages.BANS_ITEM_TEMP_TITLE.getTemplate() : Messages.BANS_ITEM_OVER_TEMP_TITLE.getTemplate() );
    }

    // Create lore with all information about this ban
    SimpleDateFormat stampF = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );
    OfflinePlayer creator = Bukkit.getOfflinePlayer( ban.getCreator() );
    OfflinePlayer revoker = ban.getRevoker() == null ? null : Bukkit.getOfflinePlayer( ban.getRevoker() );

    meta.setLore( Messages.BANS_ITEM_LORE.applyPrefixlessML(
      ban.getReason(),
      creator != null ? creator.getName() : "unbekannt",
      stampF.format( ban.getCreationDate() ),
      ban.isPermanent() ? "§cPermanent" : "§dbis " + stampF.format( ban.getExpireDate() ),
      ban.isActive() ? "§cja" : "§2nein",
      ban.getIpAddresses().size() > 0 ? String.join( ",", ban.getIpAddresses() ) : "§2keine",
      ban.getRevoker() == null ? "§cnein" : "§2ja - " + ( revoker != null ? revoker.getName() : "unbeknant" ),
      ban.getRevokeReason() == null ? "§ckeiner" : ban.getRevokeReason()

    ) );

    // Return creation
    item.setItemMeta( meta );
    return item;
  }
}
