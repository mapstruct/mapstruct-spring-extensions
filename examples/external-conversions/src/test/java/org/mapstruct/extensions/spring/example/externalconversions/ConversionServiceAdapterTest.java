package org.mapstruct.extensions.spring.example.externalconversions;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Blob;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

@ExtendWith(MockitoExtension.class)
class ConversionServiceAdapterTest {
  @Mock private ConversionService conversionService;

  @InjectMocks private ConversionServiceAdapter conversionServiceAdapter;

  @Test
  void shouldCallConversionServiceFromGeneratedMethodWithOverriddenMethodName() {
    final var blob = mock(Blob.class);
    final var expectedBytes = "Hello World!".getBytes(UTF_8);
    given(
            conversionService.convert(
                blob, TypeDescriptor.valueOf(Blob.class), TypeDescriptor.valueOf(byte[].class)))
        .willReturn(expectedBytes);

    final var actualBytes = conversionServiceAdapter.blob2Bytes(blob);

    then(actualBytes).isSameAs(expectedBytes);
  }
}
