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
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Boolean.TRUE;
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
    final ConversionServiceAdapterDescriptor descriptor = buildDescriptor(annotations, roundEnv);
    annotations.stream()
        .filter(this::isMapperAnnotation)
        .forEach(annotation -> processMapperAnnotation(roundEnv, descriptor, annotation));
    return false;
  }

  private ConversionServiceAdapterDescriptor buildDescriptor(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return new ConversionServiceAdapterDescriptor()
        .adapterClassName(getAdapterClassName(annotations, roundEnv))
        .conversionServiceBeanName(getConversionServiceName(annotations, roundEnv))
        .lazyAnnotatedConversionServiceBean(
            getLazyAnnotatedConversionServiceBean(annotations, roundEnv))
        .fromToMappings(getExternalConversionMappings(annotations, roundEnv));
  }

  private List<Pair<TypeName, TypeName>> getExternalConversionMappings(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final Optional<List<Pair<TypeName, TypeName>>> pairs = annotations.stream()
            .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
            .findFirst()
            .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
            .flatMap(this::toSpringMapperConfigMirror)
            .map(AnnotationMirror::getElementValues)
            .flatMap(this::extractExternalConversions)
            .map(Entry::getValue)
            .map(AnnotationValue::getValue)
            .map(List.class::cast)
            .map(this::toSourceTargetTypeNamePairs);
    return pairs
        .orElse(emptyList());
  }

  private List<Pair<TypeName, TypeName>> toSourceTargetTypeNamePairs(
      final List<? extends AnnotationMirror> list) {
    return list.stream()
        .map(AnnotationMirror::getElementValues)
        .map(this::toSourceTargetTypeNamePair)
        .collect(toList());
  }

  private Pair<TypeName, TypeName> toSourceTargetTypeNamePair(
      final Map<? extends ExecutableElement, ? extends AnnotationValue> elementMap) {
    return Pair.of(
        TypeName.get(findSourceType(elementMap)), TypeName.get(findTargetType(elementMap)));
  }

  private Optional<? extends Entry<? extends ExecutableElement, ? extends AnnotationValue>>
      extractExternalConversions(
          final Map<? extends ExecutableElement, ? extends AnnotationValue> map) {
    return map.entrySet().stream().filter(this::hasNameExternalConversions).findFirst();
  }

  private boolean hasNameExternalConversions(
      final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry) {
    return hasName(entry.getKey().getSimpleName(), "externalConversions");
  }

  private static TypeMirror findTargetType(
      final Map<? extends ExecutableElement, ? extends AnnotationValue>
          externalConversionElementMap) {
    return findTypeMirrorAttribute(externalConversionElementMap, "targetType");
  }

  private static TypeMirror findSourceType(
      final Map<? extends ExecutableElement, ? extends AnnotationValue>
          externalConversionElementMap) {
    return findTypeMirrorAttribute(externalConversionElementMap, "sourceType");
  }

  private static TypeMirror findTypeMirrorAttribute(
      final Map<? extends ExecutableElement, ? extends AnnotationValue>
          externalConversionElementMap,
      final String attributeName) {
    return externalConversionElementMap.entrySet().stream()
        .filter(entry -> hasName(entry.getKey().getSimpleName(), attributeName))
        .map(Entry::getValue)
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
            .filter(ConverterMapperProcessor::isKindDeclared)
            .filter(this::hasConverterSupertype)
            .map(this::toConvertMethod)
            .filter(Objects::nonNull)
            .map(ExecutableElement.class::cast)
            .map(ConverterMapperProcessor::toFromToMapping)
            .collect(toCollection(ArrayList::new));
    fromToMappings.addAll(descriptor.getFromToMappings());
    descriptor.fromToMappings(fromToMappings);
    writeAdapterClassFile(descriptor);
  }

  private boolean hasConverterSupertype(Element mapper) {
    return getConverterSupertype(mapper).isPresent();
  }

  private static boolean isKindDeclared(Element mapper) {
    return mapper.asType().getKind() == DECLARED;
  }

  private static Pair<TypeName, TypeName> toFromToMapping(final ExecutableElement convert) {
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
        .filter(ConverterMapperProcessor::isAMethod)
        .filter(ConverterMapperProcessor::isPublic)
        .filter(ConverterMapperProcessor::hasNameConvert)
        .map(ExecutableElement.class::cast)
        .filter(ConverterMapperProcessor::hasExactlyOneParameter)
        .filter(convert -> parameterTypeMatches(convert, mapper))
        .findFirst()
        .orElse(null);
  }

  private boolean parameterTypeMatches(final ExecutableElement convert, final Element mapper) {
    return processingEnv
        .getTypeUtils()
        .isSameType(
            getFirstParameterType(convert),
            getFirstTypeArgument(getConverterSupertype(mapper).orElseThrow(NoSuchElementException::new)));
  }

  private static boolean hasExactlyOneParameter(final ExecutableElement convert) {
    return convert.getParameters().size() == 1;
  }

  private static boolean hasNameConvert(final Element method) {
    return hasName(method.getSimpleName(), "convert");
  }

  private static boolean hasName(final Name name, final String comparisonName) {
    return name.contentEquals(comparisonName);
  }

  private static boolean isPublic(final Element method) {
    return method.getModifiers().contains(PUBLIC);
  }

  private static boolean isAMethod(final Element element) {
    return element.getKind() == METHOD;
  }

  private void writeAdapterClassFile(final ConversionServiceAdapterDescriptor descriptor) {
    try (final Writer outputWriter = openAdapterFile(descriptor)) {
      adapterGenerator.writeConversionServiceAdapter(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              String.format(
                  "Error while opening %s output file: %s",
                  descriptor.getAdapterClassName().simpleName(), e.getMessage()));
    }
  }

  private Writer openAdapterFile(final ConversionServiceAdapterDescriptor descriptor)
      throws IOException {
    return processingEnv
        .getFiler()
        .createSourceFile(descriptor.getAdapterClassName().canonicalName())
        .openWriter();
  }

  private ClassName getAdapterClassName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final MutablePair<String, String> packageAndClass = defaultPackageAndClassName();
    updateFromConfigAnnotationIfFound(annotations, roundEnv, packageAndClass);
    return ClassName.get(packageAndClass.getLeft(), packageAndClass.getRight());
  }

  private static MutablePair<String, String> defaultPackageAndClassName() {
    return MutablePair.of(
        ConverterMapperProcessor.class.getPackage().getName(), "ConversionServiceAdapter");
  }

  private void updateFromConfigAnnotationIfFound(
      final Set<? extends TypeElement> annotations,
      final RoundEnvironment roundEnv,
      final MutablePair<String, String> packageAndClass) {
    annotations.stream()
        .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
        .forEach(annotation -> updateFromSpringMapperConfig(packageAndClass, annotation, roundEnv));
  }

  private void updateFromSpringMapperConfig(
      final MutablePair<String, String> packageAndClass,
      final TypeElement springMapperConfig,
      final RoundEnvironment roundEnv) {
    roundEnv
        .getElementsAnnotatedWith(springMapperConfig)
        .forEach(element -> updateFromDeclaration(packageAndClass, element));
  }

  private static boolean isSpringMapperConfigAnnotation(TypeElement annotation) {
    return SPRING_MAPPER_CONFIG.contentEquals(annotation.getQualifiedName());
  }

  private void updateFromDeclaration(
      final MutablePair<String, String> adapterPackageAndClass, final Element element) {
    final SpringMapperConfig springMapperConfig = element.getAnnotation(SpringMapperConfig.class);
    adapterPackageAndClass.setLeft(
        Optional.of(springMapperConfig.conversionServiceAdapterPackage())
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> getPackageName(element)));
    adapterPackageAndClass.setRight(springMapperConfig.conversionServiceAdapterClassName());
  }

  private String getPackageName(Element element) {
    return String.valueOf(processingEnv.getElementUtils().getPackageOf(element).getQualifiedName());
  }

  private String getConversionServiceName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
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
        .filter(this::isSpringMapperConfigMirror)
        .findFirst();
  }

  private boolean isSpringMapperConfigMirror(final AnnotationMirror annotationMirror) {
    return processingEnv
        .getElementUtils()
        .getTypeElement(SpringMapperConfig.class.getName())
        .asType()
        .equals(annotationMirror.getAnnotationType().asElement().asType());
  }

  private boolean getLazyAnnotatedConversionServiceBean(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .map(this::toSpringMapperConfig)
        .map(SpringMapperConfig::lazyAnnotatedConversionServiceBean)
        .orElse(TRUE);
  }

  private Optional<? extends TypeMirror> getConverterSupertype(final Element mapper) {
    return getDirectSupertypes(mapper).stream().filter(this::isSpringConverterType).findFirst();
  }

  private List<? extends TypeMirror> getDirectSupertypes(final Element mapper) {
    return processingEnv.getTypeUtils().directSupertypes(mapper.asType());
  }

  private boolean isSpringConverterType(final TypeMirror supertype) {
    return processingEnv
        .getTypeUtils()
        .erasure(supertype)
        .toString()
        .equals(SPRING_CONVERTER_FULL_NAME);
  }

  private static TypeMirror getFirstParameterType(final ExecutableElement convert) {
    return convert.getParameters().stream().findFirst().map(Element::asType).orElse(null);
  }

  private static TypeMirror getFirstTypeArgument(final TypeMirror converterSupertype) {
    return ((DeclaredType) converterSupertype).getTypeArguments().stream().findFirst().orElse(null);
  }
}
