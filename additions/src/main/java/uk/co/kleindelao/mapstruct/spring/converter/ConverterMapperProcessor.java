package uk.co.kleindelao.mapstruct.spring.converter;

import com.squareup.javapoet.ClassName;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.converter.Converter;
import uk.co.kleindelao.mapstruct.spring.converter.ConversionServiceBridgeDescriptor.ConversionServiceBridgeDescriptorBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;

import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.tools.Diagnostic.Kind.ERROR;

@SupportedAnnotationTypes(ConverterMapperProcessor.ORG_MAPSTRUCT_MAPPER)
public class ConverterMapperProcessor extends AbstractProcessor {
  protected static final String ORG_MAPSTRUCT_MAPPER = "org.mapstruct.Mapper";

  private final ConversionServiceBridgeGenerator bridgeGenerator =
      new ConversionServiceBridgeGenerator(Clock.systemUTC());

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final Types typeUtils = processingEnv.getTypeUtils();
    final ConversionServiceBridgeDescriptorBuilder descriptorBuilder =
        ConversionServiceBridgeDescriptor.builder()
            .bridgeClassName(
                ClassName.get(
                    ConverterMapperProcessor.class.getPackage().getName(),
                    "ConversionServiceBridge"));
    for (final TypeElement annotation : annotations) {
      if (ORG_MAPSTRUCT_MAPPER.contentEquals(annotation.getQualifiedName())) {
        roundEnv.getElementsAnnotatedWith(annotation).stream()
            .filter(mapper -> mapper.asType().getKind() == DECLARED)
            .filter(mapper -> getConverterSupertype(mapper).isPresent())
            .forEach(
                mapper ->
                    mapper.getEnclosedElements().stream()
                        .filter(element -> element.getKind() == METHOD)
                        .filter(method -> method.getModifiers().contains(PUBLIC))
                        .filter(method -> method.getSimpleName().contentEquals("convert"))
                        .filter(
                            convert -> ((ExecutableElement) convert).getParameters().size() == 1)
                        .filter(
                            convert ->
                                typeUtils.isSameType(
                                    getFirstParameterType((ExecutableElement) convert),
                                    getFirstTypeArgument(getConverterSupertype(mapper).get())))
                        .forEach(
                            convert ->
                                descriptorBuilder.fromToMapping(
                                    Pair.of(
                                        (ClassName)
                                            ((ExecutableElement) convert)
                                                .getParameters().stream()
                                                    .map(Element::asType)
                                                    .map(ClassName::get)
                                                    .findFirst()
                                                    .get(),
                                        (ClassName)
                                            ClassName.get(
                                                ((ExecutableElement) convert).getReturnType())))));
        try (final Writer outputWriter =
                     processingEnv
                             .getFiler()
                             .createSourceFile(
                                     ConverterMapperProcessor.class.getPackage().getName() + ".ConversionServiceBridge")
                             .openWriter()) {
          bridgeGenerator.writeConversionServiceBridge(descriptorBuilder.build(), outputWriter);
        } catch (IOException e) {
          processingEnv
                  .getMessager()
                  .printMessage(
                          ERROR, "Error while opening ConversionServiceBridge output file: " + e.getMessage());
        }
      }
    }
    return false;
  }

  private Optional<? extends TypeMirror> getConverterSupertype(final Element mapper) {
    final Types typeUtils = processingEnv.getTypeUtils();
    return typeUtils.directSupertypes(mapper.asType()).stream()
        .filter(
            supertype -> typeUtils.erasure(supertype).toString().equals(Converter.class.getName()))
        .findFirst();
  }

  private static TypeMirror getFirstParameterType(final ExecutableElement convert) {
    return convert.getParameters().stream().findFirst().map(Element::asType).orElse(null);
  }

  private static TypeMirror getFirstTypeArgument(final TypeMirror converterSupertype) {
    return ((DeclaredType) converterSupertype).getTypeArguments().stream().findFirst().orElse(null);
  }
}
