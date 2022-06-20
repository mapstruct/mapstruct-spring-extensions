package org.mapstruct.extensions.spring.converter;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.ExternalConversion;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

import javax.tools.JavaFileObject;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.time.Clock;
import java.util.Locale;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;
import static com.google.testing.compile.JavaSourcesSubject.assertThat;
import static javax.lang.model.element.Modifier.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ConverterMapperProcessorTest {
  public static final ClassName CAR_CLASS_NAME = ClassName.get("test", "Car");
  public static final ClassName CAR_DTO_CLASS_NAME = ClassName.get("test", "CarDto");

  @Spy
  private final ConversionServiceAdapterGenerator adapterGenerator =
      new ConversionServiceAdapterGenerator(Clock.systemUTC());

  @InjectMocks private ConverterMapperProcessor processor;

  @Captor private ArgumentCaptor<ConversionServiceAdapterDescriptor> descriptorArgumentCaptor;

  private static Set<JavaFileObject> commonCompilationUnits;
  private static final String PACKAGE_NAME = "test";

  @BeforeAll
  static void setupCommonSourceFiles() {
    commonCompilationUnits =
        ImmutableSet.of(
            JavaFile.builder(PACKAGE_NAME, buildSimpleModelClassTypeSpec("Car"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder(PACKAGE_NAME, buildSimpleModelClassTypeSpec("CarDto"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder("javax.annotation", buildGeneratedAnnotationTypeSpec())
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder(
                    "org.springframework.stereotype", buildSimpleAnnotationTypeSpec("Component"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder(
                    "org.springframework.context.annotation", buildSimpleAnnotationTypeSpec("Lazy"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject());
  }

  private static TypeSpec buildSimpleAnnotationTypeSpec(final String annotationName) {
    return TypeSpec.annotationBuilder(annotationName).addModifiers(PUBLIC).build();
  }

  private static TypeSpec buildGeneratedAnnotationTypeSpec() {
    return TypeSpec.annotationBuilder("Generated")
        .addModifiers(PUBLIC)
        .addMethod(
            MethodSpec.methodBuilder("value")
                .returns(String.class)
                .addModifiers(PUBLIC, ABSTRACT)
                .build())
        .addMethod(
            MethodSpec.methodBuilder("date")
                .returns(String.class)
                .addModifiers(PUBLIC, ABSTRACT)
                .build())
        .build();
  }

  private static TypeSpec buildSimpleModelClassTypeSpec(final String className) {
    final FieldSpec makeField = FieldSpec.builder(String.class, "make", PRIVATE).build();
    final ParameterSpec makeParameter = ParameterSpec.builder(String.class, "make", FINAL).build();
    return TypeSpec.classBuilder(className)
        .addModifiers(PUBLIC)
        .addField(makeField)
        .addMethod(
            MethodSpec.methodBuilder("getMake")
                .returns(String.class)
                .addStatement("return $N", makeField)
                .build())
        .addMethod(
            MethodSpec.methodBuilder("setMake")
                .addParameter(makeParameter)
                .addStatement("this.$N = $N", makeField, makeParameter)
                .build())
        .build();
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
    assertThat(mapperFile.toJavaFileObject(), commonCompilationUnits.toArray(new JavaFileObject[0]))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(Pair.class))
        .containsExactly(Pair.of(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME));
  }

  @Test
  void shouldCompileConverterMapperWithGenericSourceType() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericSourceTypeBuilder().build())
            .build();

    // When - Then
    assertThat(mapperFile.toJavaFileObject(), commonCompilationUnits.toArray(new JavaFileObject[0]))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(Pair.class))
        .containsExactly(Pair.of(genericSourceTypeName(), CAR_DTO_CLASS_NAME));
  }

  @Test
  void shouldCompileConverterMapperWithGenericTargetType() {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericTargetTypeBuilder().build())
            .build();

    // When - Then
    assertThat(mapperFile.toJavaFileObject(), commonCompilationUnits.toArray(new JavaFileObject[0]))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    then(descriptorArgumentCaptor.getValue())
        .isNotNull()
        .extracting(ConversionServiceAdapterDescriptor::getFromToMappings)
        .asInstanceOf(list(Pair.class))
        .containsExactly(Pair.of(CAR_CLASS_NAME, genericTargetTypeName()));
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
            plainMapperFile.toJavaFileObject(),
            commonCompilationUnits.toArray(new JavaFileObject[0]))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
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
    assertThat(mapperFile.toJavaFileObject(), commonCompilationUnits.toArray(new JavaFileObject[0]))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings())
        .hasSize(1)
        .containsExactly(Pair.of(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME));
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
            mapperFile.toJavaFileObject(),
            toArray(
                concat(Set.of(mappingConfigFile.toJavaFileObject()), commonCompilationUnits),
                JavaFileObject.class))
        .processedWith(processor)
        .compilesWithoutError();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            Pair.of(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME),
            Pair.of(ClassName.get(String.class), ClassName.get(Locale.class)));
  }
}
