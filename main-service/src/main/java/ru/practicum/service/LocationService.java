package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.repository.LocationRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public void save(Event event) {
        Optional<Location> location = Optional.ofNullable(locationRepository.findByLatAndLon(
                event.getLocation().getLat(), event.getLocation().getLon()));
        if (location.isPresent()) {
            event.setLocation(location.get());
        } else {
            locationRepository.save(event.getLocation());
        }
    }
}
