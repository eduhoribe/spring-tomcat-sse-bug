package com.github.eduhoribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;


@RestController
class SseController {
	private static final Logger logger = LoggerFactory.getLogger(SseController.class);

	private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

	@GetMapping("/sample_json")
	String getSampleJson() {
		// JSON source: https://json.org/example.html
		return """
				{"glossary":{"title":"example glossary","GlossDiv":{"title":"S","GlossList":{"GlossEntry":{"ID":"SGML","SortAs":"SGML","GlossTerm":"Standard Generalized Markup Language","Acronym":"SGML","Abbrev":"ISO 8879:1986","GlossDef":{"para":"A meta-markup language, used to create markup languages such as DocBook.","GlossSeeAlso":["GML","XML"]},"GlossSee":"markup"}}}}}\
				""";
	}

	@GetMapping("/sse")
	SseEmitter createSse() throws IOException {
		var uuid = UUID.randomUUID().toString();

		var sseEmitter = new SseEmitter();
		sseEmitter.onCompletion(() -> {
			logger.info("Completed! UUID: {}", uuid);
			sseEmitters.remove(uuid);
		});
		sseEmitter.onError(e -> {
			logger.error("Error! UUID: {}", uuid, e);
			sseEmitters.remove(uuid);
		});
		sseEmitter.onTimeout(() -> {
			logger.warn("Timeout! UUID: {}", uuid);
			sseEmitters.remove(uuid);
		});

		sseEmitters.put(uuid, sseEmitter);

		// Commenting the line bellow apparently fixes the issue, but I'm not sure why...
		sseEmitter.send(event().id(uuid).data("hello"));

		return sseEmitter;
	}

	@Scheduled(fixedDelay = 500)
	void heartbeat() {
		sseEmitters.forEach((uuid, emitter) -> {
			try {
				emitter.send(event().id(uuid).data("heartbeat"));

			} catch (Exception e) {
				logger.error("Heartbeat error! UUID: {}", uuid, e);
				sseEmitters.remove(uuid);
			}
		});
	}
}
