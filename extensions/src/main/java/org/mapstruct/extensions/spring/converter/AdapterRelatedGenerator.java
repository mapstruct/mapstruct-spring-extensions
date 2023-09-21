package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.TypeSpec;

import java.io.Writer;
import java.time.Clock;

public abstract class AdapterRelatedGenerator extends Generator {
    protected AdapterRelatedGenerator(Clock clock) {
        super(clock);
    }

    public final void writeGeneratedCodeToOutput(
        final ConversionServiceAdapterDescriptor descriptor, final Writer out) {
      writeGeneratedCodeToOutput(
          () -> descriptor.getAdapterClassName().packageName(),
          () -> createMainTypeSpec(descriptor),
          out);
    }

    protected abstract TypeSpec createMainTypeSpec(ConversionServiceAdapterDescriptor descriptor);
}
