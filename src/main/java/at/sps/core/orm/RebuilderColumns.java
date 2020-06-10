package at.sps.core.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface RebuilderColumns {

  String[] fields() default {};

  String[] names() default {};

  boolean[] uniques() default {};

  String[] lengths() default {};

}
