package at.sps.core.shortcmds;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes how a command is structured internally
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface ShortCommand {

  // The command itself
  String command();

  // Whether or not this command should be denied for console execution
  // If terminal executes, Player is going to be null
  boolean terminalDeny() default false;

}
