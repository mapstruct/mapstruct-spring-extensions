package org.mapstruct.extensions.spring.converter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.UnaryOperator.identity;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.assertj.core.api.BDDAssertions.then;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

abstract class AdapterRelatedGeneratorTest extends GeneratorTest {

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
                        new FromToMapping()
                            .source(ClassName.get("test", "Car"))
                            .target(ClassName.get("test", "CarDto"))
                            .adapterMethodName("toDto"),
                        new FromToMapping()
                            .source(
                                ParameterizedTypeName.get(
                                    ClassName.get(List.class), ClassName.get("test", "Car")))
                            .target(
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

}
