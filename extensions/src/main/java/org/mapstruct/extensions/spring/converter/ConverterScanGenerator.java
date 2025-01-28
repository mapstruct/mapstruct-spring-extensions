package org.mapstruct.extensions.spring.converter;

import static com.squareup.javapoet.WildcardTypeName.subtypeOf;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.squareup.javapoet.*;
import java.lang.annotation.*;
import java.time.Clock;
import java.util.Optional;

public class ConverterScanGenerator extends AdapterRelatedGenerator {

  private static final ClassName COMPONENT_SCAN_CLASS_NAME =
      ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ComponentScan");
  private static final ClassName IMPORT_CLASS_NAME =
      ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "Import");
  private static final AnnotationSpec REPEATABLE_ANNOTATION_SPEC =
      AnnotationSpec.builder(Repeatable.class)
          .addMember("value", "$L", "ConverterScans.class")
          .build();
  private static final ClassName BEAN_NAME_GENERATOR_CLASS_NAME =
      ClassName.get("org.springframework.beans.factory.support", "BeanNameGenerator");

  public ConverterScanGenerator(final Clock clock) {
    super(clock);
  }

  @Override
  protected JavaFile.Builder modifyDefaultFileBuilder(final JavaFile.Builder javaFileBuilder) {
    return javaFileBuilder
        .addStaticImport(ElementType.class, "TYPE")
        .addStaticImport(RetentionPolicy.class, "RUNTIME")
        .addStaticImport(
            ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ScopedProxyMode"), "DEFAULT");
  }

  @Override
  protected TypeSpec createMainTypeSpec(final ConversionServiceAdapterDescriptor descriptor) {
    final var converterScanClassTypeSpecBuilder =
        TypeSpec.annotationBuilder(descriptor.getConverterScanClassName()).addModifiers(PUBLIC);
    final var importAnnotationSpec =
            AnnotationSpec.builder(IMPORT_CLASS_NAME)
                    .addMember("value", "$L", descriptor.getConverterRegistrationConfigurationClassName() + ".class")
                    .build();
    Optional.ofNullable(buildGeneratedAnnotationSpec())
        .ifPresent(converterScanClassTypeSpecBuilder::addAnnotation);
    converterScanClassTypeSpecBuilder
        .addAnnotation(COMPONENT_SCAN_CLASS_NAME)
        .addAnnotation(TARGET_TYPE_ANNOTATION_SPEC)
        .addAnnotation(importAnnotationSpec)
        .addAnnotation(Documented.class)
        .addAnnotation(RETENTION_RUNTIME_ANNOTATION_SPEC)
        .addAnnotation(REPEATABLE_ANNOTATION_SPEC)
        .addMethod(aliasArrayMethodSpec("value", ClassName.get(String.class)))
        .addMethod(aliasArrayMethodSpec("basePackages", ClassName.get(String.class)))
        .addMethod(
            aliasArrayMethodSpec(
                "basePackageClasses",
                ParameterizedTypeName.get(ClassName.get(Class.class), subtypeOf(Object.class))))
        .addMethod(
            aliasMethodSpec(
                "nameGenerator",
                ParameterizedTypeName.get(
                    ClassName.get(Class.class), subtypeOf(BEAN_NAME_GENERATOR_CLASS_NAME)),
                CodeBlock.builder().add("$T.class", BEAN_NAME_GENERATOR_CLASS_NAME).build()))
        .addMethod(
            aliasMethodSpec(
                "scopeResolver",
                ParameterizedTypeName.get(
                    ClassName.get(Class.class),
                    subtypeOf(
                        ClassName.get(
                            SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ScopeMetadataResolver"))),
                CodeBlock.builder()
                    .add(
                        "$T.class",
                        ClassName.get(
                            SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME,
                            "AnnotationScopeMetadataResolver"))
                    .build()))
        .addMethod(
            aliasMethodSpec(
                "scopedProxy",
                ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ScopedProxyMode"),
                CodeBlock.builder().add("$L", "DEFAULT").build()))
        .addMethod(
            aliasMethodSpec(
                "useDefaultFilters",
                TypeName.BOOLEAN,
                CodeBlock.builder().add("$L", "true").build()))
        .addMethod(
            aliasArrayMethodSpec(
                "includeFilters",
                ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ComponentScan", "Filter")))
        .addMethod(
            aliasArrayMethodSpec(
                "excludeFilters",
                ClassName.get(SPRING_CONTEXT_ANNOTATION_PACKAGE_NAME, "ComponentScan", "Filter")))
        .addMethod(
            aliasMethodSpec(
                "lazyInit", TypeName.BOOLEAN, CodeBlock.builder().add("$L", "false").build()));
    return converterScanClassTypeSpecBuilder.build();
  }

  private static MethodSpec aliasMethodSpec(
      final String attributeName, final TypeName returnType, final CodeBlock defaultValue) {
    return MethodSpec.methodBuilder(attributeName)
        .addAnnotation(aliasAnnotationSpec(attributeName))
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(returnType)
        .defaultValue(defaultValue)
        .build();
  }

  private static MethodSpec aliasArrayMethodSpec(
      final String attributeName, final TypeName componentType) {
    return aliasMethodSpec(
        attributeName,
        ArrayTypeName.of(componentType),
        CodeBlock.builder().add("$L", "{}").build());
  }

  private static AnnotationSpec aliasAnnotationSpec(final String attributeName) {
    return AnnotationSpec.builder(ClassName.get("org.springframework.core.annotation", "AliasFor"))
        .addMember("annotation", "$T.class", COMPONENT_SCAN_CLASS_NAME)
        .addMember("attribute", "$S", attributeName)
        .build();
  }
}
