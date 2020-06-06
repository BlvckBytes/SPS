package at.sps.dao;

/**
 * Used to mark a field's internal fields as dao fields, used for r/w to storage
 */
public @interface DaoInternalFields {

  /**
   * List of internal field names
   */
  String[] fields() default {};

}
