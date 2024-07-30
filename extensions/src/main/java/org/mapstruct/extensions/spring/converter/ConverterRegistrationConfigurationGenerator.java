package org.mapstruct.extensions.spring.converter;

import static javax.lang.model.element.Modifier.*;

import com.squareup.javapoet.*;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

public class ConverterRegistrationConfigurationGenerator extends AdapterRelatedGenerator {

  private static final ClassName CONFIGURABLE_CONVERSION_SERVICE_CLASS_NAME =
      ClassName.get("org.springframework.core.convert.support", "ConfigurableConversionService");
  private static final ParameterizedTypeName CONVERTERS_LIST_TYPE =
      ParameterizedTypeName.get(
          ClassName.get(List.class),
          ParameterizedTypeName.get(
              CONVERTER_CLASSNAME,
              WildcardTypeName.subtypeOf(Object.class),
              WildcardTypeName.subtypeOf(Object.class)));
  private static final String CONVERTERS_FIELD_NAME = "converters";

  public ConverterRegistrationConfigurationGenerator(final Clock clock) {
    super(clock);
  }

  @Override
  protected TypeSpec createMainTypeSpec(final ConversionServiceAdapterDescriptor descriptor) {
    final var converterRegistrationConfigurationTypeSpecBuilder =
        TypeSpec.classBuilder(descriptor.getConverterRegistrationConfigurationClassName());
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

  private ClassName postConstructClassName() {
    if (isTypeAvailable("jakarta.annotation.PostConstruct")) {
      return ClassName.get("jakarta.annotation", "PostConstruct");
    } else {
      return ClassName.get("javax.annotation", "PostConstruct");
    }
  }

  private MethodSpec createRegistrationMethodSpec(
      final FieldSpec convertersFieldSpec, final FieldSpec conversionServiceFieldSpec) {

    return MethodSpec.methodBuilder("registerConverters")
        .addAnnotation(postConstructClassName())
        .addStatement(
            "$N.forEach($N::addConverter)", convertersFieldSpec, conversionServiceFieldSpec)
        .build();
  }

  private static MethodSpec createConstructorSpec(
      final ParameterSpec conversionServiceParameterSpec,
      final ParameterSpec convertersParameterSpec,
      final FieldSpec conversionServiceFieldSpec,
      final FieldSpec convertersFieldSpec) {
    return MethodSpec.constructorBuilder()
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
