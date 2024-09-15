package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.service.HitService;

@RestController
@RequestMapping(path = "/hit")
@RequiredArgsConstructor
@Slf4j
public class HitController {
    private final HitService hitService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody EndpointHitDto hitDto) {
        hitService.save(hitDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
