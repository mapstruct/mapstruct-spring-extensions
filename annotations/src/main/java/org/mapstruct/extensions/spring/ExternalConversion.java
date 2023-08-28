package org.mapstruct.extensions.spring;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows the specification of a conversion that is available via the {@link
 * org.springframework.core.convert.ConversionService ConversionService}, but is <em>not</em>
 * declared as a MapStruct mapper within the scope of the {@link SpringMapperConfig}.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface ExternalConversion {
  Class<?> sourceType();

  Class<?> targetType();
}
