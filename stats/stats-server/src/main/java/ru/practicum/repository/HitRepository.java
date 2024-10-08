package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.dto.ViewStatsDto(e.app, e.uri, count(distinct e.ip)) from EndpointHit e " +
            "where e.uri in ?1 and (e.requestTime between ?2 and ?3) " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc")
    List<ViewStatsDto> findAllUniqueHitsByUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStatsDto(e.app, e.uri, count(e.ip)) from EndpointHit e " +
            "where e.uri in ?1 and (e.requestTime between ?2 and ?3) " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStatsDto> findAllHitsByUris(List<String> uris, LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStatsDto(e.app, e.uri, count(e.ip)) from EndpointHit e " +
            "where (e.requestTime between ?1 and ?2) " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStatsDto> findAllHits(LocalDateTime start, LocalDateTime end);
}
