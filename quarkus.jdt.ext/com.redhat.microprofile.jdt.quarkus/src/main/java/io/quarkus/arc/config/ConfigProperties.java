package io.quarkus.arc.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allow configuration properties with a common prefix to be grouped into a
 * single class
 * 
 * <p>
 * This class is a copy/paste of
 * https://github.com/quarkusio/quarkus/blob/99a773f3a096a36c72caa152245093d10703519e/extensions/arc/runtime/src/main/java/io/quarkus/arc/config/ConfigProperties.java
 * </p>
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ConfigProperties {

    String UNSET_PREFIX = "<< unset >>";

    /**
     * If the default is used, the class name will be used to determine the proper prefix
     */
    String prefix() default UNSET_PREFIX;
}
