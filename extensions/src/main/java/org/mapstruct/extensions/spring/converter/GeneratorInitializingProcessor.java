package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.ClassName;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

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

    protected final Writer openFile(final ClassName className) throws IOException {
      return processingEnv.getFiler().createSourceFile(className.canonicalName()).openWriter();
    }
}
