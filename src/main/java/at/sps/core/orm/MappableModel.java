package at.sps.core.orm;

import lombok.Getter;
import lombok.Setter;

/**
 * Every mappable model must have an ID, this forces them, since a
 * {@link ModelMapper}'s model type can only be of this type
 */
public class MappableModel {

  @Getter @Setter
  private int ID;

}
