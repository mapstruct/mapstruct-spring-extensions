package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.WARNING;

import com.squareup.javapoet.*;
import java.time.Clock;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ConversionServiceAdapterGenerator extends Generator {
  private static final ClassName CONVERSION_SERVICE_CLASS_NAME =
      ClassName.get("org.springframework.core.convert", "ConversionService");
  private static final String CONVERSION_SERVICE_FIELD_NAME = "conversionService";
  private static final ClassName QUALIFIER_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.beans.factory.annotation", "Qualifier");
  private static final ClassName LAZY_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.context.annotation", "Lazy");
  private static final ClassName TYPE_DESCRIPTOR_CLASS_NAME =
      ClassName.get("org.springframework.core.convert", "TypeDescriptor");
  private static final ClassName COMPONENT_ANNOTATION_CLASS_NAME =
      ClassName.get("org.springframework.stereotype", "Component");

  public ConversionServiceAdapterGenerator(final Clock clock) {
    super(clock);
  }

  @Override
  protected TypeSpec createMainTypeSpec(
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
    if (descriptor.hasNonDefaultConversionServiceBeanName()) {
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

  private boolean isCollectionWithGenericParameter(
      final ParameterizedTypeName parameterizedTypeName) {
    return parameterizedTypeName.typeArguments != null
        && !parameterizedTypeName.typeArguments.isEmpty()
        && isCollection(parameterizedTypeName);
  }

  private boolean isCollection(final ParameterizedTypeName parameterizedTypeName) {
    try {
      return Collection.class.isAssignableFrom(
          Class.forName(parameterizedTypeName.rawType.canonicalName()));
    } catch (ClassNotFoundException e) {
      getProcessingEnvironment()
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
            fromToMapping ->
                toMappingMethodSpec(injectedConversionServiceFieldSpec, fromToMapping))
        .collect(toList());
  }

  private MethodSpec toMappingMethodSpec(
      final FieldSpec injectedConversionServiceFieldSpec,
      final FromToMapping fromToMapping) {
    final ParameterSpec sourceParameterSpec = buildSourceParameterSpec(fromToMapping.getSource());
    return MethodSpec.methodBuilder(
            fromToMapping
                .getAdapterMethodName()
                .orElse(
                    String.format(
                        "map%sTo%s",
                        collectionOfNameIfApplicable(fromToMapping.getSource()),
                        collectionOfNameIfApplicable(fromToMapping.getTarget()))))
        .addParameter(sourceParameterSpec)
        .addModifiers(PUBLIC)
        .returns(fromToMapping.getTarget())
        .addStatement(
            String.format(
                "return ($T) $N.convert($N, %s, %s)",
                typeDescriptorFormat(fromToMapping.getSource()),
                typeDescriptorFormat(fromToMapping.getTarget())),
            allTypeDescriptorArguments(
                injectedConversionServiceFieldSpec, sourceParameterSpec, fromToMapping))
        .build();
  }

  private Object[] allTypeDescriptorArguments(
      final FieldSpec injectedConversionServiceFieldSpec,
      final ParameterSpec sourceParameterSpec,
      final FromToMapping fromToMapping) {
    return concat(
            concat(
                Stream.of(
                    fromToMapping.getTarget(),
                    injectedConversionServiceFieldSpec,
                    sourceParameterSpec),
                typeDescriptorArguments(fromToMapping.getSource())),
            typeDescriptorArguments(fromToMapping.getTarget()))
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

}
