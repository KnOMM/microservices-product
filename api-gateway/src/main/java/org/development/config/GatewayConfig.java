package org.development.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter tracingFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.defer(() -> {
                // Propagate trace headers
                propagateTraceHeaders(exchange);
                return Mono.empty();
            }));
        };
    }

    private void propagateTraceHeaders(ServerWebExchange exchange) {
        // Here you can access and propagate the trace headers
        // Example:
        String traceId = exchange.getRequest().getHeaders().getFirst("X-B3-TraceId");
        String spanId = exchange.getRequest().getHeaders().getFirst("X-B3-SpanId");
        // Add additional headers as needed
    }
}
