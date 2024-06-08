package org.mapstruct.extensions.spring.example.externalconversions;

import java.sql.Blob;
import java.util.Locale;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.ExternalConversion;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring")
@SpringMapperConfig(
    externalConversions = {
      @ExternalConversion(sourceType = String.class, targetType = Locale.class),
      @ExternalConversion(sourceType = Blob.class, targetType = byte[].class, adapterMethodName = "blob2Bytes")
    })
public interface MapstructConfig {}
