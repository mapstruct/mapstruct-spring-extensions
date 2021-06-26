package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.extensions.spring.SpringMapperConfig;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.tools.Diagnostic.Kind.ERROR;

@SupportedAnnotationTypes({
  ConverterMapperProcessor.MAPPER,
  ConverterMapperProcessor.SPRING_MAPPER_CONFIG
})
public class ConverterMapperProcessor extends AbstractProcessor {
  protected static final String MAPPER = "org.mapstruct.Mapper";
  protected static final String SPRING_MAPPER_CONFIG =
      "org.mapstruct.extensions.spring.SpringMapperConfig";
  protected static final String SPRING_CONVERTER_FULL_NAME =
      "org.springframework.core.convert.converter.Converter";

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
    descriptor.setAdapterClassName(getAdapterClassName(annotations, roundEnv));
    descriptor.setConversionServiceBeanName(getConversionServiceName(annotations, roundEnv));
    descriptor.setLazyAnnotatedConversionServiceBean(
        getLazyAnnotatedConversionServiceBean(annotations, roundEnv));
    descriptor.setFromToMappings(getExternalConversionMappings(annotations, roundEnv));
    annotations.stream()
        .filter(this::isMapperAnnotation)
        .forEach(annotation -> processMapperAnnotation(roundEnv, descriptor, annotation));
    return false;
  }

  private List<Pair<TypeName, TypeName>> getExternalConversionMappings(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(this::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .flatMap(this::toSpringMapperConfigMirror)
        .map(AnnotationMirror::getElementValues)
        .flatMap(
            map ->
                map.entrySet().stream()
                    .filter(
                        entry ->
                            entry.getKey().getSimpleName().contentEquals("externalConversions"))
                    .findFirst())
        .map(Map.Entry::getValue)
        .map(AnnotationValue::getValue)
        .map(objectValue -> (List<? extends AnnotationMirror>) objectValue)
        .map(
            list ->
                list.stream()
                    .map(AnnotationMirror::getElementValues)
                    .map(
                        elementMap ->
                            Pair.of(
                                TypeName.get(findSourceType(elementMap)),
                                TypeName.get(findTargetType(elementMap))))
                    .collect(toList()))
        .orElse(emptyList());
  }

  private static TypeMirror findTargetType(
      Map<? extends ExecutableElement, ? extends AnnotationValue> externalConversionElementMap) {
    return findTypeMirrorAttribute(externalConversionElementMap, "targetType");
  }

  private static TypeMirror findSourceType(
      Map<? extends ExecutableElement, ? extends AnnotationValue> externalConversionElementMap) {
    return findTypeMirrorAttribute(externalConversionElementMap, "sourceType");
  }

  private static TypeMirror findTypeMirrorAttribute(
      Map<? extends ExecutableElement, ? extends AnnotationValue> externalConversionElementMap,
      String attributeName) {
    return externalConversionElementMap.entrySet().stream()
        .filter(entry -> entry.getKey().getSimpleName().contentEquals(attributeName))
        .map(Map.Entry::getValue)
        .map(AnnotationValue::getValue)
        .map(TypeMirror.class::cast)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  private boolean isMapperAnnotation(TypeElement annotation) {
    return MAPPER.contentEquals(annotation.getQualifiedName());
  }

  private void processMapperAnnotation(
      final RoundEnvironment roundEnv,
      final ConversionServiceAdapterDescriptor descriptor,
      final TypeElement annotation) {
    final List<Pair<TypeName, TypeName>> fromToMappings =
        roundEnv.getElementsAnnotatedWith(annotation).stream()
            .filter(this::isKindDeclared)
            .filter(this::hasConverterSupertype)
            .map(this::toConvertMethod)
            .filter(Objects::nonNull)
            .map(ExecutableElement.class::cast)
            .map(this::toFromToMapping)
            .collect(toCollection(ArrayList::new));
    fromToMappings.addAll(descriptor.getFromToMappings());
    descriptor.setFromToMappings(fromToMappings);
    writeAdapterClassFile(descriptor);
  }

  private boolean hasConverterSupertype(Element mapper) {
    return getConverterSupertype(mapper).isPresent();
  }

  private boolean isKindDeclared(Element mapper) {
    return mapper.asType().getKind() == DECLARED;
  }

  private Pair<TypeName, TypeName> toFromToMapping(final ExecutableElement convert) {
    return Pair.of(
        convert.getParameters().stream()
            .map(Element::asType)
            .map(TypeName::get)
            .findFirst()
            .orElseThrow(NoSuchElementException::new),
        TypeName.get(convert.getReturnType()));
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

  private void writeAdapterClassFile(final ConversionServiceAdapterDescriptor descriptor) {
    try (final Writer outputWriter =
        processingEnv
            .getFiler()
            .createSourceFile(descriptor.getAdapterClassName().canonicalName())
            .openWriter()) {
      adapterGenerator.writeConversionServiceAdapter(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              "Error while opening "
                  + descriptor.getAdapterClassName().simpleName()
                  + " output file: "
                  + e.getMessage());
    }
  }

  private ClassName getAdapterClassName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final MutablePair<String, String> packageAndClass =
        MutablePair.of(
            ConverterMapperProcessor.class.getPackage().getName(), "ConversionServiceAdapter");
    for (final TypeElement annotation : annotations) {
      if (isSpringMapperConfigAnnotation(annotation)) {
        roundEnv
            .getElementsAnnotatedWith(annotation)
            .forEach(element -> updateFromDeclaration(element, packageAndClass));
      }
    }
    return ClassName.get(packageAndClass.getLeft(), packageAndClass.getRight());
  }

  private boolean isSpringMapperConfigAnnotation(TypeElement annotation) {
    return SPRING_MAPPER_CONFIG.contentEquals(annotation.getQualifiedName());
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

  private String getConversionServiceName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(this::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .map(this::toSpringMapperConfig)
        .map(SpringMapperConfig::conversionServiceBeanName)
        .orElse(null);
  }

  private static Optional<? extends Element> findFirstElementAnnotatedWith(
      final RoundEnvironment roundEnv, final TypeElement annotation) {
    return roundEnv.getElementsAnnotatedWith(annotation).stream().findFirst();
  }

  private SpringMapperConfig toSpringMapperConfig(final Element element) {
    return element.getAnnotation(SpringMapperConfig.class);
  }

  private Optional<? extends AnnotationMirror> toSpringMapperConfigMirror(final Element element) {
    return element.getAnnotationMirrors().stream()
        .filter(
            annotationMirror ->
                processingEnv
                    .getElementUtils()
                    .getTypeElement(SpringMapperConfig.class.getName())
                    .asType()
                    .equals(annotationMirror.getAnnotationType().asElement().asType()))
        .findFirst();
  }

  private boolean getLazyAnnotatedConversionServiceBean(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(this::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .map(this::toSpringMapperConfig)
        .map(SpringMapperConfig::lazyAnnotatedConversionServiceBean)
        .orElse(Boolean.TRUE);
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
