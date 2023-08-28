package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.SourceVersion.RELEASE_9;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class ConversionServiceAdapterGeneratorTest extends GeneratorTest {
  @Mock private Elements elements;

  private boolean isAtLeastJava9;

  private final ConversionServiceAdapterGenerator underTest =
      new ConversionServiceAdapterGenerator(FIXED_CLOCK);

  @Nested
  class DefaultProcessingEnvironment {
    @Mock private ProcessingEnvironment processingEnvironment;

    @BeforeEach
    void initWithProcessingEnvironment() {
      given(processingEnvironment.getElementUtils()).willReturn(elements);
      given(processingEnvironment.getSourceVersion())
          .will(
              (Answer<SourceVersion>)
                  (invocation) -> {
                    if (isAtLeastJava9) {
                      return RELEASE_9;
                    } else {
                      return RELEASE_8;
                    }
                  });
      underTest.init(processingEnvironment);
    }

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
            "ConversionServiceAdapterJava8Generated.java",
            underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldGenerateMatchingOutputWhenUsingCustomConversionService() throws IOException {
        ConversionServiceAdapterGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConversionServiceAdapterCustomBeanJava8Generated.java",
                underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        ConversionServiceAdapterGeneratorTest.this.shouldGenerateMatchingOutput(
            "ConversionServiceAdapterJava8GeneratedNoDate.java",
            underTest::writeGeneratedCodeToOutput);
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
      @BeforeEach
      void initElements() {
        isAtLeastJava9 = false;
      }

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
