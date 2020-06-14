package at.sps.core.utils;

import lombok.Getter;

public enum LogLevel {

  INFO( "§a" ),
  WARNING( "§6" ),
  ERROR( "§c" ),
  DEBUG( "§b" );

  @Getter
  private final String color;

  LogLevel( String color ) {
    this.color = color;
  }
}
