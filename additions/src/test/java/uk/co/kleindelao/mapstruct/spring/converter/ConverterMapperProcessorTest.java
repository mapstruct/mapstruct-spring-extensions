package uk.co.kleindelao.mapstruct.spring.converter;

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
import java.time.Clock;
import java.util.Set;
import javax.tools.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

@ExtendWith(MockitoExtension.class)
class ConverterMapperProcessorTest {
  @Spy
  private ConversionServiceAdapterGenerator adapterGenerator =
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
            JavaFile.builder(PACKAGE_NAME, buildModelClassTypeSpec("Car"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject(),
            JavaFile.builder(PACKAGE_NAME, buildModelClassTypeSpec("CarDto"))
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
                    "org.springframework.stereotype", buildSimpleAnnotationTypeSpec("Component"))
                .skipJavaLangImports(true)
                .build()
                .toJavaFileObject());
  }

  private static TypeSpec buildSimpleAnnotationTypeSpec(final String anotationName) {
    return TypeSpec.annotationBuilder(anotationName).addModifiers(PUBLIC).build();
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

  private static TypeSpec buildModelClassTypeSpec(final String className) {
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

  @Test
  void shouldCompileSimpleConverterMapper() throws IOException {
    // Given
    final JavaFile mapperFile =
        JavaFile.builder(PACKAGE_NAME, converterMapperBuilder().build()).build();

    // When
    final boolean compileResult = compile(mapperFile.toJavaFileObject());

    // Then
    then(compileResult).isTrue();
  }

  private static TypeSpec.Builder converterMapperBuilder() {
    return plainCarMapperBuilder()
        .addSuperinterface(
            ParameterizedTypeName.get(
                ClassName.get(Converter.class),
                ClassName.get("test", "Car"),
                ClassName.get("test", "CarDto")));
  }

  private static TypeSpec.Builder plainCarMapperBuilder() {
    return TypeSpec.interfaceBuilder("CarMapper")
        .addAnnotation(Mapper.class)
        .addMethod(convertMethod("Car", "CarDto"));
  }

  private static MethodSpec convertMethod(final String parameterType, final String returnType) {
    return MethodSpec.methodBuilder("convert")
        .addModifiers(PUBLIC, ABSTRACT)
        .addParameter(ClassName.get("test", parameterType), "car")
        .returns(ClassName.get("test", returnType))
        .build();
  }

  @Test
  void shouldIgnoreNonConverterMappers() throws IOException {
    // Given
    final JavaFile plainMapperFile =
        JavaFile.builder(PACKAGE_NAME, plainCarMapperBuilder().build()).build();

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
                converterMapperBuilder().addMethod(convertMethod("CarDto", "Car")).build())
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
        .containsExactly(Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto")));
  }
}
