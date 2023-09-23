package org.mapstruct.extensions.spring.converter;

import javax.lang.model.element.Name;

public class ModelElementUtils {
    private ModelElementUtils(){}

    public static boolean hasName(final Name name, final String comparisonName) {
      return name.contentEquals(comparisonName);
    }
}
