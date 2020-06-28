package at.sps.core.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a mapped column and thus can only annotate fields
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface MapperColumn {

  /**
   * Name of the column in database, keep empty to use the field's name
   */
  String name() default "";

  /**
   * Whether or not this column is unique, thus a key
   */
  boolean key() default false;

  /**
   * Length of the field, if neccesary, specifies the text in the backets TYPE()
   */
  String length() default "";

  /**
   * Whether or not this field is nullable
   */
  boolean nullable() default false;
}
