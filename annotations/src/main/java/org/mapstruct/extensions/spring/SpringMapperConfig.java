package org.mapstruct.extensions.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or interface as configuration source for the Spring extension. There can be only
 * <em>one</em> annotated type in each compiled module.
 *
 * @author Raimund Klein
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface SpringMapperConfig {
  /**
   * The package name for the generated Adapter between the MapStruct mappers and Spring's {@link
   * org.springframework.core.convert.ConversionService}. If omitted or empty, the package name will
   * be the same as the one for the annotated type.
   *
   * @return The package name for the generated Adapter.
   */
  String conversionServiceAdapterPackage() default "";

  /**
   * The class name for the generated Adapter between the MapStruct mappers and Spring's {@link
   * org.springframework.core.convert.ConversionService}.
   *
   * @return The class name for the generated Adapter.
   */
  String conversionServiceAdapterClassName() default "ConversionServiceAdapter";

  /**
   * The bean name for the Spring {@link org.springframework.core.convert.ConversionService} to use.
   *
   * @return The bean name for the Spring {@link
   *     org.springframework.core.convert.ConversionService}.
   */
  String conversionServiceBeanName() default "";

  /**
   * To set if the Lazy annotation will be added to the ConversionService's usage in the
   * ConversionServiceAdapter. Defaults to true.
   *
   * @return true to add the Lazy annotation
   */
  boolean lazyAnnotatedConversionServiceBean() default true;

  /**
   * Indicates whether to generate a {@code ConverterScan} when using {@link
   * #conversionServiceBeanName()}. Has no effect when {@link #conversionServiceBeanName()} is not
   * set.
   *
   * @return {@code true} - Generate {@code ConverterScan}, {@code false} - otherwise
   */
  boolean generateConverterScan() default false;

  /**
   * Additional {@link ExternalConversion conversions} which should be made available through the
   * generated Adapter.
   *
   * @return Additional {@link ExternalConversion conversions} which should be made available
   *     through the generated Adapter.
   */
  ExternalConversion[] externalConversions() default {};
}
