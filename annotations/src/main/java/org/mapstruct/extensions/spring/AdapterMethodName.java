package org.mapstruct.extensions.spring;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Overrides the default method name generated in the Adapter class. To be used exclusively on the
 * {@link org.springframework.core.convert.converter.Converter#convert(Object)} method in a {@link
 * org.springframework.core.convert.converter.Converter} annotated as {@code @Mapper}.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface AdapterMethodName {
  /**
   * The method name to be used instead of the default.
   *
   * @return The method name to be used instead of the default.
   */
  String value();
}
