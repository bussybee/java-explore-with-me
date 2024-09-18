package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final HitRepository hitRepository;

    public List<ViewStatsDto> getStatistic(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, boolean unique) {
        List<ViewStatsDto> stats;

        if (Optional.ofNullable(uris).isEmpty()) {
            stats = hitRepository.findAllHits(start, end);
        } else {
            if (unique) {
                stats = hitRepository.findAllUniqueHitsByUris(uris, start, end);
            } else {
                stats = hitRepository.findAllHitsByUris(uris, start, end);
            }
        }

        return stats;
    }
}
