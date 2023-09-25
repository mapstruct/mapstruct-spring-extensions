package org.mapstruct.extensions.spring.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface ConverterScans {
    ConverterScan[] value();
}
