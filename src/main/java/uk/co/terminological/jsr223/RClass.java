package uk.co.terminological.jsr223;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Will populate data in DESCRIPTION file 
 * @author terminological
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RClass {
	
	String packageName();
	String title();
	String licence();
	String version();
	String description() default "";
	String[] authors() default {};
	String[] imports() default {};
	String[] suggests() default {};
	
}
