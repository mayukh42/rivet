package org.mayukh.rivet.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mayukh42 on 11/6/17.
 *
 * Annotation to be used in Dependency Injection of a field
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Riveted {
}
