package org.mapstruct.extensions.spring.converter;

import javax.annotation.processing.ProcessingEnvironment;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenIllegalStateException;
import static org.mockito.Mockito.mock;

public class GeneratorTest {
  protected static final Clock FIXED_CLOCK =
      Clock.fixed(
          ZonedDateTime.of(2020, 3, 29, 15, 21, 34, (int) (236 * Math.pow(10, 6)), ZoneId.of("Z"))
              .toInstant(),
          ZoneId.of("Z"));

  protected final void shouldInitWithProcessingEnvironment(
      final Consumer<ProcessingEnvironment> initCall,
      final Supplier<ProcessingEnvironment> environmentSupplier) {
    final var processingEnv = mock(ProcessingEnvironment.class);
    initCall.accept(processingEnv);
    then(environmentSupplier.get()).isEqualTo(processingEnv);
  }

  protected final void shouldThrowIllegalStateExceptionWhenCalledRepeatedly(
      final Consumer<ProcessingEnvironment> initCall) {
    final var processingEnv = mock(ProcessingEnvironment.class);
    initCall.accept(processingEnv);
    thenIllegalStateException().isThrownBy(() -> initCall.accept(processingEnv));
  }
}
