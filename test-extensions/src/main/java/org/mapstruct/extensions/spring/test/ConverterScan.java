package org.mapstruct.extensions.spring.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.context.annotation.ScopedProxyMode.DEFAULT;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.mapstruct.extensions.spring.runtime.ConverterRegistrationConfiguration;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.annotation.AliasFor;

@ComponentScan
@Target(TYPE)
@Import({ConverterRegistrationConfiguration.class, ConversionServiceProvider.class})
@Documented
@Retention(RUNTIME)
@Repeatable(ConverterScans.class)
public @interface ConverterScan {
  @AliasFor(annotation = ComponentScan.class, attribute = "value")
  String[] value() default {};

  @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
  String[] basePackages() default {};

  @AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
  Class<?>[] basePackageClasses() default {};

  @AliasFor(annotation = ComponentScan.class, attribute = "nameGenerator")
  Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

  @AliasFor(annotation = ComponentScan.class, attribute = "scopeResolver")
  Class<? extends ScopeMetadataResolver> scopeResolver() default
      AnnotationScopeMetadataResolver.class;

  @AliasFor(annotation = ComponentScan.class, attribute = "scopedProxy")
  ScopedProxyMode scopedProxy() default DEFAULT;

  @AliasFor(annotation = ComponentScan.class, attribute = "useDefaultFilters")
  boolean useDefaultFilters() default true;

  @AliasFor(annotation = ComponentScan.class, attribute = "includeFilters")
  Filter[] includeFilters() default {};

  @AliasFor(annotation = ComponentScan.class, attribute = "excludeFilters")
  Filter[] excludeFilters() default {};

  @AliasFor(annotation = ComponentScan.class, attribute = "lazyInit")
  boolean lazyInit() default false;
}
