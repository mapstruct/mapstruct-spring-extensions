package org.mapstruct.extensions.spring.test;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MapStructConverterScans {
    MapStructConverterScan[] value();
}
