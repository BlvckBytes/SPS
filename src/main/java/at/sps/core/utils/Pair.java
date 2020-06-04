package at.sps.core.utils;

import lombok.Getter;

public class Pair< A, B > {

  @Getter
  private final A key;

  @Getter
  private final B value;

  /**
   * Create a new pair which consists of a key and it's corresponding value
   * @param key Key of pair
   * @param value Value of pair's key
   */
  public Pair( A key, B value ) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    return "Pair(" + key + ", " + value + ")";
  }
}
