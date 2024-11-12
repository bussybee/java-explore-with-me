package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.Client;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.LocalDateTimeFormatter.FORMATTER;

@Service
@RequiredArgsConstructor
public class StatsManager {
    private final Client client;
    private final String appName = "ewm-main-service";

    public void sendEventHit(String uri, String ip) {
        client.sendHit(EndpointHitDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
    }

    public int getEventHits(Event event) {
        String eventUri = "/events/" + event.getId();
        List<ViewStatsDto> stats = client.getStats(event.getPublishedOn(), LocalDateTime.now(),
                List.of(eventUri), true);
        return !stats.isEmpty() ? stats.getFirst().getHits().intValue() : 0;
    }
}
