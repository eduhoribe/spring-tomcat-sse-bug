package com.github.eduhoribe;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TestSseController {
	private static final Logger logger = LoggerFactory.getLogger(TestSseController.class);

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private SseController sseController;

	@Test
	void testSsePayloadLeakage() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(2);
		executor.submit(() -> createSseConnectionsIndefinitely(executor));
		executor.submit(() -> getSampleJsonIndefinitely(executor));

		var ranFor5MinutesNonstop = !executor.awaitTermination(5, TimeUnit.MINUTES);
		if (!ranFor5MinutesNonstop) {
			// Wait for loggers to finish writing
			Thread.sleep(5_000);
		}
		assertThat(ranFor5MinutesNonstop).isTrue();
	}

	private void createSseConnectionsIndefinitely(ExecutorService executor) {
		while (!executor.isShutdown()) {
			try {
				// Using `curl` instead of RestTemplate since:
				//  - setting a timeout per request was troublesome
				//  - the response here is not relevant
				Runtime.getRuntime().exec("curl http://localhost:%d/sse --max-time .001".formatted(port)).waitFor();

			} catch (Exception e) {
				logger.error("Error to create SSE connection with curl, trying again...");
			}
		}
	}

	private void getSampleJsonIndefinitely(ExecutorService executor) {
		while (!executor.isShutdown()) {
			try {
				var sampleJson = restTemplate.getForObject("http://localhost:%d/sample_json".formatted(port), String.class);
				if (sampleJson.contains("id:") || sampleJson.contains("data:")) {
					logger.error("SSE payload leaked to another request! {}", sampleJson);
					executor.shutdownNow();
				}
			} catch (Exception e) {
				logger.error("Error to get JSON, trying again...");
			}
		}
	}
}
