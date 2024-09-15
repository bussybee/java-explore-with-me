package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final HitRepository hitRepository;

    public List<ViewStatsDto> getStatistic(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, boolean unique) {
        List<ViewStatsDto> stats;

        if (unique) {
            stats = hitRepository.findAllUniqueHits(uris, start, end);
        } else {
            stats = hitRepository.findAllHits(uris, start, end);
        }

        return stats;
    }
}
