package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByIdAndRequesterId(Long id, Long requestId);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByRequesterId(Long requesterId);

    @Query("select r from Request as r join Event as e on e.id = r.event.id where e.initiator.id = ?1 and e.id = ?2")
    List<Request> findAllByEvent(Long userId, Long eventId);
}
