package org.development.productservice.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.development.productservice.utils.TracingMongoObservationCommandListener;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.ContextProviderFactory;

@Configuration
public class MongoConfig {

    private final Tracer tracer;

    public MongoConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Bean
    MongoClientSettingsBuilderCustomizer mongoMetricsSynchronousContextProvider(ObservationRegistry registry) {
        return (clientSettingsBuilder) -> clientSettingsBuilder.contextProvider(ContextProviderFactory.create(registry))
                .addCommandListener(new TracingMongoObservationCommandListener(registry, tracer));
    }
}

