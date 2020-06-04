package at.sps.core;

import at.sps.core.utils.Utils;

public enum GlobalConstants {

  // Prefix, used in every message
  PREFIX( "&5&lSPS &7| " ),

  // No permission to do this
  NO_PERM( "&cDu besitzt die dazu notwendigen Rechte leider nicht!" ),

  // Player only commands get blocked in the console
  PLAYER_ONLY( "This command is only usable ingame!" );

  private final String value;

  /**
   * Constructor for enum values, every field has it's
   * corresponding string value with #toString
   * @param value Value of field
   */
  GlobalConstants( String value ) {
    this.value = value;
  }

  /**
   * The toString method returns the field's value for use
   * in concatenation f.e.
   * @return Field value
   */
  @Override
  public String toString() {
    return Utils.translateColors( value );
  }
}
