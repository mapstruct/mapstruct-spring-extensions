package org.mapstruct.extensions.spring.converter;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.*;

import com.squareup.javapoet.*;
import java.io.Writer;
import java.time.Clock;
import java.util.Optional;
import org.mapstruct.Mapper;

public class DelegatingConverterGenerator extends Generator {
  public DelegatingConverterGenerator(final Clock clock) {
    super(clock);
  }

  public void writeGeneratedCodeToOutput(
      final DelegatingConverterDescriptor descriptor, final Writer outputWriter) {
    writeGeneratedCodeToOutput(
        () -> descriptor.getOriginalMapperClassName().packageName(),
        () -> createDelegatingMapperTypeSpec(descriptor),
        outputWriter);
  }

  private TypeSpec createDelegatingMapperTypeSpec(final DelegatingConverterDescriptor descriptor) {
    final var mapperTypeSpecBuilder =
        TypeSpec.classBuilder(descriptor.getConverterClassName())
            .addModifiers(PUBLIC, ABSTRACT)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    CONVERTER_CLASSNAME,
                    descriptor.getFromToMapping().getSource(),
                    descriptor.getFromToMapping().getTarget()));
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(mapperTypeSpecBuilder::addAnnotation);
    final var injectedMapperField = buildInjectedMapperField(descriptor);
    return mapperTypeSpecBuilder
        .addAnnotation(buildMapperAnnotation(descriptor))
        .addField(injectedMapperField)
        .addMethod(buildConvertMethod(descriptor, injectedMapperField))
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

  private static FieldSpec buildInjectedMapperField(
      final DelegatingConverterDescriptor descriptor) {
    return FieldSpec.builder(descriptor.getOriginalMapperClassName(), "delegateMapper", PRIVATE)
        .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation", "Autowired"))
        .build();
  }

  private static AnnotationSpec buildMapperAnnotation(
      final DelegatingConverterDescriptor descriptor) {
    final var builder = AnnotationSpec.builder(Mapper.class);
    descriptor
        .getComponentModel()
        .ifPresent(componentModel -> builder.addMember("componentModel", "$S", componentModel));
    descriptor
        .getConfigTypeName()
        .ifPresent(configTypeName -> builder.addMember("config", "$T.class", configTypeName));
    return builder.build();
  }
}
