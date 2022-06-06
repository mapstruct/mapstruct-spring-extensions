package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ConversionServiceAdapterGeneratorTest {
  @Mock private Elements elements;

  private boolean isAtLeastJava9;

  private static final Clock FIXED_CLOCK =
      Clock.fixed(
          ZonedDateTime.of(2020, 3, 29, 15, 21, 34, (int) (236 * Math.pow(10, 6)), ZoneId.of("Z"))
              .toInstant(),
          ZoneId.of("Z"));
  private final ConversionServiceAdapterGenerator generator =
      new ConversionServiceAdapterGenerator(FIXED_CLOCK);

  @Nested
  class Java8Generated {
    @BeforeEach
    void initElements() {
      isAtLeastJava9 = false;
      given(elements.getTypeElement("javax.annotation.Generated"))
          .willReturn(mock(TypeElement.class));
    }

    @Test
    void shouldGenerateMatchingOutput() throws IOException {
      ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
          "ConversionServiceAdapterJava8Generated.java");
    }

    @Test
    void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
      ConversionServiceAdapterGeneratorTest.this
          .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
              "ConversionServiceAdapterCustomBeanJava8Generated.java");
    }
  }

  @Nested
  class Java9PlusGenerated {
    @BeforeEach
    void initElements() {
      isAtLeastJava9 = true;
      given(elements.getTypeElement("javax.annotation.processing.Generated"))
          .willReturn(mock(TypeElement.class));
    }

    @Test
    void shouldGenerateMatchingOutput() throws IOException {
      ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
          "ConversionServiceAdapterJava9PlusGenerated.java");
    }

    @Test
    void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
      ConversionServiceAdapterGeneratorTest.this
          .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
              "ConversionServiceAdapterCustomBeanJava9PlusGenerated.java");
    }
  }

  @Nested
  class NoGenerated {
    @BeforeEach
    void initElements() {
      isAtLeastJava9 = false;
    }

    @Test
    void shouldGenerateMatchingOutput() throws IOException {
      ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
          "ConversionServiceAdapterNoGenerated.java");
    }

    @Test
    void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
      ConversionServiceAdapterGeneratorTest.this
          .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
              "ConversionServiceAdapterCustomBeanNoGenerated.java");
    }
  }

  void shouldGenerateMatchingOutput(final String expectedContentFileName) throws IOException {
    // Given
    final ConversionServiceAdapterDescriptor descriptor =
        new ConversionServiceAdapterDescriptor()
            .adapterClassName(
                ClassName.get(
                    ConversionServiceAdapterGeneratorTest.class.getPackage().getName(),
                    "ConversionServiceAdapter"))
            .fromToMappings(
                singletonList(
                    Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto"))))
            .lazyAnnotatedConversionServiceBean(true)
            .elementUtils(elements)
            .sourceVersionAtLeast9(isAtLeastJava9);
    final StringWriter outputWriter = new StringWriter();

    // When
    generator.writeConversionServiceAdapter(descriptor, outputWriter);

    // Then
    then(outputWriter.toString())
        .isEqualToIgnoringWhitespace(resourceToString('/' + expectedContentFileName, UTF_8));
  }

  void shouldGenerateMatchingOutputWhenUsingCustomConversionService(
      final String expectedContentFileName) throws IOException {
    // Given
    final ConversionServiceAdapterDescriptor descriptor =
        new ConversionServiceAdapterDescriptor()
            .adapterClassName(
                ClassName.get(
                    ConversionServiceAdapterGeneratorTest.class.getPackage().getName(),
                    "ConversionServiceAdapter"))
            .conversionServiceBeanName("myConversionService")
            .fromToMappings(
                singletonList(
                    Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto"))))
            .lazyAnnotatedConversionServiceBean(true)
            .elementUtils(elements)
            .sourceVersionAtLeast9(isAtLeastJava9);
    final StringWriter outputWriter = new StringWriter();

    // When
    generator.writeConversionServiceAdapter(descriptor, outputWriter);

    // Then
    then(outputWriter.toString())
        .isEqualToIgnoringWhitespace(resourceToString('/' + expectedContentFileName, UTF_8));
  }
}
