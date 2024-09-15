package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.repository.HitRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class HitService {
    private final HitRepository hitRepository;
    private final HitMapper hitMapper;

    public void save(EndpointHitDto hitDto) {
        log.info("Hit {} saved", hitRepository.save(hitMapper.toEntity(hitDto)));
    }

}
