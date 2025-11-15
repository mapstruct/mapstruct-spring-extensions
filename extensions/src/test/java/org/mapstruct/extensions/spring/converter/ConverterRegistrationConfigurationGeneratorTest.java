package org.mapstruct.extensions.spring.converter;

import static java.lang.Boolean.TRUE;
import static javax.lang.model.SourceVersion.RELEASE_9;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
class ConverterRegistrationConfigurationGeneratorTest extends AdapterRelatedGeneratorTest {
  @Mock private Elements elements;

  private final ConverterRegistrationConfigurationGenerator underTest =
      new ConverterRegistrationConfigurationGenerator(FIXED_CLOCK);

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
        ConverterRegistrationConfigurationGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConverterRegistrationConfigurationJava9PlusGenerated.java",
                underTest::writeGeneratedCodeToOutput);
      }

      @Test
      void shouldSuppressDateGenerationWhenProcessingEnvironmentHasSuppressionSetToTrue()
          throws IOException {
        given(processingEnvironment.getOptions())
            .willReturn(Map.of("mapstruct.suppressGeneratorTimestamp", String.valueOf(TRUE)));
        ConverterRegistrationConfigurationGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConverterRegistrationConfigurationJava9PlusGeneratedNoDate.java",
                underTest::writeGeneratedCodeToOutput);
      }
    }

    @Nested
    class NoGenerated {
      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        ConverterRegistrationConfigurationGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                "ConverterRegistrationConfigurationNoGenerated.java",
                underTest::writeGeneratedCodeToOutput);
      }
    }

    @Nested
    class NoGeneratedJakartaAnnotation {
      @BeforeEach
      void initElements() {
        when(elements.getTypeElement(anyString())).thenReturn(null);
      }

      @Test
      void shouldGenerateMatchingOutput() throws IOException {
        ConverterRegistrationConfigurationGeneratorTest.this
            .shouldGenerateMatchingOutputWhenUsingCustomConversionService(
                    "ConverterRegistrationConfigurationNoGeneratedJakartaAnnotation.java",
                    underTest::writeGeneratedCodeToOutput);
      }
    }
  }

  @Nested
  class Init {
    @Test
    void shouldInitWithProcessingEnvironment() {
      ConverterRegistrationConfigurationGeneratorTest.this.shouldInitWithProcessingEnvironment(
          underTest::init, underTest::getProcessingEnvironment);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCalledRepeatedly() {
      ConverterRegistrationConfigurationGeneratorTest.this
          .shouldThrowIllegalStateExceptionWhenCalledRepeatedly(underTest::init);
    }
  }
}
