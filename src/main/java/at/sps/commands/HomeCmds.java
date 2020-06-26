package at.sps.commands;

import at.sps.core.conf.Messages;
import at.sps.core.orm.ActionResult;
import at.sps.core.orm.mappers.HomeMapper;
import at.sps.core.orm.models.Home;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.ComplexMessage;
import at.sps.core.utils.ComplexPart;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeCmds extends CommandBase {

    private final SimpleDateFormat homeDateFormat;

    public HomeCmds() {
        homeDateFormat = new SimpleDateFormat( "dd.MM.yyyy" );
    }

    /**
     * Command: home
     * Usage: /home name
     * Used to teleport to a previously set home
     */
    @ShortCommand( command = "home", terminalDeny = true )
    private void onHome( Player sender, String[] args ) {
        // No name provided
        if( args.length != 1 ) {
            sender.sendMessage( Messages.USAGE.apply( "/home <Name>" ) );
            return;
        }

        // Fetch home from DB and make sure it exists
        Home target = HomeMapper.getInst().getByName( sender.getUniqueId(), args[ 0 ] );
        if( target == null ) {
            sender.sendMessage( Messages.HOME_NON_EXISTENT.apply( args[ 0 ] ) );
            return;
        }

        // Teleport to home, notify
        sender.teleport( target.getLocation() );
        sender.sendMessage( Messages.HOME_TELEPORT.apply( target.getName() ) );
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
            sender.sendMessage( Messages.USAGE.apply( "/sethome <Name>" ) );
            return;
        }

        int allowed = getMaxAllowed( sender );
        int has = HomeMapper.getInst().getHomeCount( sender.getUniqueId() );

        // Has reached his home limit
        if( has + 1 > allowed ) {
            sender.sendMessage( Messages.HOME_LIMIT.apply( allowed ) );
            return;
        }

        // Create and add home
        Home added = new Home( sender.getUniqueId(), args[ 0 ], sender.getLocation(), System.currentTimeMillis() );
        ActionResult result = HomeMapper.getInst().addHome( added );

        switch ( result ) {
            // Home added
            case OK:
                sender.sendMessage( Messages.HOME_CREATED.apply( added.getName() ) );
                break;

            // Name is already existent
            case ALREADY_EXISTENT:
                sender.sendMessage( Messages.HOME_EXISTING.apply( added.getName() ) );
                break;

            // Error
            default:
                sender.sendMessage( Messages.INTERNAL_ERR.apply( "HOMECR->" + result ) );
                break;
        }
    }

    /**
     * Find the max. number of homes a player may own
     * @param target Target player to check
     * @return Number of allowed homes
     */
    private int getMaxAllowed( Player target ) {
        // Has the permission to create as many as he likes
        if( target.isOp() || target.hasPermission( "sps.homes.*" ) )
            return Integer.MAX_VALUE;

        // Loop all permission attachments of the player
        int allowed = 0;
        for( PermissionAttachmentInfo permInf : target.getEffectivePermissions() ) {
            String perm = permInf.getPermission();

            // Not the target permission (home limiter)
            if( !perm.startsWith( "sps.homes." ) )
                continue;

            // Substring from the second dot to the end, make sure it's an integer
            Integer amount = tryParseInt( perm.substring( perm.indexOf( ".", perm.indexOf( "." ) + 1 ) + 1 ) );
            if( amount == null )
                continue;

            // Update the value if current is higher
            if( amount > allowed )
                allowed = amount;
        }

        return allowed;
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
            sender.sendMessage( Messages.USAGE.apply( "/delhome <Name>" ) );
            return;
        }

        // Try to delete the existing home from the database
        ActionResult result = HomeMapper.getInst().removeHome( sender.getUniqueId(), args[ 0 ] );

        switch ( result ) {
            // Home deleted
            case OK:
                sender.sendMessage( Messages.HOME_DELETED.apply( args[ 0 ] ) );
                break;

            // Name is not existent
            case NON_EXISTENT:
                sender.sendMessage( Messages.HOME_NON_EXISTENT.apply( args[ 0 ] ) );
                break;

            // Error
            default:
                sender.sendMessage( Messages.INTERNAL_ERR.apply( "HOMEDL->" + result ) );
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
        List< Home > homes = HomeMapper.getInst().listHomes( sender.getUniqueId() );

        // No homes found
        if( homes.size() == 0 ) {
            sender.sendMessage( Messages.HOMES_LIST.apply() + Messages.HOMES_NONE.getTemplate() );
            return;
        }

        // Build complex message
        ComplexMessage msg = new ComplexMessage( new ComplexPart( Messages.HOMES_LIST.apply(), "", "", true ) );

        // Build home list
        for( int i = 0; i < homes.size(); i++ ) {
            // Append delimiter on everything but the first element
            if( i != 0 )
                msg.append( new ComplexPart( Messages.HOME_DELIMITER.getTemplate(), "", "", true ) );

            // Build hover string from home data
            Home currHome = homes.get( i );
            String date = homeDateFormat.format( new Date( currHome.getCreationDate() ) );
            Location loc = currHome.getLocation();
            String locCords = "(" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
            String hover = Messages.HOMES_LIST_HOVER.applyPrefixless( date, locCords );

            // Append part
            String text = Messages.HOME_COLOR.getTemplate() + currHome.getName();
            msg.append( new ComplexPart( text, hover, "/home " + currHome.getName(), true ) );
        }

        // Send list of available homes to the player
        msg.send( sender );
    }
}
