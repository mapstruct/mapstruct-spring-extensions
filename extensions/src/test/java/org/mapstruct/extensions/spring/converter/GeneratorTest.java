package org.mapstruct.extensions.spring.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.UnaryOperator.identity;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalStateException;
import static org.mockito.Mockito.mock;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.tuple.Pair;

abstract class GeneratorTest {
  protected static final Clock FIXED_CLOCK =
      Clock.fixed(
          ZonedDateTime.of(2020, 3, 29, 15, 21, 34, (int) (236 * Math.pow(10, 6)), ZoneId.of("Z"))
              .toInstant(),
          ZoneId.of("Z"));

  protected final void shouldGenerateMatchingOutput(
      final String expectedContentFileName,
      final UnaryOperator<ConversionServiceAdapterDescriptor> descriptorDecorator,
      final BiConsumer<ConversionServiceAdapterDescriptor, Writer> generatorCall)
      throws IOException {
    final ConversionServiceAdapterDescriptor descriptor =
        descriptorDecorator.apply(
            new ConversionServiceAdapterDescriptor()
                .adapterClassName(
                    ClassName.get(
                        ConversionServiceAdapterGeneratorTest.class.getPackage().getName(),
                        "ConversionServiceAdapter"))
                .fromToMappings(
                    List.of(
                        Pair.of(ClassName.get("test", "Car"), ClassName.get("test", "CarDto")),
                        Pair.of(
                            ParameterizedTypeName.get(
                                ClassName.get(List.class), ClassName.get("test", "Car")),
                            ParameterizedTypeName.get(
                                ClassName.get(List.class), ClassName.get("test", "CarDto")))))
                .lazyAnnotatedConversionServiceBean(true));
    final StringWriter outputWriter = new StringWriter();

    // When
    generatorCall.accept(descriptor, outputWriter);

    // Then
    then(outputWriter.toString())
        .isEqualToIgnoringWhitespace(resourceToString('/' + expectedContentFileName, UTF_8));
  }

  protected final void shouldGenerateMatchingOutput(
      final String expectedContentFileName,
      final BiConsumer<ConversionServiceAdapterDescriptor, Writer> generatorCall)
      throws IOException {
    shouldGenerateMatchingOutput(expectedContentFileName, identity(), generatorCall);
  }

  protected final void shouldGenerateMatchingOutputWhenUsingCustomConversionService(
      final String expectedContentFileName,
      final BiConsumer<ConversionServiceAdapterDescriptor, Writer> generatorCall)
      throws IOException {
    shouldGenerateMatchingOutput(
        expectedContentFileName,
        descriptor -> descriptor.conversionServiceBeanName("myConversionService"),
        generatorCall);
  }

  protected final void shouldInitWithProcessingEnvironment(
      final Consumer<ProcessingEnvironment> initCall,
      final Supplier<ProcessingEnvironment> environmentSupplier) {
    final var processingEnv = mock(ProcessingEnvironment.class);
    initCall.accept(processingEnv);
    then(environmentSupplier.get()).isEqualTo(processingEnv);
  }

  protected final void shouldThrowIllegalStateExceptionWhenCalledRepeatedly(
      final Consumer<ProcessingEnvironment> initCall) {
    final var processingEnv = mock(ProcessingEnvironment.class);
    initCall.accept(processingEnv);
    thenIllegalStateException().isThrownBy(() -> initCall.accept(processingEnv));
  }
}
