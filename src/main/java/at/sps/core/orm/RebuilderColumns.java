package at.sps.core.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for object fields that contain further target
 * fields that need to be mapped aswell
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface RebuilderColumns {

  /**
   * Target field names as in the object
   */
  String[] fields() default {};

  /**
   * Column names, in the same order as fields
   */
  String[] names() default {};

  /**
   * Unique key states, in the same order as fields
   */
  boolean[] uniques() default {};

  /**
   * Column lengths in brackets like TYPE(), in the same order as fields
   */
  String[] lengths() default {};

}
