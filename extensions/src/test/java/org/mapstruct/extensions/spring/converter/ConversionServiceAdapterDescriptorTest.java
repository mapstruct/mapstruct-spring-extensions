package org.mapstruct.extensions.spring.converter;

import static org.assertj.core.api.BDDAssertions.then;

import io.goodforgod.dummymaker.GenFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConversionServiceAdapterDescriptorTest {
  @Nested
  class HasNonDefaultConversionServiceBeanName {
    @Test
    void shouldReturnFalseByDefault() {
      final var descriptor = new ConversionServiceAdapterDescriptor();

      then(descriptor.hasNonDefaultConversionServiceBeanName()).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyString() {
      final var descriptor = new ConversionServiceAdapterDescriptor().conversionServiceBeanName("");

      then(descriptor.hasNonDefaultConversionServiceBeanName()).isFalse();
    }

    @Test
    void shouldReturnTrueForAnyNonEmptyString() {
      final var descriptor =
          new ConversionServiceAdapterDescriptor()
              .conversionServiceBeanName(GenFactory.build().build(String.class));

      then(descriptor.hasNonDefaultConversionServiceBeanName()).isTrue();
    }
  }
}
