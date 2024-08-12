package org.development.productservice.utils;

import com.mongodb.event.CommandStartedEvent;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;

@Slf4j
public class TracingMongoObservationCommandListener extends MongoObservationCommandListener {

    private final Tracer tracer;

    public TracingMongoObservationCommandListener(ObservationRegistry registry, Tracer tracer) {
        super(registry);
        this.tracer = tracer;
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        super.commandStarted(event);
        Span currentSpan = tracer.nextSpan().name("query info");

        try (Tracer.SpanInScope ignored = this.tracer.withSpan(currentSpan.start())) {
            currentSpan.tag("mongodb.command", event.getCommandName());
            currentSpan.tag("mongodb.query", event.getCommand().toJson());
            // You can log an event on a span - an event is an annotated timestamp
//            currentSpan.event("taxCalculated");
        } finally {
            currentSpan.end();
        }
        log.info("Mongo Command Executed");
    }
}

