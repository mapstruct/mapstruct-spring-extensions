package org.mapstruct.extensions.spring.converter;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.*;

import com.squareup.javapoet.*;
import java.io.Writer;
import java.time.Clock;
import java.util.Optional;

public class DelegatingConverterGenerator extends Generator {
  public DelegatingConverterGenerator(final Clock clock) {
    super(clock);
  }

  public void writeGeneratedCodeToOutput(
      final DelegatingConverterDescriptor descriptor, final Writer outputWriter) {
    writeGeneratedCodeToOutput(
        () -> descriptor.getOriginalMapperClassName().packageName(),
        () -> createDelegatingConverterTypeSpec(descriptor),
        outputWriter);
  }

  private TypeSpec createDelegatingConverterTypeSpec(
      final DelegatingConverterDescriptor descriptor) {
    final var converterTypeSpecBuilder =
        TypeSpec.classBuilder(descriptor.getConverterClassName())
            .addModifiers(PUBLIC)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    CONVERTER_CLASSNAME,
                    descriptor.getFromToMapping().getSource(),
                    descriptor.getFromToMapping().getTarget()));
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(converterTypeSpecBuilder::addAnnotation);
    final var injectedMapperField = buildMapperField(descriptor);
    return converterTypeSpecBuilder
        .addAnnotation(ClassName.get("org.springframework.stereotype", "Component"))
        .addField(injectedMapperField)
        .addMethod(buildConstructor(injectedMapperField))
        .addMethod(buildConvertMethod(descriptor, injectedMapperField))
        .build();
  }

  private static MethodSpec buildConstructor(final FieldSpec injectedMapperField) {
    final var constructorParameter = buildConstructorParameter(injectedMapperField);
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(constructorParameter)
        .addStatement("this.$N = $N", injectedMapperField, constructorParameter)
        .build();
  }

  private static ParameterSpec buildConstructorParameter(final FieldSpec injectedMapperField) {
    return ParameterSpec.builder(injectedMapperField.type, injectedMapperField.name, FINAL)
        .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation", "Autowired"))
        .build();
  }

  private static MethodSpec buildConvertMethod(
      final DelegatingConverterDescriptor descriptor, final FieldSpec injectedMapperField) {
    final var sourceParameterSpec = buildConvertSourceParameterSpec(descriptor);
    return methodBuilder("convert")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .addParameter(sourceParameterSpec)
        .returns(descriptor.getFromToMapping().getTarget())
        .addStatement(
            "return $N.$L($N)",
            injectedMapperField,
            descriptor.getOriginalMapperMethodName(),
            sourceParameterSpec)
        .build();
  }

  private static ParameterSpec buildConvertSourceParameterSpec(
      final DelegatingConverterDescriptor descriptor) {
    return ParameterSpec.builder(descriptor.getFromToMapping().getSource(), "source", FINAL)
        .build();
  }

  private static FieldSpec buildMapperField(final DelegatingConverterDescriptor descriptor) {
    return FieldSpec.builder(descriptor.getOriginalMapperClassName(), "delegateMapper", PRIVATE)
        .build();
  }
}
