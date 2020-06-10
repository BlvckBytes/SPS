package at.sps.core.orm;

import lombok.Getter;

import java.lang.reflect.Field;

public class ColInfo {

  @Getter
  private final String name, type, length;

  @Getter
  private final boolean unique;

  @Getter
  private final Field target;

  /**
   * Represents information about a mapped column for r/W
   * @param name Name of the column
   * @param type Datatype of the column
   * @param length Length of the datatype
   * @param unique If this is a unique key
   * @param target Target field for reflect use
   */
  public ColInfo( String name, String type, String length, boolean unique, Field target ) {
    this.name = name;
    this.type = type;
    this.length = length;
    this.unique = unique;
    this.target = target;
  }
}
