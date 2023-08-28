package org.mapstruct.extensions.spring.converter;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.annotation.Generated;

@Generated(
        value = "org.mapstruct.extensions.spring.converter.ConverterScansGenerator",
        date = "2020-03-29T15:21:34.236Z")
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface ConverterScans {
    ConverterScan[] value();
}
