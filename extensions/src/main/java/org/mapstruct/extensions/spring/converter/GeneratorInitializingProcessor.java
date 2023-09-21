package org.mapstruct.extensions.spring.converter;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

import com.squareup.javapoet.ClassName;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

public abstract class GeneratorInitializingProcessor extends AbstractProcessor {
  private final List<Generator> generators;

  protected GeneratorInitializingProcessor(final Generator... generators) {
    this.generators = List.of(generators);
  }

  @Override
  public synchronized void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    generators.forEach(generator -> generator.init(processingEnv));
  }

  @Override
  public final SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  protected final Writer openSourceFile(final ClassName className) throws IOException {
    return processingEnv.getFiler().createSourceFile(className.canonicalName()).openWriter();
  }

  protected final Writer openResourceFile(
      final ClassName className, final Element... originatingElements) throws IOException {
    return processingEnv
        .getFiler()
        .createResource(
            SOURCE_OUTPUT,
            className.packageName(),
            className.simpleName() + ".java",
            originatingElements)
        .openWriter();
  }
}
