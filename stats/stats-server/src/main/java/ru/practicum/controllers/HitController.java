package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.service.HitService;

@RestController
@RequestMapping(path = "/hit")
@RequiredArgsConstructor
public class HitController {
    private final HitService hitService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody EndpointHitDto hitDto) {
        hitService.save(hitDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
