package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static javax.lang.model.element.Modifier.PUBLIC;

public class AutoMapSpringConfigGenerator {
    private final Clock clock;

    public AutoMapSpringConfigGenerator(final Clock clock) {
        this.clock = clock;
    }

    private final String CLASS_NAME = "AutoMapSpringConfig";
    public void write(String packageNName, Writer out) {
        try {
            JavaFile.builder(
                    packageNName,
                    createTypeSpec())
                    .build()
                    .writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private TypeSpec createTypeSpec() {
        return TypeSpec.interfaceBuilder(CLASS_NAME)
                .addModifiers(PUBLIC)
                .addAnnotation(buildGeneratedAnnotationSpec())
                .addAnnotation(buildGeneratedMapperConfigAnnotationSpec())
                .addAnnotation(buildGeneratedSpringMapperConfigAnnotationSpec())
                .build();
    }


    private AnnotationSpec buildGeneratedAnnotationSpec() {
        return AnnotationSpec.builder(ClassName.get("javax.annotation", "Generated"))
                .addMember("value", "$S", CLASS_NAME)
                .addMember("date", "$S", DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now(clock)))
                .build();
    }

    private AnnotationSpec buildGeneratedMapperConfigAnnotationSpec() {
        ClassName autoMapperAdapterClass = ClassName.get("org.mapstruct.extensions.spring",
                "ConversionServiceAdapter");

        CodeBlock uses = CodeBlock.builder().add("{$T.class}",autoMapperAdapterClass).build();

        return AnnotationSpec.builder(ClassName.get("org.mapstruct", "MapperConfig"))
                .addMember("componentModel", "$S", "spring")
                .addMember("uses", uses)
                .build();
    }
    private AnnotationSpec buildGeneratedSpringMapperConfigAnnotationSpec() {

        return AnnotationSpec.builder(
                ClassName.get("org.mapstruct.extensions.spring",
                        "SpringMapperConfig"))
                .build();
    }

}
