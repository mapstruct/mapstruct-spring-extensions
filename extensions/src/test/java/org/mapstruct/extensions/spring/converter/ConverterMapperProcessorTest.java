package org.mapstruct.extensions.spring.converter;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

import javax.tools.*;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.time.Clock;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static org.assertj.core.api.BDDAssertions.then;
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
                    "org.springframework.beans.factory.annotation",
                    buildSimpleAnnotationTypeSpec("Autowired"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder(
                    "org.springframework.beans.factory.annotation",
                    buildAnnotationWithValueTypeSpec("Qualifier"))
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
                 .toJavaFileObject(),
            JavaFile.builder(PACKAGE_NAME, buildConfigClass("MyMappingConfig"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject());
  }

  private static TypeSpec buildSimpleAnnotationTypeSpec(final String anotationName) {
    return TypeSpec.annotationBuilder(anotationName).addModifiers(PUBLIC).build();
  }

  private static TypeSpec buildAnnotationWithValueTypeSpec(final String anotationName) {
    return TypeSpec.annotationBuilder(anotationName)
        .addMethod(
            MethodSpec.methodBuilder("value")
                .returns(String.class)
                .addModifiers(PUBLIC, ABSTRACT)
                .build())
        .addModifiers(PUBLIC)
        .build();
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

  private static TypeSpec buildConfigClass(final String className) {
    return TypeSpec.interfaceBuilder(className)
        .addModifiers(PUBLIC)
        .addAnnotation(
            AnnotationSpec.builder(
                    ClassName.get("org.mapstruct.extensions.spring", "SpringMapperConfig"))
                .addMember("conversionServiceBeanName", "$S", "myConversionService")
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
    // e.g. Mapper converting from java.lang.ref.WeakReference<Car> to CarDto

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
    // e.g. Mapper converting from Car to java.lang.ref.WeakReference<CarDto>

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
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
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
        .writeConversionServiceAdapter(descriptorArgumentCaptor.capture(), any(Writer.class));
    final ConversionServiceAdapterDescriptor descriptor = descriptorArgumentCaptor.getValue();
    then(descriptor).isNotNull();
    then(descriptor.getFromToMappings())
        .hasSize(1)
        .containsExactly(Pair.of(CAR_CLASS_NAME, CAR_DTO_CLASS_NAME));
  }
}
