package org.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a banking operation that should be audited.
 * A processor (Step 58) reads this via reflection and
 * logs the operation automatically.
 *
 * RUNTIME retention → readable by reflection.
 * METHOD target → only goes on methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    String level() default "INFO";
    boolean sensitive() default false;
}