package uk.co.terminological.jsr223;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Will populate data in NAMESPACE and .Rd files
 * @author terminological
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RMethod {

	String title();
	String description() default "";
	String[] details() default {};
	String[] aliases() default {};
	String[] imports() default {};
	String[] examples() default {};
	String returns() default "";
	
}
