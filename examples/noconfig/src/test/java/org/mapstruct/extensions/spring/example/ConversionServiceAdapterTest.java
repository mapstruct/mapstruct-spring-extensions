package org.mapstruct.extensions.spring.example;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mapstruct.extensions.spring.example.WheelPosition.RIGHT_FRONT;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;
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
    final var wheel = new Wheel();
    wheel.setPosition(RIGHT_FRONT);
    wheel.setDiameter(16);
    final var expectedDto = new WheelDto();
    expectedDto.setDiameter(16);
    expectedDto.setPosition("RIGHT_FRONT");
    given(
            conversionService.convert(
                wheel, TypeDescriptor.valueOf(Wheel.class), TypeDescriptor.valueOf(WheelDto.class)))
        .willReturn(expectedDto);

    final var actualDto = conversionServiceAdapter.toDto(wheel);

    then(actualDto).isSameAs(expectedDto);
  }
}
