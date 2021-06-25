package org.mapstruct.extensions.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows the specification of a conversion that is available via the {@link
 * org.springframework.core.convert.ConversionService ConversionService}, but is <em>not</em>
 * declared as a MapStruct mapper within the scope of the {@link SpringMapperConfig}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ExternalConversion {
  Class<?> sourceType();

  Class<?> targetType();
}
