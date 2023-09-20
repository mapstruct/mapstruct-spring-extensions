package org.mapstruct.extensions.spring.converter;

import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.IOException;
import java.io.Writer;
import java.time.Clock;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes(DelegatingConverterProcessor.DELEGATING_CONVERTER)
public class DelegatingConverterProcessor extends GeneratorInitializingProcessor {

  protected static final String DELEGATING_CONVERTER =
      "org.mapstruct.extensions.spring.DelegatingConverter";

  private final DelegatingConverterGenerator delegatingConverterGenerator;

  public DelegatingConverterProcessor() {
    this(Clock.systemUTC());
  }

  DelegatingConverterProcessor(final Clock clock) {
    this(new DelegatingConverterGenerator(clock));
  }

  DelegatingConverterProcessor(final DelegatingConverterGenerator delegatingConverterGenerator) {
    super(delegatingConverterGenerator);
    this.delegatingConverterGenerator = delegatingConverterGenerator;
  }

  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    annotations.stream()
        .filter(DelegatingConverterProcessor::isDelegatingConverterAnnotation)
        .map(roundEnv::getElementsAnnotatedWith)
        .flatMap(Set::stream)
        .map(ExecutableElement.class::cast)
        .forEach(this::writeDelegatingConverterFile);
    return false;
  }

  private void writeDelegatingConverterFile(final ExecutableElement annotatedMethod) {
    final var descriptor = new DelegatingConverterDescriptor(annotatedMethod, processingEnv);
    try (final Writer outputWriter = openFile(descriptor.getConverterClassName())) {
      delegatingConverterGenerator.writeGeneratedCodeToOutput(descriptor, outputWriter);
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              ERROR,
              String.format(
                  "Error while opening %s output file: %s",
                  descriptor.getConverterClassName().simpleName(), e.getMessage()));
    }
  }

  private static boolean isDelegatingConverterAnnotation(final TypeElement annotation) {
    return DELEGATING_CONVERTER.contentEquals(annotation.getQualifiedName());
  }
}
