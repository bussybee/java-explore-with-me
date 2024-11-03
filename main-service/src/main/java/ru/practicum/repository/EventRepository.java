package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    @Query("select e from Event as e " +
            "where e.initiator.id in ?1 and e.state in ?2 and e.category.id in ?3 " +
            "and (e.eventDate between ?4 and ?5)")
    List<Event> findAllByParams(List<Integer> userIds, List<String> states, List<Integer> categories,
                                LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    List<Event> findAllByCategoryId(Long catId);
}
