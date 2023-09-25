package org.mapstruct.extensions.spring.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.lang.model.SourceVersion.RELEASE_9;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
abstract class AbstractConversionServiceBeanNameIndependentGeneratorTest extends AdapterRelatedGeneratorTest {
  @Mock private Elements elements;

  private boolean isAtLeastJava9;

  private final String java8GeneratedExpectedContentFilename;
  private final String java8GeneratedNoDateExpectedContentFileName;
  private final String java9PlusGeneratedExpectedContentFileName;
  private final String java9PlusGeneratedNoDateExpectedContentFileName;
  private final String noGeneratedExpectedContentFileName;
  private final AdapterRelatedGenerator underTest;

  protected AbstractConversionServiceBeanNameIndependentGeneratorTest(
          final String java8GeneratedExpectedContentFilename,
          final String java8GeneratedNoDateExpectedContentFileName,
          final String java9PlusGeneratedExpectedContentFileName,
          final String java9PlusGeneratedNoDateExpectedContentFileName,
          final String noGeneratedExpectedContentFileName,
          final AdapterRelatedGenerator underTest) {
    this.java8GeneratedExpectedContentFilename = java8GeneratedExpectedContentFilename;
    this.java8GeneratedNoDateExpectedContentFileName = java8GeneratedNoDateExpectedContentFileName;
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
        AbstractConversionServiceBeanNameIndependentGeneratorTest.this.shouldGenerateMatchingOutput(
            java8GeneratedExpectedContentFilename, underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        AbstractConversionServiceBeanNameIndependentGeneratorTest.this.shouldGenerateMatchingOutput(
            java8GeneratedNoDateExpectedContentFileName, underTest::writeGeneratedCodeToOutput);
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
      @BeforeEach
      void initElements() {
        isAtLeastJava9 = false;
      }

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
