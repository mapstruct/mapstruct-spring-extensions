package org.mapstruct.extensions.spring.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.sql.Blob;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ConversionServiceAdapterTest {
    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ConversionServiceAdapter conversionServiceAdapter;

    @Test
    void shouldMapViaConversionServiceInGeneratedMethod() {
        // Given
        final Blob blob = mock(Blob.class);

        // When
        conversionServiceAdapter.mapBlobToArrayOfbyte(blob);

        // Then
        then(conversionService).should().convert(blob, byte[].class);
    }
}
