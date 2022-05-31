package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.*;

public class ConversionServiceAdapterGenerator {
  private static final String CONVERSION_SERVICE_PACKAGE_NAME = "org.springframework.core.convert";
  private static final String CONVERSION_SERVICE_CLASS_NAME = "ConversionService";
  private static final String CONVERSION_SERVICE_FIELD_NAME = "conversionService";
  private static final String QUALIFIER_ANNOTATION_PACKAGE_NAME =
      "org.springframework.beans.factory.annotation";
  private static final String QUALIFIER_ANNOTATION_CLASSS_NAME = "Qualifier";
  private static final String LAZY_ANNOTATION_PACKAGE_NAME =
      "org.springframework.context.annotation";
  private static final String LAZY_ANNOTATION_CLASS_NAME = "Lazy";

  private final Clock clock;

  public ConversionServiceAdapterGenerator(final Clock clock) {
    this.clock = clock;
  }

  public void writeConversionServiceAdapter(
      final ConversionServiceAdapterDescriptor descriptor, final Writer out) {
    try {
      JavaFile.builder(
              descriptor.getAdapterClassName().packageName(),
              createConversionServiceTypeSpec(descriptor))
          .build()
          .writeTo(out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private TypeSpec createConversionServiceTypeSpec(
      final ConversionServiceAdapterDescriptor descriptor) {
    final FieldSpec conversionServiceFieldSpec = buildConversionServiceFieldSpec();
    return TypeSpec.classBuilder(descriptor.getAdapterClassName())
        .addModifiers(PUBLIC)
        .addAnnotation(buildGeneratedAnnotationSpec())
        .addAnnotation(ClassName.get("org.springframework.stereotype", "Component"))
        .addField(conversionServiceFieldSpec)
        .addMethod(buildConstructorSpec(descriptor, conversionServiceFieldSpec))
        .addMethods(buildMappingMethods(descriptor, conversionServiceFieldSpec))
        .build();
  }

  private static MethodSpec buildConstructorSpec(
      final ConversionServiceAdapterDescriptor descriptor,
      final FieldSpec conversionServiceFieldSpec) {
    final ParameterSpec constructorParameterSpec =
        buildConstructorParameterSpec(descriptor, conversionServiceFieldSpec);
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(constructorParameterSpec)
        .addStatement("this.$N = $N", conversionServiceFieldSpec, constructorParameterSpec)
        .build();
  }

  private static ParameterSpec buildConstructorParameterSpec(
      final ConversionServiceAdapterDescriptor descriptor,
      final FieldSpec conversionServiceFieldSpec) {
    final ParameterSpec.Builder parameterBuilder =
        ParameterSpec.builder(
            conversionServiceFieldSpec.type, conversionServiceFieldSpec.name, FINAL);
    if (StringUtils.isNotEmpty(descriptor.getConversionServiceBeanName())) {
      parameterBuilder.addAnnotation(buildQualifierAnnotation(descriptor));
    }
    if (Boolean.TRUE.equals(descriptor.isLazyAnnotatedConversionServiceBean())) {
      parameterBuilder.addAnnotation(buildLazyAnnotation());
    }
    return parameterBuilder.build();
  }

  private static AnnotationSpec buildQualifierAnnotation(
      ConversionServiceAdapterDescriptor descriptor) {
    return AnnotationSpec.builder(
            ClassName.get(QUALIFIER_ANNOTATION_PACKAGE_NAME, QUALIFIER_ANNOTATION_CLASSS_NAME))
        .addMember("value", "$S", descriptor.getConversionServiceBeanName())
        .build();
  }

  private static AnnotationSpec buildLazyAnnotation() {
    return AnnotationSpec.builder(
            ClassName.get(LAZY_ANNOTATION_PACKAGE_NAME, LAZY_ANNOTATION_CLASS_NAME))
        .build();
  }

  private static String simpleName(final TypeName typeName) {
    final TypeName rawType = rawType(typeName);
    if (rawType instanceof ArrayTypeName) {
      return arraySimpleName((ArrayTypeName) rawType);
    } else if (rawType instanceof ClassName) {
      return ((ClassName) rawType).simpleName();
    } else return String.valueOf(typeName);
  }

  private static String arraySimpleName(ArrayTypeName arrayTypeName) {
    return "ArrayOf"
        + (arrayTypeName.componentType instanceof ArrayTypeName
            ? arraySimpleName((ArrayTypeName) arrayTypeName.componentType)
            : arrayTypeName.componentType);
  }

  private static TypeName rawType(final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) typeName).rawType;
    }
    return typeName;
  }

  private static Iterable<MethodSpec> buildMappingMethods(
      final ConversionServiceAdapterDescriptor descriptor,
      final FieldSpec injectedConversionServiceFieldSpec) {
    return descriptor.getFromToMappings().stream()
        .map(
            sourceTargetPair ->
                toMappingMethodSpec(injectedConversionServiceFieldSpec, sourceTargetPair))
        .collect(toList());
  }

  private static MethodSpec toMappingMethodSpec(
      final FieldSpec injectedConversionServiceFieldSpec,
      final Pair<TypeName, TypeName> sourceTargetPair) {
    final ParameterSpec sourceParameterSpec = buildSourceParameterSpec(sourceTargetPair.getLeft());
    return MethodSpec.methodBuilder(
            String.format(
                "map%sTo%s",
                simpleName(sourceTargetPair.getLeft()), simpleName(sourceTargetPair.getRight())))
        .addParameter(sourceParameterSpec)
        .addModifiers(PUBLIC)
        .returns(sourceTargetPair.getRight())
        .addStatement(
            "return $N.convert($N, $T.class)",
            injectedConversionServiceFieldSpec,
            sourceParameterSpec,
            rawType(sourceTargetPair.getRight()))
        .build();
  }

  private static ParameterSpec buildSourceParameterSpec(final TypeName sourceClassName) {
    return ParameterSpec.builder(sourceClassName, "source", FINAL).build();
  }

  private static FieldSpec buildConversionServiceFieldSpec() {
    return FieldSpec.builder(
            ClassName.get(CONVERSION_SERVICE_PACKAGE_NAME, CONVERSION_SERVICE_CLASS_NAME),
            CONVERSION_SERVICE_FIELD_NAME,
            PRIVATE,
            FINAL)
        .build();
  }

  private AnnotationSpec buildGeneratedAnnotationSpec() {
    return AnnotationSpec.builder(ClassName.get("javax.annotation.processing", "Generated"))
        .addMember("value", "$S", ConversionServiceAdapterGenerator.class.getName())
        .addMember("date", "$S", ISO_INSTANT.format(ZonedDateTime.now(clock)))
        .build();
  }
}
