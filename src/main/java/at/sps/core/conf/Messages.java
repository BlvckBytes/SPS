package at.sps.core.conf;

import at.sps.core.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public enum Messages {

  PREFIX( "&5&lSPS &7┃ " ),
  PANEL_SPACER( "&8&m----------------&r&8( &5${0} &8)&8&m----------------" ),
  NO_PERM( "&7Dir fehlt das Recht &c${0} &7um diese Aktion zu tätigen!" ),
  PLAYER_NOT_EXISTING( "&7Der Spieler &c${0} &7existiert nicht!" ),
  PLAYER_NOT_ONLINE( "&7Der Spieler &c${0} &7ist nicht online!" ),
  PLAYER_ONLY( "&7Der Befehl &c${0}&7 kann nur als &cSpieler &7ausgeführt werden!" ),
  INTERNAL_ERR( "&4Es trat ein interner Fehler auf, bitte melde den ERRCODE ${0}!" ),
  USAGE( "&7Bitte benutze: &c${0}" ),
  MSG_OUT( "&6Du &7» &6${0}&7: ${1}" ),
  MSG_IN( "&6${0} &7» &6Dich&7: ${1}" ),
  MSG_SPY( "&6&o${0} &7&o» &6&o${1}&7&o: ${2}" ),
  MSG_SPY_SUBSCRIBED( "&7Du &dbist &7nun ein MSG-Spion!" ),
  MSG_SPY_UBSUBSCRIBED( "&7Du bist &dkein &7MSG-Spion mehr!" ),
  MSG_SELF( "&7Du kannst &cdir selbst &7keine Nachrichten senden!" ),
  SPS_LIST( "&7Registrierte Befehle: &d${0}" ),
  SPS_DELIMITER( "&7, &d" ),
  NO_MSG_PARTNER( "&7Du hast leider noch &ckeine &7Nachrichten zum beantworten erhalten!" ),
  WENT_OFFLINE( "&7Der Spieler &c${0} &7ist leider offline gegangen!" ),
  INVALID_GM( "&7Du hast einen &cungültigen &7GameMode angegeben (${0})!" ),
  GM_CHANGED( "&7Du hast deinen &dGameMode &7auf &d${0} &7geändert!" ),
  GM_CHANGED_TARGET( "&7Dein &dGameMode §7wurde von &d${0} &7auf &d${1} &7geändert!" ),
  GM_CHANGED_EXECUTOR( "&7Du hast den &dGameMode &7von &d${0} &7erfolgreich auf &d${1} &7geändert!" ),
  FLY_CHANGED( "&7Du hast deinen &dFlugmodus &7auf &d${0} &7geändert!" ),
  FLY_CHANGED_TARGET( "&7Dein &dFlugmodus §7wurde von &d${0} &7auf &d${1} &7geändert!" ),
  FLY_CHANGED_EXECUTOR( "&7Du hast den &dFlugmodus &7von &d${0} &7erfolgreich auf &d${1} &7geändert!" ),
  HOME_NON_EXISTENT( "&7Das Home &d${0} &7existiert nicht!" ),
  HOME_TELEPORT( "&7Du wurdest erfolgreich zum Home &d${0} &7teleportiert!" ),
  HOME_EXISTING( "&7Das Home &c${0} &7existiert bereits!" ),
  HOME_CREATED( "&7Du hast das Home &d${0} &7erfolgreich erstellt!" ),
  HOME_DELETED( "&7Du hast das Home &d${0} &7erfolgreich gelöscht!" ),
  HOME_LIMIT( "&7Du hast dein &cLimit &7an &c${0} &7Homes erreicht!" ),
  HOMES_LIST( "&7Deine Homes: " ),
  HOMES_LIST_HOVER( "&7Erstellt am: &d${0}&7, Ort: &d${1}" ),
  HOME_COLOR( "&d" ),
  HOMES_NONE( "&cKeine Homes gesetzt!" ),
  HOME_DELIMITER( "&7, " ),
  KIT_NON_EXISTENT( "&7Das Kit &c${0} &7existiert nicht!" ),
  KIT_EXISTING( "&7Das Kit &c${0} &7existiert bereits!" ),
  KIT_RECEIVED( "&7Du hast das Kit &d${0} &7erhalten!" ),
  KIT_DROPPED( "&7Du hattest leider &cnicht genug &7Platz im Inventar, daher wurden &c${0} &7Items &cgedroppt&7!" ),
  KIT_CREATED( "&7Das Kit &d${0} &7wurde erfolgreich mit &d${1} &7Items angelegt!" ),
  KIT_NO_ITEMS( "&7Du hast &ckeine Items &7im Inventar, um ein Kit zu erstellen!" ),
  KIT_WRONG_COOLDOWN( "&7Bitte gib einen &cganzzahligen &7Coldown in Sekunden an!" ),
  KIT_DELETED( "&7Das Kit &d${0} &7wurde erfolgreich gelöscht!" ),
  KIT_UPDATED( "&7Das Kit &d${0} &7wurde erfolgreich geändert!" ),
  KIT_LIST( "&7Verfügbare Kits: &d" ),
  KIT_DELIMITER( "&7, " ),
  KIT_COLOR( "&d" ),
  KIT_NONE( "&cKeine Kits erstellt!" ),
  KIT_LIST_HOVER( "&7Items: &d${0}&7, &7Beschreibung: &d${1}" ),
  KIT_COOLDOWN( "&7Du musst noch &c${0} &7warten, bevor du das Kit &d${1} &7erneut anfordern kannst!" ),
  WARP_NON_EXISTING( "&7Der Warp &c${0} &7existiert nicht!" ),
  WARP_TELEPORT( "&7Du wurdest erfolgreich zum Warp &d${0} &7teleportiert!" ),
  WARP_CREATED( "&7Der Warp &d${0} &7wurde erfolgreich erstellt!" ),
  WARP_EXISTING( "&7Der Warp &c${0} &7existiert bereits!" ),
  WARP_DELETED( "&7Der Warp &d${0} &7wurde erfolgreich gelöscht!" ),
  WARP_UPDATED( "&7Der Warp &d${0} &7wurde erfolgreich geändert!" ),
  WARP_LIST( "&7Verfügbare Warps: " ),
  WARP_LIST_HOVER( "&7Ort: &d${1}" ),
  WARP_DELIMITER( "&7, " ),
  WARP_COLOR( "&d" ),
  WARP_NONE( "&cKeine Warps verfügbar!" ),
  PAGER_PREV_TITLE( "&8« &dVorherige Seite &8»" ),
  PAGER_PREV_LORE(
    "&7Klicken, um auf die vorherige",
    "&7Seite zu gelangen"
  ),
  PAGER_NEXT_TITLE( "&8« &dNächste Seite &8»" ),
  PAGER_NEXT_LORE(
    "&7Klicken, um auf die nächste",
    "&7Seite zu gelangen"
  ),
  PAGER_PAGE_TITLE( "&8« &dSeite ${0}&8/&d${1} &8»" ),
  PAGER_PAGE_LORE(
    "&7Dieses Item zeigt Dir, auf",
    "&7welcher Seite Du bist"
  ),
  BANS_PAGE_TITLE( "&8${0}" ),
  BANS_NO_PREV( "&7Du bist bereits an der &cersten Seite &7angekommen!" ),
  BANS_NO_NEXT( "&7Du bist bereits an der &cletzten Seite &7angekommen!" ),
  BANS_ITEM_LORE(
    "&7Grund: &d${0}",
    "&7Ersteller: &d${1}",
    "&7Erstellt am: &d${2}",
    "&7Dauer: ${3}",
    "&7Aktiv: ${4}",
    "&7IPs: ${5}",
    "&7Aufgehoben: ${6}",
    "&7Aufhebegrund: &d${7}"
  ),
  BANS_ITEM_PERM_TITLE( "&4Permanenter Bann" ),
  BANS_ITEM_TEMP_TITLE( "&6Temporärer Bann" ),
  BANS_ITEM_OVER_PERM_TITLE( "&2Permanenter Bann" ),
  BANS_ITEM_OVER_TEMP_TITLE( "&2Permanenter Bann" );

  @Setter @Getter
  private String[] internalTemplate;

  /**
   * Internal constructor to set the template string corresponding to the enum
   */
  Messages( String... template ) {
    this.internalTemplate = template;
  }

  /**
   * Apply placeholder texts to the message's markings, like ${x} where x
   * is the index of the placeholder from parameters
   * @param placeholders Array of values to put in placeholders
   * @return String with translated colors and applied placeholders
   */
  public String applyPrefixless( Object... placeholders ) {
    // Apply prefixless for all lines, joined by newline char
    return applyPrefixless( String.join( "\n", internalTemplate ), placeholders );
  }

  /**
   * Apply placeholder texts to the message's markings, like ${x} where x
   * is the index of the placeholder from parameters
   * @param input String to apply to
   * @param placeholders Array of values to put in placeholders
   * @return String with translated colors and applied placeholders
   */
  private String applyPrefixless( String input, Object... placeholders ) {
    // Apply placeholders by their index
    int c = 0;
    for( Object ph : placeholders ) {
      input = input.replace( "${" + c + "}", ph.toString() );
      c++;
    }

    // Apply prefix if specified by the boolean
    return Utils.translateColors( input );
  }

  /**
   * Apply prefixless templates and respond with multiple lines
   * @param placeholders Placeholders to apply in order
   * @return List of strings with replaced lines
   */
  public List< String > applyPrefixlessML( Object... placeholders ) {
    List< String > buf = new ArrayList<>();

    // Apply the placeholders to every line
    for( String line : internalTemplate )
      buf.add( applyPrefixless( line, placeholders ) );

    return buf;
  }

  /**
   * Getter for the template only, replaces colors
   * @return Template string of enum
   */
  public String getTemplate() {
    return Utils.translateColors( String.join( "\n", internalTemplate ) );
  }

  /**
   * Getter for the template only, replaces colors
   * @return Template lines of enum
   */
  public List< String > getTemplateML() {
    List< String > buf = new ArrayList<>();

    // Apply the color translation to every line
    for( String line : internalTemplate )
      buf.add( Utils.translateColors( line ) );

    return buf;
  }

  /**
   * This just calls {@link #applyPrefixless(Object...)} and adds the prefix
   * @param placeholders Array of values to put in placeholders
   * @return String with translated colors and applied placeholders plus prefix
   */
  public String apply( Object... placeholders ) {
    StringBuilder buf = new StringBuilder();

    // Apply the prefix and placeholders to every line
    for( String line : internalTemplate )
      buf.append( PREFIX.getTemplate() ).append( applyPrefixless( line, placeholders ) );

    return Utils.translateColors( buf.toString() );
  }
}
