package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class Client {
    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:9090";

    public void sendHit(EndpointHitDto hitDto) {
        restTemplate.postForObject(baseUrl + "/hit", hitDto, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> params = new HashMap<>();
        params.put("start", start.format(formatter));
        params.put("end", end.format(formatter));
        params.put("uris", uris);
        params.put("unique", unique);

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(baseUrl + "/stats", ViewStatsDto[].class, params);

        return Arrays.stream(response.getBody()).toList();
    }
}
