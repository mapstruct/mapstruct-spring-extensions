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
abstract class AbstractConversionServiceBeanNameIndependentGeneratorTest extends AdapterRelatedGeneratorTest {
  @Mock private Elements elements;

  private final String java9PlusGeneratedExpectedContentFileName;
  private final String java9PlusGeneratedNoDateExpectedContentFileName;
  private final String noGeneratedExpectedContentFileName;
  private final AdapterRelatedGenerator underTest;

  protected AbstractConversionServiceBeanNameIndependentGeneratorTest(
          final String java9PlusGeneratedExpectedContentFileName,
          final String java9PlusGeneratedNoDateExpectedContentFileName,
          final String noGeneratedExpectedContentFileName,
          final AdapterRelatedGenerator underTest) {
    this.java9PlusGeneratedExpectedContentFileName = java9PlusGeneratedExpectedContentFileName;
    this.java9PlusGeneratedNoDateExpectedContentFileName =
        java9PlusGeneratedNoDateExpectedContentFileName;
    this.noGeneratedExpectedContentFileName = noGeneratedExpectedContentFileName;
    this.underTest = underTest;
  }

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
        AbstractConversionServiceBeanNameIndependentGeneratorTest.this.shouldGenerateMatchingOutput(
            java9PlusGeneratedExpectedContentFileName, underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        AbstractConversionServiceBeanNameIndependentGeneratorTest.this.shouldGenerateMatchingOutput(
            java9PlusGeneratedNoDateExpectedContentFileName, underTest::writeGeneratedCodeToOutput);
      }
    }

    @Nested
    class NoGenerated {
      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        AbstractConversionServiceBeanNameIndependentGeneratorTest.this.shouldGenerateMatchingOutput(
            noGeneratedExpectedContentFileName, underTest::writeGeneratedCodeToOutput);
      }
    }
  }

  @Nested
  class Init {
    @Test
    void shouldInitWithProcessingEnvironment() {
      AbstractConversionServiceBeanNameIndependentGeneratorTest.this
          .shouldInitWithProcessingEnvironment(
              underTest::init, underTest::getProcessingEnvironment);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCalledRepeatedly() {
      AbstractConversionServiceBeanNameIndependentGeneratorTest.this
          .shouldThrowIllegalStateExceptionWhenCalledRepeatedly(underTest::init);
    }
  }
}
