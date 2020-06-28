package at.sps.core.orm;

import lombok.Getter;

import java.lang.reflect.Field;

public class ColInfo {

  @Getter
  private final String name, type, length;

  @Getter
  private final boolean unique, nullable;

  @Getter
  private final Field target;

  /**
   * Represents information about a mapped column for r/w, contains SQL column
   * informations and the model target field reference for data reading
   * @param name Name of the column
   * @param type Datatype of the column
   * @param length Length of the datatype
   * @param unique If this is a unique key
   * @param target Target field for reflect use
   */
  public ColInfo( String name, String type, String length, boolean unique, boolean nullable, Field target ) {
    this.name = name;
    this.type = type;
    this.length = length;
    this.unique = unique;
    this.nullable = nullable;
    this.target = target;
  }
}
