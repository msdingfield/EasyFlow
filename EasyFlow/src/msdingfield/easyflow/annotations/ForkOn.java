package msdingfield.easyflow.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation added to input which will create multiple instances of the 
 * operation to handle items in a collection in parallel.
 * 
 * @author Matt
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForkOn {
}
