package org.mapstruct.extensions.spring.converter;

import static com.google.common.collect.Iterables.concat;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static javax.lang.model.element.Modifier.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;

import com.google.testing.compile.Compilation;
import com.squareup.javapoet.*;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import javax.tools.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.DelegatingConverter;
import org.mapstruct.extensions.spring.ExternalConversion;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

@ExtendWith(MockitoExtension.class)
class ConverterMapperProcessorTest extends AbstractProcessorTest {
  @Spy
  private final ConversionServiceAdapterGenerator adapterGenerator =
      new ConversionServiceAdapterGenerator(Clock.systemUTC());

  @Spy
  private final ConverterScanGenerator converterScanGenerator =
      new ConverterScanGenerator(Clock.systemUTC());

  @Spy
  private final ConverterScansGenerator converterScansGenerator =
      new ConverterScansGenerator(Clock.systemUTC());

  @Spy
  private final DelegatingConverterGenerator delegatingConverterGenerator =
      new DelegatingConverterGenerator(Clock.systemUTC());

  @Spy
  private final ConverterRegistrationConfigurationGenerator
      converterRegistrationConfigurationGenerator =
          new ConverterRegistrationConfigurationGenerator(Clock.systemUTC());

  @InjectMocks private ConverterMapperProcessor processor;

  @Captor private ArgumentCaptor<ConversionServiceAdapterDescriptor> descriptorArgumentCaptor;

  private Compilation compile(final JavaFileObject... additionalCompilationUnits) {
    return compile(processor, additionalCompilationUnits);
  }

  private static TypeSpec buildConfigClassWithExternalConversion(final String className) {
    return TypeSpec.interfaceBuilder(className)
        .addModifiers(PUBLIC)
        .addAnnotation(
            AnnotationSpec.builder(
                    ClassName.get("org.mapstruct.extensions.spring", "SpringMapperConfig"))
                .addMember(
                    "externalConversions",
                    "@$T(sourceType = $T.class, targetType = $T.class)",
                    ExternalConversion.class,
                    String.class,
                    Locale.class)
                .build())
        .build();
  }

  @Test
  void shouldCompileSimpleConverterMapper() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME, converterMapperWithoutGenericSourceOrTargetTypeBuilder().build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(concat(List.of(mapperFile.toJavaFileObject()), COMMON_COMPILATION_UNITS)))
        .succeeded();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(FromToMapping.class))
        .containsExactly(new FromToMapping().source(CAR_CLASS_NAME).target(CAR_DTO_CLASS_NAME));
  }

  @Test
  void shouldCompileConverterMapperWithGenericSourceType() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericSourceTypeBuilder().build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(concat(List.of(mapperFile.toJavaFileObject()), COMMON_COMPILATION_UNITS)))
        .succeeded();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(FromToMapping.class))
        .containsExactly(
            new FromToMapping().source(genericSourceTypeName()).target(CAR_DTO_CLASS_NAME));
  }

  @Test
  void shouldCompileConverterMapperWithGenericTargetType() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericTargetTypeBuilder().build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(concat(List.of(mapperFile.toJavaFileObject()), COMMON_COMPILATION_UNITS)))
        .succeeded();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(FromToMapping.class))
        .containsExactly(
            new FromToMapping().source(CAR_CLASS_NAME).target(genericTargetTypeName()));
  }

  private static TypeSpec.Builder converterMapperWithoutGenericSourceOrTargetTypeBuilder() {
    return converterMapperBuilder(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME);
  }

  private static TypeSpec.Builder converterMapperBuilder(
      final TypeName sourceTypeName, final TypeName targetTypeName) {
    return plainCarMapperBuilder(sourceTypeName, targetTypeName)
        .addSuperinterface(
            ParameterizedTypeName.get(
                ClassName.get(Converter.class), sourceTypeName, targetTypeName));
  }

  private static TypeSpec.Builder converterMapperWithGenericSourceTypeBuilder() {
    return converterMapperBuilder(genericSourceTypeName(), CAR_DTO_CLASS_NAME);
  }

  private static ParameterizedTypeName genericSourceTypeName() {
    return genericTypeName(CAR_CLASS_NAME);
  }

  private static ParameterizedTypeName genericTypeName(final ClassName wrappedClassName) {
    return ParameterizedTypeName.get(ClassName.get(WeakReference.class), wrappedClassName);
  }

  private static TypeSpec.Builder converterMapperWithGenericTargetTypeBuilder() {
    return converterMapperBuilder(CAR_CLASS_NAME, genericTargetTypeName());
  }

  private static ParameterizedTypeName genericTargetTypeName() {
    return genericTypeName(CAR_DTO_CLASS_NAME);
  }

  private static TypeSpec.Builder plainCarMapperBuilder(
      final TypeName sourceTypeName, final TypeName targetTypeName) {
    return TypeSpec.interfaceBuilder("CarMapper")
        .addAnnotation(Mapper.class)
        .addMethod(convertMethod(sourceTypeName, targetTypeName));
  }

  private static MethodSpec convertMethod(
      final TypeName sourceTypeName, final TypeName targetTypeName) {
    return MethodSpec.methodBuilder("convert")
        .addModifiers(PUBLIC, ABSTRACT)
        .addParameter(sourceTypeName, "car")
        .returns(targetTypeName)
        .build();
  }

  @Test
  void shouldIgnoreNonConverterMappers() {
    // Given
    final JavaFile plainMapperFile =
        JavaFile.builder(
                PACKAGE_NAME, plainCarMapperBuilder(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME).build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(
                    concat(List.of(plainMapperFile.toJavaFileObject()), COMMON_COMPILATION_UNITS)))
        .succeeded();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings()).isEmpty();
  }

  @Test
  void shouldProcessOnlyConvertMethodForMapperWithMultipleMethods() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME,
                converterMapperWithoutGenericSourceOrTargetTypeBuilder()
                    .addMethod(convertMethod(CAR_DTO_CLASS_NAME, CAR_CLASS_NAME))
                    .build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(concat(List.of(mapperFile.toJavaFileObject()), COMMON_COMPILATION_UNITS)))
        .succeeded();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings())
        .hasSize(1)
        .containsExactly(new FromToMapping().source(CAR_CLASS_NAME).target(CAR_DTO_CLASS_NAME));
  }

  @Test
  void shouldAddConversionServiceCallsForExternalConversions() {
    // Given
    final JavaFile mappingConfigFile =
        JavaFile.builder(
                PACKAGE_NAME, buildConfigClassWithExternalConversion("StringToLocaleConfig"))
            .build();
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME, converterMapperWithoutGenericSourceOrTargetTypeBuilder().build())
            .build();

    // When - Then
    assertThat(
            javac()
                .withProcessors(processor)
                .compile(
                    concat(
                        List.of(
                            mapperFile.toJavaFileObject(), mappingConfigFile.toJavaFileObject()),
                        COMMON_COMPILATION_UNITS)))
        .succeeded();

    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FromToMapping().source(CAR_CLASS_NAME).target(CAR_DTO_CLASS_NAME),
            new FromToMapping()
                .source(ClassName.get(String.class))
                .target(ClassName.get(Locale.class)));
  }

  @Test
  void shouldCompileMapperWithDelegatingConverterAnnotation() {
    final var delegatingConverterAnnotationTypeSpec =
        TypeSpec.annotationBuilder(ClassName.get(DelegatingConverter.class))
            .addAnnotation(
                AnnotationSpec.builder(Target.class).addMember("value", "$L", METHOD).build())
            .addAnnotation(
                AnnotationSpec.builder(Retention.class).addMember("value", "$L", SOURCE).build())
            .addModifiers(PUBLIC)
            .build();
    final var delegatingConverterFile =
        JavaFile.builder(
                DelegatingConverter.class.getPackageName(), delegatingConverterAnnotationTypeSpec)
            .addStaticImport(METHOD)
            .addStaticImport(SOURCE)
            .build();
    final var mapperTypeSpec =
        TypeSpec.classBuilder("CarMapper")
            .addAnnotation(Mapper.class)
            .addModifiers(PUBLIC, ABSTRACT)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(Converter.class), CAR_CLASS_NAME, CAR_DTO_CLASS_NAME))
            .addMethod(
                MethodSpec.methodBuilder("convert")
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(CAR_CLASS_NAME, "car")
                    .returns(CAR_DTO_CLASS_NAME)
                    .build())
            .addMethod(
                MethodSpec.methodBuilder("invert")
                    .addAnnotation(DelegatingConverter.class)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(CAR_DTO_CLASS_NAME, "carDto")
                    .returns(CAR_CLASS_NAME)
                    .build())
            .build();
    final var mapperFile = JavaFile.builder(PACKAGE_NAME, mapperTypeSpec).build();
    final var autowiredTypeSpec =
        TypeSpec.annotationBuilder(
                ClassName.get("org.springframework.beans.factory.annotation", "Autowired"))
            .addModifiers(PUBLIC)
            .build();
    final var autowiredFile =
        JavaFile.builder("org.springframework.beans.factory.annotation", autowiredTypeSpec).build();

    assertThat(
            compile(
                delegatingConverterFile.toJavaFileObject(),
                mapperFile.toJavaFileObject(),
                autowiredFile.toJavaFileObject()))
        .succeeded();
  }
}
