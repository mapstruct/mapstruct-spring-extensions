package org.mapstruct.extensions.spring.example.externalconversions;

import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.ExternalConversion;
import org.mapstruct.extensions.spring.SpringMapperConfig;

import java.sql.Blob;
import java.util.Locale;

@MapperConfig(componentModel = "spring")
@SpringMapperConfig(
    externalConversions = {
      @ExternalConversion(sourceType = String.class, targetType = Locale.class),
      @ExternalConversion(sourceType = Blob.class, targetType = byte[].class)
    })
public interface MapstructConfig {}
