package com.relief.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimeBroadcaster {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register(Long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        sendHeartbeat(emitter);
        return emitter;
    }

    public void broadcast(String type, Object payload) {
        Map<String, Object> event = Map.of(
                "id", UUID.randomUUID().toString(),
                "type", type,
                "ts", Instant.now().toString(),
                "data", payload
        );
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(type)
                        .data(event));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        });
    }

    public void sendHeartbeat(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data(Instant.now().toString()));
        } catch (IOException e) {
            emitter.complete();
            emitters.remove(emitter);
        }
    }
}





