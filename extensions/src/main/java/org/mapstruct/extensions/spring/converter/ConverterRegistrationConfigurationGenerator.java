package org.mapstruct.extensions.spring.converter;

import static javax.lang.model.element.Modifier.*;

import com.squareup.javapoet.*;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

public class ConverterRegistrationConfigurationGenerator extends Generator {

  private static final ClassName CONFIGURABLE_CONVERSION_SERVICE_CLASS_NAME =
      ClassName.get("org.springframework.core.convert.support", "ConfigurableConversionService");
  private static final ParameterizedTypeName CONVERTERS_LIST_TYPE =
      ParameterizedTypeName.get(
          ClassName.get(List.class),
          ParameterizedTypeName.get(
              ClassName.get("org.springframework.core.convert.converter", "Converter"),
              WildcardTypeName.subtypeOf(Object.class),
              WildcardTypeName.subtypeOf(Object.class)));
  private static final String CONVERTERS_FIELD_NAME = "converters";

  public ConverterRegistrationConfigurationGenerator(final Clock clock) {
    super(clock);
  }

  @Override
  protected TypeSpec createMainTypeSpec(ConversionServiceAdapterDescriptor descriptor) {
    final var converterRegistrationConfigurationTypeSpecBuilder =
        TypeSpec.classBuilder(descriptor.getConverterRegistrationConfigurationClassName())
            .addModifiers(PUBLIC);
    final var conversionServiceFieldSpec =
        FieldSpec.builder(
                CONFIGURABLE_CONVERSION_SERVICE_CLASS_NAME,
                descriptor.getConversionServiceBeanName(),
                PRIVATE,
                FINAL)
            .build();
    final var convertersFieldSpec =
        FieldSpec.builder(CONVERTERS_LIST_TYPE, CONVERTERS_FIELD_NAME, PRIVATE, FINAL).build();
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(converterRegistrationConfigurationTypeSpecBuilder::addAnnotation);
    final var conversionServiceParameterSpec = createConversionServiceParameter(descriptor);
    final var convertersParameterSpec = createConvertersParameterSpec();
    return converterRegistrationConfigurationTypeSpecBuilder
        .addAnnotation(ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "Configuration"))
        .addField(conversionServiceFieldSpec)
        .addField(convertersFieldSpec)
        .addMethod(
            createConstructorSpec(
                conversionServiceParameterSpec,
                convertersParameterSpec,
                conversionServiceFieldSpec,
                convertersFieldSpec))
        .addMethod(createRegistrationMethodSpec(convertersFieldSpec, conversionServiceFieldSpec))
        .build();
  }

  private static MethodSpec createRegistrationMethodSpec(
      FieldSpec convertersFieldSpec, FieldSpec conversionServiceFieldSpec) {
    return MethodSpec.methodBuilder("registerConverters")
        .addAnnotation(ClassName.get("javax.annotation", "PostConstruct"))
        .addStatement(
            "$N.forEach($N::addConverter)", convertersFieldSpec, conversionServiceFieldSpec)
        .build();
  }

  private static MethodSpec createConstructorSpec(
      ParameterSpec conversionServiceParameterSpec,
      ParameterSpec convertersParameterSpec,
      FieldSpec conversionServiceFieldSpec,
      FieldSpec convertersFieldSpec) {
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(conversionServiceParameterSpec)
        .addParameter(convertersParameterSpec)
        .addStatement("this.$N = $N", conversionServiceFieldSpec, conversionServiceParameterSpec)
        .addStatement("this.$N = $N", convertersFieldSpec, convertersParameterSpec)
        .build();
  }

  private ParameterSpec createConvertersParameterSpec() {
    return ParameterSpec.builder(CONVERTERS_LIST_TYPE, CONVERTERS_FIELD_NAME, FINAL).build();
  }

  private ParameterSpec createConversionServiceParameter(
      final ConversionServiceAdapterDescriptor descriptor) {
    return ParameterSpec.builder(
            CONFIGURABLE_CONVERSION_SERVICE_CLASS_NAME,
            descriptor.getConversionServiceBeanName(),
            FINAL)
        .addAnnotation(
            AnnotationSpec.builder(
                    ClassName.get("org.springframework.beans.factory.annotation", "Qualifier"))
                .addMember("value", "$S", descriptor.getConversionServiceBeanName())
                .build())
        .build();
  }
}
