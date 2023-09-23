package org.mapstruct.extensions.spring.converter;

import static org.mapstruct.extensions.spring.converter.TypeNameUtils.collectionOfNameIfApplicable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;

public class DelegatingConverterDescriptor {
  private final ClassName converterClassName;
  private final FromToMapping fromToMapping;
  private final ClassName originalMapperClassName;
  private final String originalMapperMethodName;

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
}
