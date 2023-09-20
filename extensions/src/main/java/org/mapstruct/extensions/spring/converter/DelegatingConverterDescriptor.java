package org.mapstruct.extensions.spring.converter;

import static com.squareup.javapoet.TypeName.VOID;
import static org.mapstruct.extensions.spring.converter.ModelElementUtils.hasName;
import static org.mapstruct.extensions.spring.converter.TypeNameUtils.collectionOfNameIfApplicable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.mapstruct.Mapper;

public class DelegatingConverterDescriptor {
  private static final String DEFAULT_COMPONENT_MODEL = "default";
  private static final TypeName DEFAULT_CONFIG_TYPE_NAME = VOID;
  private final ClassName converterClassName;
  private final FromToMapping fromToMapping;
  private final ClassName originalMapperClassName;
  private final String originalMapperMethodName;
  private final String componentModel;
  private final TypeName configTypeName;

  public DelegatingConverterDescriptor(
      final ExecutableElement annotatedMethod, final ProcessingEnvironment processingEnv) {
    final var parameterList = annotatedMethod.getParameters();
    final var returnType = annotatedMethod.getReturnType();
    if (parameterList.size() != 1 || TypeKind.VOID.equals(returnType.getKind())) {
      final var errorMessage =
          "Can only generate a delegating converter for methods with exactly one parameter and non-void return type.";
      processingEnv
          .getMessager()
          .printMessage(Diagnostic.Kind.ERROR, errorMessage, annotatedMethod);
      throw new IllegalArgumentException(errorMessage);
    }
    fromToMapping =
        new FromToMapping()
            .source(TypeName.get(parameterList.iterator().next().asType()))
            .target(TypeName.get(returnType));

    originalMapperClassName = ClassName.get((TypeElement) annotatedMethod.getEnclosingElement());
    converterClassName =
        ClassName.get(
            originalMapperClassName.packageName(),
            String.format(
                "%sTo%sConverter",
                collectionOfNameIfApplicable(processingEnv, fromToMapping.getSource()),
                collectionOfNameIfApplicable(processingEnv, fromToMapping.getTarget())));
    originalMapperMethodName = annotatedMethod.getSimpleName().toString();
    final var mapperAnnotationMirrorElementValues =
        annotatedMethod.getEnclosingElement().getAnnotationMirrors().stream()
            .filter(
                annotationMirror ->
                    processingEnv
                        .getElementUtils()
                        .getTypeElement(Mapper.class.getName())
                        .asType()
                        .equals(annotationMirror.getAnnotationType().asElement().asType()))
            .findFirst()
            .map(AnnotationMirror::getElementValues);
    if (mapperAnnotationMirrorElementValues.isPresent()) {
      configTypeName =
          mapperAnnotationMirrorElementValues.get().entrySet().stream()
              .filter(entry -> hasName(entry.getKey().getSimpleName(), "config"))
              .findFirst()
              .map(Map.Entry::getValue)
              .map(AnnotationValue::getValue)
              .map(TypeMirror.class::cast)
              .map(ClassName::get)
              .map(TypeName.class::cast)
              .orElse(DEFAULT_CONFIG_TYPE_NAME);
      componentModel =
          mapperAnnotationMirrorElementValues.get().entrySet().stream()
              .filter(entry -> hasName(entry.getKey().getSimpleName(), "componentModel"))
              .findFirst()
              .map(Map.Entry::getValue)
              .map(AnnotationValue::getValue)
              .map(String.class::cast)
              .orElse(DEFAULT_COMPONENT_MODEL);
    } else {
      configTypeName = DEFAULT_CONFIG_TYPE_NAME;
      componentModel = DEFAULT_COMPONENT_MODEL;
    }
  }

  public ClassName getConverterClassName() {
    return converterClassName;
  }

  public FromToMapping getFromToMapping() {
    return fromToMapping;
  }

  public ClassName getOriginalMapperClassName() {
    return originalMapperClassName;
  }

  public String getOriginalMapperMethodName() {
    return originalMapperMethodName;
  }

  public Optional<String> getComponentModel() {
    return DEFAULT_COMPONENT_MODEL.equals(componentModel)
        ? Optional.empty()
        : Optional.of(componentModel);
  }

  public Optional<TypeName> getConfigTypeName() {
    return DEFAULT_CONFIG_TYPE_NAME.equals(configTypeName)
        ? Optional.empty()
        : Optional.of(configTypeName);
  }
}
