package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final HitRepository hitRepository;

    public List<ViewStatsDto> getStatistic(String start, String end, List<String> uris, boolean unique) {
        List<ViewStatsDto> stats;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        if (Optional.ofNullable(uris).isEmpty()) {
            stats = hitRepository.findAllHits(startTime, endTime);
        } else {
            if (unique) {
                stats = hitRepository.findAllUniqueHitsByUris(uris, startTime, endTime);
            } else {
                stats = hitRepository.findAllHitsByUris(uris, startTime, endTime);
            }
        }

        return stats;
    }
}
