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

  // Aliases that should also call on this receiver
  String[] aliases() default {};

  // Whether or not this command should be denied for console execution
  // If terminal executes, ConsoleSender will be the sender
  boolean terminalDeny() default false;

}
