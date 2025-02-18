package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.tools.Diagnostic.Kind.ERROR;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.mapstruct.extensions.spring.SpringMapperConfig.DEFAULT_CONFIGURATION_CLASS_NAME;
import static org.mapstruct.extensions.spring.SpringMapperConfig.DEFAULT_CONVERSION_SERVICE_BEAN_NAME;
import static org.mapstruct.extensions.spring.converter.ModelElementUtils.hasName;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mapstruct.extensions.spring.AdapterMethodName;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@SupportedAnnotationTypes({
  ConverterMapperProcessor.MAPPER,
  ConverterMapperProcessor.SPRING_MAPPER_CONFIG,
  ConverterMapperProcessor.DELEGATING_CONVERTER
})
public class ConverterMapperProcessor extends GeneratorInitializingProcessor {
  protected static final String DELEGATING_CONVERTER =
      "org.mapstruct.extensions.spring.DelegatingConverter";
  protected static final String MAPPER = "org.mapstruct.Mapper";
  protected static final String SPRING_MAPPER_CONFIG =
      "org.mapstruct.extensions.spring.SpringMapperConfig";
  protected static final String SPRING_CONVERTER_FULL_NAME =
      "org.springframework.core.convert.converter.Converter";

  private final ConversionServiceAdapterGenerator adapterGenerator;
  private final ConverterScanGenerator converterScanGenerator;
  private final ConverterScansGenerator converterScansGenerator;
  private final ConverterRegistrationConfigurationGenerator
      converterRegistrationConfigurationGenerator;
  private final DelegatingConverterGenerator delegatingConverterGenerator;

  public ConverterMapperProcessor() {
    this(Clock.systemUTC());
  }

  ConverterMapperProcessor(final Clock clock) {
    this(
        new ConversionServiceAdapterGenerator(clock),
        new ConverterScanGenerator(clock),
        new ConverterScansGenerator(clock),
        new ConverterRegistrationConfigurationGenerator(clock),
        new DelegatingConverterGenerator(clock));
  }

  ConverterMapperProcessor(
      final ConversionServiceAdapterGenerator adapterGenerator,
      final ConverterScanGenerator converterScanGenerator,
      final ConverterScansGenerator converterScansGenerator,
      final ConverterRegistrationConfigurationGenerator converterRegistrationConfigurationGenerator,
      final DelegatingConverterGenerator delegatingConverterGenerator) {
    super(
        adapterGenerator,
        converterScanGenerator,
        converterScansGenerator,
        converterRegistrationConfigurationGenerator,
        delegatingConverterGenerator);
    this.adapterGenerator = adapterGenerator;
    this.converterScanGenerator = converterScanGenerator;
    this.converterScansGenerator = converterScansGenerator;
    this.converterRegistrationConfigurationGenerator = converterRegistrationConfigurationGenerator;
    this.delegatingConverterGenerator = delegatingConverterGenerator;
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    final var delegatingConverterDescriptors =
        annotations.stream()
            .filter(ConverterMapperProcessor::isDelegatingConverterAnnotation)
            .map(roundEnv::getElementsAnnotatedWith)
            .flatMap(Set::stream)
            .map(ExecutableElement.class::cast)
            .map(
                annotatedMethod ->
                    new DelegatingConverterDescriptor(annotatedMethod, processingEnv))
            .collect(toList());
    delegatingConverterDescriptors.forEach(this::writeDelegatingConverterFile);

    final ConversionServiceAdapterDescriptor adapterDescriptor =
        buildAdapterDescriptor(annotations, roundEnv);
    annotations.stream()
        .filter(ConverterMapperProcessor::isMapperAnnotation)
        .forEach(
            annotation ->
                processMapperAnnotation(
                    roundEnv, adapterDescriptor, delegatingConverterDescriptors, annotation));
    return false;
  }

  private void writeDelegatingConverterFile(final DelegatingConverterDescriptor descriptor) {
    try (final Writer outputWriter = openSourceFile(descriptor::getConverterClassName)) {
      delegatingConverterGenerator.writeGeneratedCodeToOutput(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              String.format(
                  "Error while opening %s output file: %s",
                  descriptor.getConverterClassName().simpleName(), e.getMessage()));
    }
  }

  private static boolean isDelegatingConverterAnnotation(final TypeElement annotation) {
    return DELEGATING_CONVERTER.contentEquals(annotation.getQualifiedName());
  }

  private ConversionServiceAdapterDescriptor buildAdapterDescriptor(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return new ConversionServiceAdapterDescriptor()
        .adapterClassName(getAdapterClassName(annotations, roundEnv))
        .conversionServiceBeanName(getConversionServiceBeanName(annotations, roundEnv))
        .generateConverterScan(getGenerateConverterScan(annotations, roundEnv))
        .lazyAnnotatedConversionServiceBean(
            getLazyAnnotatedConversionServiceBean(annotations, roundEnv))
        .configurationClassName(
                getConfigurationClassName(annotations, roundEnv))
        .fromToMappings(getExternalConversionMappings(annotations, roundEnv));
  }

  private List<FromToMapping> getExternalConversionMappings(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return annotations.stream()
        .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .flatMap(this::toSpringMapperConfigMirror)
        .map(AnnotationMirror::getElementValues)
        .flatMap(this::extractExternalConversions)
        .map(Entry::getValue)
        .map(AnnotationValue::getValue)
        .map(List.class::cast)
        .map(this::toFromToMappings)
        .orElse(emptyList());
  }

  private List<FromToMapping> toFromToMappings(final List<? extends AnnotationMirror> list) {
    return list.stream()
        .map(AnnotationMirror::getElementValues)
        .map(this::toFromToMapping)
        .collect(toList());
  }

  private FromToMapping toFromToMapping(
      final Map<? extends ExecutableElement, ? extends AnnotationValue> elementMap) {
    return new FromToMapping()
        .source(TypeName.get(findSourceType(elementMap)))
        .target(TypeName.get(findTargetType(elementMap)))
        .adapterMethodName(findAdapterMethodName(elementMap));
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

  private static String findAdapterMethodName(
      final Map<? extends ExecutableElement, ? extends AnnotationValue>
          externalConversionElementMap) {
    return defaultIfBlank(
        findAttribute(externalConversionElementMap, "adapterMethodName", String.class), null);
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
    return findAttribute(externalConversionElementMap, attributeName, TypeMirror.class);
  }

  private static <T> T findAttribute(
      final Map<? extends ExecutableElement, ? extends AnnotationValue>
          externalConversionElementMap,
      final String attributeName,
      final Class<T> attributeClass) {
    return externalConversionElementMap.entrySet().stream()
        .filter(entry -> hasName(entry.getKey().getSimpleName(), attributeName))
        .map(Entry::getValue)
        .map(AnnotationValue::getValue)
        .map(attributeClass::cast)
        .findFirst()
        .orElse(null);
  }

  private static boolean isMapperAnnotation(final TypeElement annotation) {
    return MAPPER.contentEquals(annotation.getQualifiedName());
  }

  private void processMapperAnnotation(
      final RoundEnvironment roundEnv,
      final ConversionServiceAdapterDescriptor adapterDescriptor,
      final List<DelegatingConverterDescriptor> delegatingConverterDescriptors,
      final TypeElement annotation) {
    final List<FromToMapping> fromToMappings =
        roundEnv.getElementsAnnotatedWith(annotation).stream()
            .filter(ConverterMapperProcessor::isKindDeclared)
            .filter(this::hasConverterSupertype)
            .map(this::toFromToMapping)
            .collect(toCollection(ArrayList::new));
    fromToMappings.addAll(adapterDescriptor.getFromToMappings());
    fromToMappings.addAll(
        delegatingConverterDescriptors.stream()
            .map(DelegatingConverterDescriptor::getFromToMapping)
            .collect(toList()));
    adapterDescriptor.fromToMappings(fromToMappings);
    writeAdapterClassFile(adapterDescriptor);
    if (adapterDescriptor.isGenerateConverterScan()) {
      writeConverterScanFiles(adapterDescriptor);
    }
  }

  private boolean hasConverterSupertype(Element mapper) {
    return getConverterSupertype(mapper).isPresent();
  }

  private static boolean isKindDeclared(Element mapper) {
    return mapper.asType().getKind() == DECLARED;
  }

  private FromToMapping toFromToMapping(final Element mapper) {
    final var sourceTypeTargetType = toTypeArguments(mapper);

    return new FromToMapping()
        .source(TypeName.get(sourceTypeTargetType.get(0)))
        .target(TypeName.get(sourceTypeTargetType.get(1)))
        .adapterMethodName(
            Optional.ofNullable(mapper.getAnnotation(AdapterMethodName.class))
                .map(AdapterMethodName::value)
                .orElse(null));
  }

  private List<? extends TypeMirror> toTypeArguments(final Element mapper) {
    return ((DeclaredType) getConverterSupertype(mapper).orElseThrow()).getTypeArguments();
  }

  private void writeAdapterClassFile(final ConversionServiceAdapterDescriptor descriptor) {
    writeOutputFile(
        descriptor, this::openAdapterFile, adapterGenerator, descriptor::getAdapterClassName);
  }

  private void writeConverterScanFiles(final ConversionServiceAdapterDescriptor descriptor) {
    writeOutputFile(
        descriptor,
        this::openConverterScanFile,
        converterScanGenerator,
        descriptor::getConverterScanClassName);
    writeOutputFile(
        descriptor,
        this::openConverterScansFile,
        converterScansGenerator,
        descriptor::getConverterScansClassName);
    writeOutputFile(
        descriptor,
        this::openConverterRegistrationConfigurationFile,
        converterRegistrationConfigurationGenerator,
        descriptor::getConverterRegistrationConfigurationClassName);
  }

  private interface OpenFileFunction {
    Writer open(ConversionServiceAdapterDescriptor descriptor) throws IOException;
  }

  private void writeOutputFile(
      final ConversionServiceAdapterDescriptor descriptor,
      final OpenFileFunction openFileFunction,
      final AdapterRelatedGenerator generator,
      final Supplier<ClassName> outputFileClassNameSupplier) {
    try (final Writer outputWriter = openFileFunction.open(descriptor)) {
      generator.writeGeneratedCodeToOutput(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              String.format(
                  "Error while opening %s output file: %s",
                  outputFileClassNameSupplier.get().simpleName(), e.getMessage()));
    }
  }

  private Writer openConverterRegistrationConfigurationFile(
      final ConversionServiceAdapterDescriptor descriptor) throws IOException {
    return openSourceFile(descriptor::getConverterRegistrationConfigurationClassName);
  }

  private Writer openConverterScanFile(final ConversionServiceAdapterDescriptor descriptor)
      throws IOException {
    return openSourceFile(descriptor::getConverterScanClassName);
  }

  private Writer openConverterScansFile(final ConversionServiceAdapterDescriptor descriptor)
      throws IOException {
    return openSourceFile(descriptor::getConverterScansClassName);
  }

  private Writer openAdapterFile(final ConversionServiceAdapterDescriptor descriptor)
      throws IOException {
    return openSourceFile(descriptor::getAdapterClassName);
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
    final SpringMapperConfig springMapperConfig = toSpringMapperConfig(element);
    adapterPackageAndClass.setLeft(
        Optional.of(springMapperConfig.conversionServiceAdapterPackage())
            .filter(StringUtils::isNotBlank)
            .orElseGet(() -> getPackageName(element)));
    adapterPackageAndClass.setRight(springMapperConfig.conversionServiceAdapterClassName());
  }

  private String getPackageName(Element element) {
    return String.valueOf(processingEnv.getElementUtils().getPackageOf(element).getQualifiedName());
  }

  private static String getConversionServiceBeanName(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return getConfigAnnotationAttribute(
        annotations, roundEnv, SpringMapperConfig::conversionServiceBeanName, DEFAULT_CONVERSION_SERVICE_BEAN_NAME);
  }

  private static Optional<? extends Element> findFirstElementAnnotatedWith(
      final RoundEnvironment roundEnv, final TypeElement annotation) {
    return roundEnv.getElementsAnnotatedWith(annotation).stream().findFirst();
  }

  private static SpringMapperConfig toSpringMapperConfig(final Element element) {
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

  private static boolean getLazyAnnotatedConversionServiceBean(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return getConfigAnnotationAttribute(
        annotations, roundEnv, SpringMapperConfig::lazyAnnotatedConversionServiceBean, TRUE);
  }

  private static String getConfigurationClassName(
          final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return getConfigAnnotationAttribute(
            annotations,
            roundEnv,
            SpringMapperConfig::converterRegistrationConfigurationClassName,
            DEFAULT_CONFIGURATION_CLASS_NAME);
  }

  private static boolean getGenerateConverterScan(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    return getConfigAnnotationAttribute(
        annotations, roundEnv, SpringMapperConfig::generateConverterScan, FALSE);
  }

  private static <T> T getConfigAnnotationAttribute(
      final Set<? extends TypeElement> annotations,
      final RoundEnvironment roundEnv,
      final Function<SpringMapperConfig, T> attributeGetter,
      final T defaultValue) {
    return annotations.stream()
        .filter(ConverterMapperProcessor::isSpringMapperConfigAnnotation)
        .findFirst()
        .flatMap(annotation -> findFirstElementAnnotatedWith(roundEnv, annotation))
        .map(ConverterMapperProcessor::toSpringMapperConfig)
        .map(attributeGetter)
        .orElse(defaultValue);
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
}
