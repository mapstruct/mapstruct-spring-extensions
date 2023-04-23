package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.WARNING;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.squareup.javapoet.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.tuple.Pair;

public class ConversionServiceAdapterGenerator {
  private static final ClassName CONVERSION_SERVICE_CLASS_NAME =
      ClassName.get("org.springframework.core.convert", "ConversionService");
  private static final String CONVERSION_SERVICE_FIELD_NAME = "conversionService";
  private static final ClassName QUALIFIER_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.beans.factory.annotation", "Qualifier");
  private static final ClassName LAZY_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.context.annotation", "Lazy");
  private static final ClassName TYPE_DESCRIPTOR_CLASS_NAME =
      ClassName.get("org.springframework.core.convert", "TypeDescriptor");
  private static final String GENERATED_ANNOTATION_CLASS_NAME_STRING = "Generated";
  private static final String PRE_JAVA_9_ANNOTATION_GENERATED_PACKAGE = "javax.annotation";
  private static final ClassName PRE_JAVA_9_ANNOTATION_GENERATED_CLASS_NAME =
      ClassName.get(
          PRE_JAVA_9_ANNOTATION_GENERATED_PACKAGE, GENERATED_ANNOTATION_CLASS_NAME_STRING);
  private static final String JAVA_9_PLUS_ANNOTATION_GENERATED_PACKAGE =
      "javax.annotation.processing";
  private static final ClassName JAVA_9_PLUS_ANNOTATION_GENERATED_CLASS_NAME =
      ClassName.get(
          JAVA_9_PLUS_ANNOTATION_GENERATED_PACKAGE, GENERATED_ANNOTATION_CLASS_NAME_STRING);
  private static final String PRE_JAVA_9_ANNOTATION_GENERATED =
      String.format(
          "%s.%s", PRE_JAVA_9_ANNOTATION_GENERATED_PACKAGE, GENERATED_ANNOTATION_CLASS_NAME_STRING);
  private static final String JAVA_9_PLUS_ANNOTATION_GENERATED =
      String.format(
          "%s.%s",
          JAVA_9_PLUS_ANNOTATION_GENERATED_PACKAGE, GENERATED_ANNOTATION_CLASS_NAME_STRING);
  private static final ClassName COMPONENT_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.stereotype", "Component");
  private final Clock clock;

  private final AtomicReference<ProcessingEnvironment> processingEnvironment;

  public ConversionServiceAdapterGenerator(final Clock clock) {
    this.clock = clock;
    processingEnvironment = new AtomicReference<>();
  }

  
  ProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironment.get();
  }
  
  public void writeConversionServiceAdapter(
      final ConversionServiceAdapterDescriptor descriptor, final Writer out) {
    try {
      JavaFile.builder(
              descriptor.getAdapterClassName().packageName(),
              createConversionServiceTypeSpec(descriptor))
          .build()
          .writeTo(out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private TypeSpec createConversionServiceTypeSpec(
      final ConversionServiceAdapterDescriptor descriptor) {
    final FieldSpec conversionServiceFieldSpec = buildConversionServiceFieldSpec();
    final TypeSpec.Builder adapterClassTypeSpec =
        TypeSpec.classBuilder(descriptor.getAdapterClassName()).addModifiers(PUBLIC);
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(adapterClassTypeSpec::addAnnotation);
    return adapterClassTypeSpec
        .addAnnotation(COMPONENT_ANNOTATION_CLASS_NAME)
        .addField(conversionServiceFieldSpec)
        .addMethod(buildConstructorSpec(descriptor, conversionServiceFieldSpec))
        .addMethods(buildMappingMethods(descriptor, conversionServiceFieldSpec))
        .build();
  }

  private static MethodSpec buildConstructorSpec(
      final ConversionServiceAdapterDescriptor descriptor,
      final FieldSpec conversionServiceFieldSpec) {
    final ParameterSpec constructorParameterSpec =
        buildConstructorParameterSpec(descriptor, conversionServiceFieldSpec);
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(constructorParameterSpec)
        .addStatement("this.$N = $N", conversionServiceFieldSpec, constructorParameterSpec)
        .build();
  }

  private static ParameterSpec buildConstructorParameterSpec(
      final ConversionServiceAdapterDescriptor descriptor,
      final FieldSpec conversionServiceFieldSpec) {
    final ParameterSpec.Builder parameterBuilder =
        ParameterSpec.builder(
            conversionServiceFieldSpec.type, conversionServiceFieldSpec.name, FINAL);
    if (isNotEmpty(descriptor.getConversionServiceBeanName())) {
      parameterBuilder.addAnnotation(buildQualifierAnnotation(descriptor));
    }
    if (TRUE.equals(descriptor.isLazyAnnotatedConversionServiceBean())) {
      parameterBuilder.addAnnotation(buildLazyAnnotation());
    }
    return parameterBuilder.build();
  }

  private static AnnotationSpec buildQualifierAnnotation(
      final ConversionServiceAdapterDescriptor descriptor) {
    return AnnotationSpec.builder(QUALIFIER_ANNOTATION_CLASS_NAME)
        .addMember("value", "$S", descriptor.getConversionServiceBeanName())
        .build();
  }

  private static AnnotationSpec buildLazyAnnotation() {
    return AnnotationSpec.builder(LAZY_ANNOTATION_CLASS_NAME).build();
  }

  private String collectionOfMethodName(final ParameterizedTypeName parameterizedTypeName) {
    if (isCollectionWithGenericParameter(parameterizedTypeName)) {
      return simpleName(parameterizedTypeName)
          + "Of"
          + collectionOfNameIfApplicable(parameterizedTypeName.typeArguments.iterator().next());
    }

    return simpleName(parameterizedTypeName);
  }

  private boolean isCollectionWithGenericParameter(final ParameterizedTypeName parameterizedTypeName) {
    return parameterizedTypeName.typeArguments != null
            && parameterizedTypeName.typeArguments.size() > 0
            && isCollection(parameterizedTypeName);
  }

  private boolean isCollection(final ParameterizedTypeName parameterizedTypeName) {
    try {
      return Collection.class.isAssignableFrom(
          Class.forName(parameterizedTypeName.rawType.canonicalName()));
    } catch (ClassNotFoundException e) {
      processingEnvironment
          .get()
          .getMessager()
          .printMessage(
              WARNING,
              "Caught ClassNotFoundException when trying to resolve parameterized type: "
                  + e.getMessage());
      return false;
    }
  }

  private String collectionOfNameIfApplicable(final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return collectionOfMethodName((ParameterizedTypeName) typeName);
    }
    return simpleName(typeName);
  }

  private static String simpleName(final TypeName typeName) {
    final TypeName rawType = rawType(typeName);
    if (rawType instanceof ArrayTypeName) {
      return arraySimpleName((ArrayTypeName) rawType);
    } else if (rawType instanceof ClassName) {
      return ((ClassName) rawType).simpleName();
    } else return String.valueOf(typeName);
  }

  private static String arraySimpleName(final ArrayTypeName arrayTypeName) {
    return "ArrayOf"
        + (arrayTypeName.componentType instanceof ArrayTypeName
            ? arraySimpleName((ArrayTypeName) arrayTypeName.componentType)
            : arrayTypeName.componentType);
  }

  private static TypeName rawType(final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) typeName).rawType;
    }
    return typeName;
  }

  private Iterable<MethodSpec> buildMappingMethods(
          final ConversionServiceAdapterDescriptor descriptor,
          final FieldSpec injectedConversionServiceFieldSpec) {
    return descriptor.getFromToMappings().stream()
        .map(
            sourceTargetPair ->
                toMappingMethodSpec(injectedConversionServiceFieldSpec, sourceTargetPair))
        .collect(toList());
  }

  private MethodSpec toMappingMethodSpec(
          final FieldSpec injectedConversionServiceFieldSpec,
          final Pair<TypeName, TypeName> sourceTargetPair) {
    final ParameterSpec sourceParameterSpec = buildSourceParameterSpec(sourceTargetPair.getLeft());
    return MethodSpec.methodBuilder(
            String.format(
                "map%sTo%s",
                collectionOfNameIfApplicable(sourceTargetPair.getLeft()),
                collectionOfNameIfApplicable(sourceTargetPair.getRight())))
        .addParameter(sourceParameterSpec)
        .addModifiers(PUBLIC)
        .returns(sourceTargetPair.getRight())
        .addStatement(
            String.format(
                "return ($T) $N.convert($N, %s, %s)",
                typeDescriptorFormat(sourceTargetPair.getLeft()),
                typeDescriptorFormat(sourceTargetPair.getRight())),
                allTypeDescriptorArguments(injectedConversionServiceFieldSpec, sourceParameterSpec, sourceTargetPair))
        .build();
  }

  private Object[] allTypeDescriptorArguments(
      final FieldSpec injectedConversionServiceFieldSpec,
      final ParameterSpec sourceParameterSpec,
      final Pair<TypeName, TypeName> sourceTargetPair) {
    return concat(
            concat(
                Stream.of(
                    sourceTargetPair.getRight(),
                    injectedConversionServiceFieldSpec,
                    sourceParameterSpec),
                typeDescriptorArguments(sourceTargetPair.getLeft())),
            typeDescriptorArguments(sourceTargetPair.getRight()))
        .toArray();
  }

  private String typeDescriptorFormat(final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName
            && isCollectionWithGenericParameter((ParameterizedTypeName) typeName)) {
      return String.format(
              "$T.collection($T.class, %s)",
              typeDescriptorFormat(((ParameterizedTypeName) typeName).typeArguments.iterator().next()));
    }
    return "$T.valueOf($T.class)";
  }

  private Stream<Object> typeDescriptorArguments(final TypeName typeName) {
    return typeName instanceof ParameterizedTypeName
            && isCollectionWithGenericParameter((ParameterizedTypeName) typeName)
        ? concat(
            Stream.of(TYPE_DESCRIPTOR_CLASS_NAME, ((ParameterizedTypeName) typeName).rawType),
            typeDescriptorArguments(
                ((ParameterizedTypeName) typeName).typeArguments.iterator().next()))
        : Stream.of(TYPE_DESCRIPTOR_CLASS_NAME, rawType(typeName));
  }

  private static ParameterSpec buildSourceParameterSpec(final TypeName sourceClassName) {
    return ParameterSpec.builder(sourceClassName, "source", FINAL).build();
  }

  private static FieldSpec buildConversionServiceFieldSpec() {
    return FieldSpec.builder(
            CONVERSION_SERVICE_CLASS_NAME, CONVERSION_SERVICE_FIELD_NAME, PRIVATE, FINAL)
        .build();
  }

  private AnnotationSpec buildGeneratedAnnotationSpec() {
    return Optional.ofNullable(baseAnnotationSpecBuilder())
        .map(
            build ->
                build.addMember("value", "$S", ConversionServiceAdapterGenerator.class.getName()))
        .map(build -> build.addMember("date", "$S", ISO_INSTANT.format(ZonedDateTime.now(clock))))
        .map(AnnotationSpec.Builder::build)
        .orElse(null);
  }

  private AnnotationSpec.Builder baseAnnotationSpecBuilder() {
    final AnnotationSpec.Builder builder;
    if (isJava9PlusGeneratedAvailable()) {
      builder = AnnotationSpec.builder(JAVA_9_PLUS_ANNOTATION_GENERATED_CLASS_NAME);
    } else if (isPreJava9GeneratedAvailable()) {
      builder = AnnotationSpec.builder(PRE_JAVA_9_ANNOTATION_GENERATED_CLASS_NAME);
    } else {
      builder = null;
    }
    return builder;
  }

  private boolean isPreJava9GeneratedAvailable() {
    return isTypeAvailable(PRE_JAVA_9_ANNOTATION_GENERATED);
  }

  private boolean isJava9PlusGeneratedAvailable() {
    return isSourceVersionAtLeast9()
            && isTypeAvailable(JAVA_9_PLUS_ANNOTATION_GENERATED);
  }

  private boolean isSourceVersionAtLeast9() {
    return processingEnvironment.get().getSourceVersion().compareTo(RELEASE_8) > 0;
  }

  private boolean isTypeAvailable(final String name) {
    return processingEnvironment.get().getElementUtils().getTypeElement(name) != null;
  }

  public void init(final ProcessingEnvironment processingEnv) {
    if (!this.processingEnvironment.compareAndSet(null, processingEnv)) {
      throw new IllegalStateException("ProcessingEnvironment already set.");
    }
  }
}
