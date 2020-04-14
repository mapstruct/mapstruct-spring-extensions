package uk.co.kleindelao.mapstruct.spring.converter;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.*;

import com.squareup.javapoet.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.ConversionService;

public class ConversionServiceAdapterGenerator {
  private final Clock clock;

  public ConversionServiceAdapterGenerator(final Clock clock) {
    this.clock = clock;
  }

  public void writeConversionServiceBridge(
      ConversionServiceBridgeDescriptor descriptor, Writer out) {
    try {
      JavaFile.builder(
              descriptor.getBridgeClassName().packageName(),
              createConversionServiceTypeSpec(descriptor))
          .build()
          .writeTo(out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private TypeSpec createConversionServiceTypeSpec(
      final ConversionServiceBridgeDescriptor descriptor) {
    final FieldSpec injectedConversionServiceFieldSpec = buildInjectedConversionServiceFieldSpec();
    return TypeSpec.classBuilder(descriptor.getBridgeClassName())
        .addModifiers(PUBLIC)
        .addAnnotation(buildGeneratedAnnotationSpec())
        .addAnnotation(ClassName.get("org.springframework.stereotype", "Component"))
        .addField(injectedConversionServiceFieldSpec)
        .addMethods(buildMappingMethods(descriptor, injectedConversionServiceFieldSpec))
        .build();
  }

  private static Iterable<MethodSpec> buildMappingMethods(
      final ConversionServiceBridgeDescriptor descriptor,
      final FieldSpec injectedConversionServiceFieldSpec) {
    return descriptor.getFromToMappings().stream()
        .map(
            sourceTargetPair -> {
              final ParameterSpec sourceParameterSpec =
                  buildSourceParameterSpec(sourceTargetPair.getLeft());
              return MethodSpec.methodBuilder(
                      "map"
                          + sourceTargetPair.getLeft().simpleName()
                          + "To"
                          + sourceTargetPair.getRight().simpleName())
                  .addParameter(sourceParameterSpec)
                  .addModifiers(PUBLIC)
                  .returns(sourceTargetPair.getRight())
                  .addStatement(
                      "return $N.convert($N, $T.class)",
                      injectedConversionServiceFieldSpec,
                      sourceParameterSpec,
                      sourceTargetPair.getRight())
                  .build();
            })
        .collect(toList());
  }

  private static ParameterSpec buildSourceParameterSpec(final TypeName sourceClassName) {
    return ParameterSpec.builder(sourceClassName, "source", FINAL).build();
  }

  private static FieldSpec buildInjectedConversionServiceFieldSpec() {
    return FieldSpec.builder(ConversionService.class, "conversionService", PRIVATE)
        .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation", "Autowired"))
        .build();
  }

  private AnnotationSpec buildGeneratedAnnotationSpec() {
    return AnnotationSpec.builder(ClassName.get("javax.annotation", "Generated"))
        .addMember("value", "$S", ConversionServiceAdapterGenerator.class.getName())
        .addMember("date", "$S", DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now(clock)))
        .build();
  }
}
