package org.mapstruct.extensions.spring.example.boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;


public class ContextLoadsWithLazyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Start.class));

    @Test
    public void shouldLoadContext() {
        this.contextRunner.run(context -> {
           assertThat(context).hasBean("conversionServiceAdapter");
        });
    }
}
