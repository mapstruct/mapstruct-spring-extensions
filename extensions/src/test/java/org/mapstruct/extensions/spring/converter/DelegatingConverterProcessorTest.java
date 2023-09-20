package org.mapstruct.extensions.spring.converter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.assertj.core.api.BDDAssertions.then;

import com.squareup.javapoet.*;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Clock;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.Mapper;
import org.mapstruct.extensions.spring.DelegatingConverter;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

@ExtendWith(MockitoExtension.class)
class DelegatingConverterProcessorTest extends AbstractProcessorTest {
  @Spy
  private final DelegatingConverterGenerator generator =
      new DelegatingConverterGenerator(Clock.systemUTC());

  @InjectMocks private DelegatingConverterProcessor underTest;

  private boolean compile(final JavaFileObject... additionalCompilationUnits) throws IOException {
    return compile(underTest, additionalCompilationUnits);
  }

  @Test
  void shouldCompileMapperWithDelegatingConverterAnnotation() throws IOException {
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

    final var compileResult =
        compile(
            delegatingConverterFile.toJavaFileObject(),
            mapperFile.toJavaFileObject(),
            autowiredFile.toJavaFileObject());

    then(compileResult).isTrue();
  }
}
