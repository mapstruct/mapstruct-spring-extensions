package org.mapstruct.extensions.spring.converter;

import static com.google.common.collect.Iterables.concat;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import javax.tools.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.ExternalConversion;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

@ExtendWith(MockitoExtension.class)
class ConverterMapperProcessorTest {
  public static final ClassName CAR_CLASS_NAME = ClassName.get("test", "Car");
  public static final ClassName CAR_DTO_CLASS_NAME = ClassName.get("test", "CarDto");

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
  private final ConverterRegistrationConfigurationGenerator
      converterRegistrationConfigurationGenerator =
          new ConverterRegistrationConfigurationGenerator(Clock.systemUTC());

  @InjectMocks private ConverterMapperProcessor processor;

  @Captor private ArgumentCaptor<ConversionServiceAdapterDescriptor> descriptorArgumentCaptor;

  private static Set<JavaFileObject> commonCompilationUnits;
  private static final String PACKAGE_NAME = "test";

  private boolean compile(JavaFileObject... additionalCompilationUnits) throws IOException {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    final StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, null);
    fileManager.setLocation(CLASS_OUTPUT, singletonList(createTempDirectory("classes").toFile()));

    final JavaCompiler.CompilationTask task =
        compiler.getTask(
            null,
            fileManager,
            diagnostics,
            null,
            null,
            concat(commonCompilationUnits, asList(additionalCompilationUnits)));
    task.setProcessors(singletonList(processor));

    final boolean success = task.call();
    diagnostics.getDiagnostics().forEach(System.err::println);
    return success;
  }

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
  void shouldCompileSimpleConverterMapper() throws IOException {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME, converterMapperWithoutGenericSourceOrTargetTypeBuilder().build())
            .build();

    // When
    final boolean compileResult = compile(mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
  }

  @Test
  void shouldCompileConverterMapperWithGenericSourceType() throws IOException {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericSourceTypeBuilder().build())
            .build();

    // When
    final boolean compileResult = compile(mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
  }

  @Test
  void shouldCompileConverterMapperWithGenericTargetType() throws IOException {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperWithGenericTargetTypeBuilder().build())
            .build();

    // When
    final boolean compileResult = compile(mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
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
    return converterMapperBuilder(
        ParameterizedTypeName.get(ClassName.get(WeakReference.class), CAR_CLASS_NAME),
        CAR_DTO_CLASS_NAME);
  }

  private static TypeSpec.Builder converterMapperWithGenericTargetTypeBuilder() {
    return converterMapperBuilder(
        CAR_CLASS_NAME,
        ParameterizedTypeName.get(ClassName.get(WeakReference.class), CAR_DTO_CLASS_NAME));
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
  void shouldIgnoreNonConverterMappers() throws IOException {
    // Given
    final JavaFile plainMapperFile =
        JavaFile.builder(
                PACKAGE_NAME, plainCarMapperBuilder(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME).build())
            .build();

    // When
    final boolean compileResult = compile(plainMapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
    BDDMockito.then(adapterGenerator)
        .should()
        .writeGeneratedCodeToOutput(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings()).isEmpty();
  }

  @Test
  void shouldProcessOnlyConvertMethodForMapperWithMultipleMethods() throws IOException {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME,
                converterMapperWithoutGenericSourceOrTargetTypeBuilder()
                    .addMethod(convertMethod(CAR_DTO_CLASS_NAME, CAR_CLASS_NAME))
                    .build())
            .build();

    // When
    final boolean compileResult = compile(mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
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
  void shouldAddConversionServiceCallsForExternalConversions() throws IOException {
    // Given
    final JavaFile mappingConfigFile =
        JavaFile.builder(
                PACKAGE_NAME, buildConfigClassWithExternalConversion("StringToLocaleConfig"))
            .build();
    final JavaFile mapperFile =
        JavaFile.builder(
                PACKAGE_NAME, converterMapperWithoutGenericSourceOrTargetTypeBuilder().build())
            .build();

    // When
    final boolean compileResult =
        compile(mappingConfigFile.toJavaFileObject(), mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
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
}
