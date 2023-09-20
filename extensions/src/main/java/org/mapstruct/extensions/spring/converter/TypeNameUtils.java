package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Collection;

import static javax.tools.Diagnostic.Kind.WARNING;

public class TypeNameUtils {
  private TypeNameUtils() {}

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

  public static TypeName rawType(final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) typeName).rawType;
    }
    return typeName;
  }

  private static String collectionOfName(
      final ProcessingEnvironment processingEnvironment,
      final ParameterizedTypeName parameterizedTypeName) {
    if (isCollectionWithGenericParameter(processingEnvironment, parameterizedTypeName)) {
      return simpleName(parameterizedTypeName)
          + "Of"
          + collectionOfNameIfApplicable(
              processingEnvironment, parameterizedTypeName.typeArguments.iterator().next());
    }

    return simpleName(parameterizedTypeName);
  }

  public static boolean isCollectionWithGenericParameter(
          final ProcessingEnvironment processingEnvironment,
          final ParameterizedTypeName parameterizedTypeName) {
    return parameterizedTypeName.typeArguments != null
        && !parameterizedTypeName.typeArguments.isEmpty()
        && isCollection(processingEnvironment, parameterizedTypeName);
  }

  private static boolean isCollection(
      final ProcessingEnvironment processingEnvironment,
      final ParameterizedTypeName parameterizedTypeName) {
    try {
      return Collection.class.isAssignableFrom(
          Class.forName(parameterizedTypeName.rawType.canonicalName()));
    } catch (ClassNotFoundException e) {
      processingEnvironment
          .getMessager()
          .printMessage(
              WARNING,
              "Caught ClassNotFoundException when trying to resolve parameterized type: "
                  + e.getMessage());
      return false;
    }
  }

  public static String collectionOfNameIfApplicable(
      final ProcessingEnvironment processingEnvironment, final TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName) {
      return collectionOfName(processingEnvironment, (ParameterizedTypeName) typeName);
    }
    return simpleName(typeName);
  }
}
