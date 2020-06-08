package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.Main;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.storage.ActionResult;
import at.sps.mappers.HomeMapper;
import at.sps.model.Home;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCmd {

    /**
     * Command: home
     * Usage: /home name
     * Used to teleport to a previously set home
     */
    @ShortCommand( command = "home", terminalDeny = true )
    private void onHome( Player sender, String[] args ) {

        // No name provided
        if( args.length != 1 ) {
            sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /home <Name>" );
            return;
        }

        // Fetch home from DB
        HomeMapper hm = new HomeMapper( Main.getDb() );
        Home target = hm.getByName( sender.getUniqueId(), args[ 0 ] );

        // Home non existent
        if( target == null ) {
            sender.sendMessage( GlobalConstants.PREFIX + "§7Das home §d" + args[ 0 ] + " §7existiert nicht!" );
            return;
        }

        // Teleport to his home
        sender.teleport( target.getLocation() );
        sender.sendMessage( GlobalConstants.PREFIX + "§7Du wurdest erfolgreich zum home §d" + target.getName() + " §7teleportiert!" );
    }

    /**
     * Command: sethome
     * Usage: /sethome name
     * Used to set a home with a name at the current location
     */
    @ShortCommand( command = "sethome", terminalDeny = true )
    private void onSethome( Player sender, String[] args ) {
        // No name provided
        if( args.length != 1 ) {
            sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /sethome <Name>" );
            return;
        }

        HomeMapper hm = new HomeMapper( Main.getDb() );
        Home added = new Home( sender.getUniqueId(), args[ 0 ], sender.getLocation() );
        ActionResult result = hm.addHome( added );

        switch ( result ) {

            // Home added
            case OK:
                sender.sendMessage( GlobalConstants.PREFIX + "§7Das home §d" + added.getName() + " §7wurde erfolgreich erstellt!" );
                break;

            // Name is already existent
            case ALREADY_EXISTENT:
                sender.sendMessage( GlobalConstants.PREFIX + "§7Das home §d" + added.getName() + " §7existiert bereits!" );
                break;

            // Error
            default:
                sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.INTERNAL_ERROR + " (" + result + ")" );
                break;
        }
    }

    /**
     * Command: delhome
     * Usage: /delhome name
     * Used to delete a previously set home
     */
    @ShortCommand( command = "delhome", terminalDeny = true )
    private void onDelhome( Player sender, String[] args ) {
        // No name provided
        if( args.length != 1 ) {
            sender.sendMessage( GlobalConstants.PREFIX + "§cBenutze: /delhome <Name>" );
            return;
        }

        // Try to delete the existing home from the database
        HomeMapper hm = new HomeMapper( Main.getDb() );
        ActionResult result = hm.removeHome( sender.getUniqueId(), args[ 0 ] );

        switch ( result ) {

            // Home deleted
            case OK:
                sender.sendMessage( GlobalConstants.PREFIX + "§7Das home §d" + args[ 0 ] + " §7wurde erfolgreich gelöscht!" );
                break;

            // Name is not existent
            case NON_EXISTENT:
                sender.sendMessage( GlobalConstants.PREFIX + "§7Das home §d" + args[ 0 ] + " §7existiert nicht!" );
                break;

            // Error
            default:
                sender.sendMessage( GlobalConstants.PREFIX.toString() + GlobalConstants.INTERNAL_ERROR + " (" + result + ")" );
                break;
        }
    }

    /**
     * Command: homes
     * Usage: /homes
     * Used to list all previously set homes
     */
    @ShortCommand( command = "homes", terminalDeny = true )
    private void onHomes( Player sender, String[] args ) {
        // Fetch all existing homes from the database
        HomeMapper hm = new HomeMapper( Main.getDb() );
        List< Home > homes = hm.listHomes( sender.getUniqueId() );

        // Build message
        StringBuilder sb = new StringBuilder( "§7Deine Homes: " );

        // No homes found
        if( homes.size() == 0 ) {
            sender.sendMessage( GlobalConstants.PREFIX + sb.toString() + "§cKeine Homes gesetzt!" );
            return;
        }

        // Build home list
        sb.append( "§d" ).append( homes.get( 0 ).getName() );
        for( int i = 1; i < homes.size(); i++ )
            sb.append( "§7, §d" ).append( homes.get( i ).getName() );

        // Send list of available homes to the player
        sender.sendMessage( GlobalConstants.PREFIX + sb.toString() );
    }
}
