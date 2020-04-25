package org.mapstruct.extensions.spring.converter;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.tools.Diagnostic.Kind.ERROR;

import com.squareup.javapoet.ClassName;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@SupportedAnnotationTypes({
  ConverterMapperProcessor.MAPPER,
  ConverterMapperProcessor.SPRING_MAPPER_CONFIG
})
public class ConverterMapperProcessor extends AbstractProcessor {
  protected static final String MAPPER = "org.mapstruct.Mapper";
  protected static final String SPRING_MAPPER_CONFIG =
      "org.mapstruct.extensions.spring.SpringMapperConfig";
  protected static final String SPRING_CONVERTER_FULL_NAME = "org.springframework.core.convert.converter.Converter";

  private final ConversionServiceAdapterGenerator adapterGenerator;

  public ConverterMapperProcessor() {
    this(new ConversionServiceAdapterGenerator(Clock.systemUTC()));
  }

  ConverterMapperProcessor(final ConversionServiceAdapterGenerator adapterGenerator) {
    super();
    this.adapterGenerator = adapterGenerator;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final ConversionServiceAdapterDescriptor descriptor = new ConversionServiceAdapterDescriptor();
    final Pair<String, String> adapterPackageAndClass =
        getAdapterPackageAndClassName(annotations, roundEnv);
    descriptor.setAdapterClassName(
        ClassName.get(adapterPackageAndClass.getLeft(), adapterPackageAndClass.getRight()));
    annotations.stream()
        .filter(annotation -> MAPPER.contentEquals(annotation.getQualifiedName()))
        .forEach(
            annotation ->
                processMapperAnnotation(roundEnv, descriptor, adapterPackageAndClass, annotation));
    return false;
  }

  private void processMapperAnnotation(
      final RoundEnvironment roundEnv,
      final ConversionServiceAdapterDescriptor descriptor,
      final Pair<String, String> adapterPackageAndClass,
      final TypeElement annotation) {
    final List<Pair<ClassName, ClassName>> fromToMappings =
        roundEnv.getElementsAnnotatedWith(annotation).stream()
            .filter(mapper -> mapper.asType().getKind() == DECLARED)
            .filter(mapper -> getConverterSupertype(mapper).isPresent())
            .map(this::toConvertMethod)
            .filter(Objects::nonNull)
            .map(ExecutableElement.class::cast)
            .map(this::toFromToMapping)
            .collect(toList());
    descriptor.setFromToMappings(fromToMappings);
    writeAdapterClassFile(descriptor, adapterPackageAndClass);
  }

  private Pair<ClassName, ClassName> toFromToMapping(final ExecutableElement convert) {
    return Pair.of(
        (ClassName)
            convert.getParameters().stream()
                .map(Element::asType)
                .map(ClassName::get)
                .findFirst()
                .get(),
        (ClassName) ClassName.get(convert.getReturnType()));
  }

  private Element toConvertMethod(final Element mapper) {
    return mapper.getEnclosedElements().stream()
        .filter(element -> element.getKind() == METHOD)
        .filter(method -> method.getModifiers().contains(PUBLIC))
        .filter(method -> method.getSimpleName().contentEquals("convert"))
        .filter(convert -> ((ExecutableElement) convert).getParameters().size() == 1)
        .filter(
            convert ->
                processingEnv
                    .getTypeUtils()
                    .isSameType(
                        getFirstParameterType((ExecutableElement) convert),
                        getFirstTypeArgument(getConverterSupertype(mapper).get())))
        .findFirst()
        .orElse(null);
  }

  private void writeAdapterClassFile(
      final ConversionServiceAdapterDescriptor descriptor,
      final Pair<String, String> adapterPackageAndClass) {
    try (final Writer outputWriter =
        processingEnv
            .getFiler()
            .createSourceFile(
                adapterPackageAndClass.getLeft() + "." + adapterPackageAndClass.getRight())
            .openWriter()) {
      adapterGenerator.writeConversionServiceAdapter(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              "Error while opening "
                  + adapterPackageAndClass.getRight()
                  + " output file: "
                  + e.getMessage());
    }
  }

  private Pair<String, String> getAdapterPackageAndClassName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final MutablePair<String, String> packageAndClass =
        MutablePair.of(
            ConverterMapperProcessor.class.getPackage().getName(), "ConversionServiceAdapter");
    for (final TypeElement annotation : annotations) {
      if (SPRING_MAPPER_CONFIG.contentEquals(annotation.getQualifiedName())) {
        roundEnv
            .getElementsAnnotatedWith(annotation)
            .forEach(element -> updateFromDeclaration(element, packageAndClass));
      }
    }
    return packageAndClass;
  }

  private void updateFromDeclaration(
      final Element element, final MutablePair<String, String> adapterPackageAndClass) {
    final SpringMapperConfig springMapperConfig = element.getAnnotation(SpringMapperConfig.class);
    adapterPackageAndClass.setLeft(
        Optional.of(springMapperConfig.conversionServiceAdapterPackage())
            .filter(StringUtils::isNotBlank)
            .orElse(
                String.valueOf(
                    processingEnv.getElementUtils().getPackageOf(element).getQualifiedName())));
    adapterPackageAndClass.setRight(springMapperConfig.conversionServiceAdapterClassName());
  }

  private Optional<? extends TypeMirror> getConverterSupertype(final Element mapper) {
    final Types typeUtils = processingEnv.getTypeUtils();
    return typeUtils.directSupertypes(mapper.asType()).stream()
        .filter(
            supertype -> typeUtils.erasure(supertype).toString().equals(SPRING_CONVERTER_FULL_NAME))
        .findFirst();
  }

  private static TypeMirror getFirstParameterType(final ExecutableElement convert) {
    return convert.getParameters().stream().findFirst().map(Element::asType).orElse(null);
  }

  private static TypeMirror getFirstTypeArgument(final TypeMirror converterSupertype) {
    return ((DeclaredType) converterSupertype).getTypeArguments().stream().findFirst().orElse(null);
  }
}
