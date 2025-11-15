package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static javax.lang.model.SourceVersion.RELEASE_9;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversionServiceAdapterGeneratorTest extends AdapterRelatedGeneratorTest {
  @Mock private Elements elements;

  private final ConversionServiceAdapterGenerator underTest =
      new ConversionServiceAdapterGenerator(FIXED_CLOCK);

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
        ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
            "ConversionServiceAdapterJava9PlusGenerated.java",
            underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
        ConversionServiceAdapterGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConversionServiceAdapterCustomBeanJava9PlusGenerated.java",
                underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
            "ConversionServiceAdapterJava9PlusGeneratedNoDate.java",
            underTest::writeGeneratedCodeToOutput);
      }
    }

    @Nested
    class NoGenerated {
      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
            "ConversionServiceAdapterNoGenerated.java", underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
        ConversionServiceAdapterGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConversionServiceAdapterCustomBeanNoGenerated.java",
                underTest::writeGeneratedCodeToOutput);
      }
    }
  }

  @Nested
  class Init {
    @Test
    void shouldInitWithProcessingEnvironment() {
      ConversionServiceAdapterGeneratorTest.this.shouldInitWithProcessingEnvironment(
          underTest::init, underTest::getProcessingEnvironment);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCalledRepeatedly() {
      ConversionServiceAdapterGeneratorTest.this
          .shouldThrowIllegalStateExceptionWhenCalledRepeatedly(underTest::init);
    }
  }
}
