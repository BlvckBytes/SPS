package at.sps.dao;

/**
 * Used to mark a field as a dao field, used for r/w to storage
 */
public @interface DaoField {

  /**
   * The name of the corresponding field name in storage
   */
  String storageName() default "";

}
