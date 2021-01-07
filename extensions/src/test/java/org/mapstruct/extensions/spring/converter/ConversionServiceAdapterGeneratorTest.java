package org.mapstruct.extensions.spring.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.assertj.core.api.BDDAssertions.then;

import com.squareup.javapoet.ClassName;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class ConversionServiceAdapterGeneratorTest {
  private static final Clock FIXED_CLOCK =
      Clock.fixed(
          ZonedDateTime.of(2020, 3, 29, 15, 21, 34, (int) (236 * Math.pow(10, 6)), ZoneId.of("Z"))
              .toInstant(),
          ZoneId.of("Z"));
  private final ConversionServiceAdapterGenerator generator =
      new ConversionServiceAdapterGenerator(FIXED_CLOCK);

  @Test
  void shouldGenerateMatchingOutput() throws IOException {
    // Given
    final ConversionServiceAdapterDescriptor descriptor = new ConversionServiceAdapterDescriptor();
    descriptor.setAdapterClassName(
        ClassName.get(
            ConversionServiceAdapterGeneratorTest.class.getPackage().getName(),
            "ConversionServiceAdapter"));
    descriptor.setFromToMappings(
        singletonList(Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto"))));
    final StringWriter outputWriter = new StringWriter();

    // When
    generator.writeConversionServiceAdapter(descriptor, outputWriter);

    // Then
    then(outputWriter.toString())
        .isEqualToIgnoringWhitespace(resourceToString("/ConversionServiceAdapter.java", UTF_8));
  }

  @Test
  void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
    // Given
    final ConversionServiceAdapterDescriptor descriptor = new ConversionServiceAdapterDescriptor();
    descriptor.setAdapterClassName(
            ClassName.get(
                    ConversionServiceAdapterGeneratorTest.class.getPackage().getName(),
                    "ConversionServiceAdapter"));
    descriptor.setConversionServiceBeanName("myConversionService");
    descriptor.setFromToMappings(
            singletonList(Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto"))));
    final StringWriter outputWriter = new StringWriter();

    // When
    generator.writeConversionServiceAdapter(descriptor, outputWriter);

    // Then
    then(outputWriter.toString())
            .isEqualToIgnoringWhitespace(resourceToString("/ConversionServiceAdapterCustomBean.java", UTF_8));
  }
}
