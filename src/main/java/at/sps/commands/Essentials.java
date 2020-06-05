package at.sps.commands;

import at.sps.core.GlobalConstants;
import at.sps.core.shortcmds.ShortCommand;
import at.sps.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Essentials {

    private Map<Player, Player> msgPartners;

    /**
     * Essential commands holder, create all maps and lists
     */
    public Essentials() {
        this.msgPartners = new HashMap<>();
    }

    /**
     * Command: msg
     * Used to send messages between players privately
     */
    @ShortCommand(command = "msg", terminalDeny = true)
    private void onMessage(Player sender, String[] args) {

        // No recipient or no message specified
        if (args.length <= 1) {
            sender.sendMessage(GlobalConstants.PREFIX + "§cBenutze: /msg <Empfänger> <Nachricht>");
            return;
        }

        // Make sure the recipient is online
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(GlobalConstants.PREFIX + "§cDer Spieler '" + args[0] + "' ist nicht online!");
            return;
        }

        // Send out messages
        String msg = Utils.concatArgs(args, 1);
        sender.sendMessage(GlobalConstants.PREFIX + "§6Du §7» §6" + target.getDisplayName() + "§7: " + msg);
        target.sendMessage(GlobalConstants.PREFIX + "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg);

        // Keep last recipient in buffer
        this.msgPartners.put(sender, target);
        this.msgPartners.put(target, sender);
    }

    /**
     * Command: r
     * Used to reply to the last received message
     */
    @ShortCommand(command = "r", terminalDeny = true)
    private void onReply(Player sender, String[] args) {

        // No recipient in buffer yet
        Player partner = this.msgPartners.get(sender);
        if (partner == null) {
            sender.sendMessage(GlobalConstants.PREFIX + "§cDir hat noch niemand geschrieben, dem du antworten kannst!");
            return;
        }

        // Recipient went offline
        if (!partner.isOnline()) {
            sender.sendMessage(GlobalConstants.PREFIX + "§cDer Spieler '" + partner.getDisplayName() + "' ist in der Zwischenzeit offline gegangen!");
            return;
        }

        // No message specified
        if (args.length == 0) {
            sender.sendMessage(GlobalConstants.PREFIX + "§cBenutze: /r <message>");
            return;
        }

        // Send out messages
        String msg = Utils.concatArgs(args, 0);
        sender.sendMessage(GlobalConstants.PREFIX + "§6Du §7» §6" + partner.getDisplayName() + "§7: " + msg);
        partner.sendMessage(GlobalConstants.PREFIX + "§6" + sender.getDisplayName() + " §7» §6Dich§7: " + msg);
    }

    /**
     * Command: gm
     * Used to change the gamemode of the player
     */
    @ShortCommand(command = "gm")
    private void changeGameMode(Player sender, String[] args) {
        if( sender.hasPermission( "sps.gm")) {
            Player target = sender;
            GameMode gameMode = null;
            if( args.length >= 1) {
                gameMode = parseGameMode( tryParseInt( args[0] ) );
            }
            if( args.length >= 2 ) {
                target = Bukkit.getPlayer( args[1] );
            }
            if( gameMode != null) {
                target.setGameMode( gameMode );
            }
            else {
                sender.sendMessage( "Du hast kein gültiges Argument angegeben");
                // format this text -> so it's prettier -> colors
            }
        }
    }

    /**
     * Parse a string to an integer -> default 0
     * @param value the string to parse
     * @return the parsed int or -1 if an error occurred
     */
    private int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch ( Exception ex ) {

        }
        return -1;
    }

    /**
     * Parse an integer to GameMode Enum
     * @param mode
     * @return
     */
    private GameMode parseGameMode(int mode) {
        for( GameMode gameMode : GameMode.values()) {
            if( gameMode.getValue() == mode) {
                return gameMode;
            }
        }
        return null;
    }

    /**
     * Change the setAllowFly state of player
     * @param sender
     * @param args
     */
    @ShortCommand(command = "fly")
    private void changeFly(Player sender, String[] args) {
        if( sender.hasPermission( "sps.fly" )) {
            sender.setAllowFlight(!sender.getAllowFlight());
        }
    }
}
