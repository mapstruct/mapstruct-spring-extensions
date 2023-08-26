package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.time.Clock;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ConverterScansGenerator extends Generator {
  public ConverterScansGenerator(final Clock clock) {
    super(clock);
  }

  @Override
  protected JavaFile.Builder modifyDefaultFileBuilder(final JavaFile.Builder javaFileBuilder) {
    return javaFileBuilder
        .addStaticImport(ElementType.class, "TYPE")
        .addStaticImport(RetentionPolicy.class, "RUNTIME");
  }

  @Override
  protected TypeSpec createMainTypeSpec(final ConversionServiceAdapterDescriptor descriptor) {
    final var converterScansClassTypeSpecBuilder =
        TypeSpec.annotationBuilder(descriptor.getConverterScansClassName()).addModifiers(PUBLIC);
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(converterScansClassTypeSpecBuilder::addAnnotation);
    return converterScansClassTypeSpecBuilder
        .addAnnotation(RETENTION_RUNTIME_ANNOTATION_SPEC)
        .addAnnotation(TARGET_TYPE_ANNOTATION_SPEC)
        .addAnnotation(Documented.class)
        .addMethod(
            MethodSpec.methodBuilder("value")
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(ArrayTypeName.of(descriptor.getConverterScanClassName()))
                .build())
        .build();
  }
}
