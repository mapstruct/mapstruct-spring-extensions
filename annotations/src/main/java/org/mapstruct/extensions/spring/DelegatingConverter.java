package org.mapstruct.extensions.spring;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the system should generate a delegating {@link
 * org.springframework.core.convert.converter.Converter} that will call the annotated method in its
 * own {@link org.springframework.core.convert.converter.Converter#convert(Object)}.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface DelegatingConverter {}
