package at.sps.core.orm;

import lombok.Getter;

/**
 * Every mappable model must have an ID, this forces them, since a
 * {@link ModelMapper}'s model type can only be of this type
 */
public class MappableModel {

  @Getter
  private int ID;

}
