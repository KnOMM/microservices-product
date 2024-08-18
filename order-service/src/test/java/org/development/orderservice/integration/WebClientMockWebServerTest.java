package org.development.orderservice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.development.orderservice.dto.InventoryResponse;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Import(WebClientTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class WebClientMockWebServerTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:13-alpine");

    private static MockWebServer mockWebServer;
//    private
@Autowired
WebClient.Builder webClientBuilder;
    //    @Autowired
//    WebClient webClient;
    @Autowired
    ObjectMapper objectMapper;


    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        System.out.println("Mock server started on port: " + mockWebServer.getPort());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = mockWebServer.url("/").toString();
        webClientBuilder.baseUrl(baseUrl);
        WebClient webClient = webClientBuilder.build();
        System.out.println(webClient.toString());
    }

    @Test
    void test() throws JsonProcessingException {
        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        // Set up the expected response from the mock server
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Expected Response"));

        // Debugging output
        System.out.println("Base URL: " + baseUrl);

        // Perform your test
        String response = webClient.get()
                .uri("/api/order")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Assertions.assertEquals("Expected Response", response);
    }
}