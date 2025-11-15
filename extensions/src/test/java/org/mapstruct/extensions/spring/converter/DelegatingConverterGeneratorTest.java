package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.SourceVersion.RELEASE_9;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.converter.AbstractProcessorTest.CAR_DTO_SIMPLE_NAME;
import static org.mapstruct.extensions.spring.converter.AbstractProcessorTest.CAR_SIMPLE_NAME;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.ClassName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelegatingConverterGeneratorTest extends GeneratorTest {
  private static final String MAPPER_PACKAGE_NAME = "org.mapstruct.extensions.spring.converter";
  @Mock private Elements elements;

  private final DelegatingConverterGenerator underTest =
      new DelegatingConverterGenerator(FIXED_CLOCK);

  @Nested
  class DefaultProcessingEnvironment {
    @Mock private ProcessingEnvironment processingEnvironment;

    @BeforeEach
    void initWithProcessingEnvironment() {
      given(processingEnvironment.getElementUtils()).willReturn(elements);
      given(processingEnvironment.getSourceVersion()).willReturn(RELEASE_9);
      underTest.init(processingEnvironment);
    }

    @Nested
    class Java9PlusGenerated {
      @BeforeEach
      void initElements() {
        given(elements.getTypeElement("javax.annotation.processing.Generated"))
            .willReturn(mock(TypeElement.class));
      }

      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        DelegatingConverterGeneratorTest.this.shouldGenerateMatchingOutput(
            "DelegatingConverterJava9PlusGenerated.java");
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        DelegatingConverterGeneratorTest.this.shouldGenerateMatchingOutput(
            "DelegatingConverterJava9PlusGeneratedNoDate.java");
      }
    }

    @Nested
    class NoGenerated {
      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        DelegatingConverterGeneratorTest.this.shouldGenerateMatchingOutput(
            "DelegatingConverterNoGenerated.java");
      }
    }
  }

  @Nested
  class Init {
    @Test
    void shouldInitWithProcessingEnvironment() {
      DelegatingConverterGeneratorTest.this.shouldInitWithProcessingEnvironment(
          underTest::init, underTest::getProcessingEnvironment);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCalledRepeatedly() {
      DelegatingConverterGeneratorTest.this.shouldThrowIllegalStateExceptionWhenCalledRepeatedly(
          underTest::init);
    }
  }

  void shouldGenerateMatchingOutput(final String expectedContentFileName) throws IOException {
    final var descriptor = mock(DelegatingConverterDescriptor.class);
    given(descriptor.getConverterClassName())
        .willReturn(ClassName.get(MAPPER_PACKAGE_NAME, "CarDtoToCarConverter"));
    given(descriptor.getFromToMapping())
        .willReturn(
            new FromToMapping()
                .source(ClassName.get(MAPPER_PACKAGE_NAME, CAR_DTO_SIMPLE_NAME))
                .target(ClassName.get(MAPPER_PACKAGE_NAME, CAR_SIMPLE_NAME)));
    given(descriptor.getOriginalMapperClassName())
        .willReturn(ClassName.get(MAPPER_PACKAGE_NAME, "CarMapper"));
    given(descriptor.getOriginalMapperMethodName()).willReturn("convertInverse");
    final var outputWriter = new StringWriter();

    underTest.writeGeneratedCodeToOutput(descriptor, outputWriter);

    then(outputWriter.toString())
        .isEqualToIgnoringWhitespace(resourceToString('/' + expectedContentFileName, UTF_8));
  }
}
