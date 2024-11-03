package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.QEvent;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.util.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.util.LocalDateTimeFormatter.FORMATTER;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    CompilationRepository compilationRepository;
    CompilationMapper compilationMapper;

    EventRepository eventRepository;
    EventMapper eventMapper;

    StatsManager statsManager;

    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена")));
    }

    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        return categoryRepository.findAll(pageable).stream().map(categoryMapper::toDto).toList();
    }

    public CompilationDto getCompilationById(Long id) {
        return compilationMapper.toDto(compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подоборка не найдена")));
    }

    public List<CompilationDto> getCompilations(boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        return compilationRepository.findAllByPinned(pinned, pageable).stream().map(compilationMapper::toDto).toList();
    }

    public FullEventDto getEventById(Long id, String uri, String ip) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new IllegalArgumentException("Событие не опубликовано");
        }

        statsManager.sendEventHit(uri, ip);
        event.setViews(statsManager.getEventHits(event));

        return eventMapper.toDto(event);
    }

    public List<FullEventDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                        String rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
                                        String uri, String ip) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(event.state.eq(State.PUBLISHED));

        Optional.ofNullable(text)
                .map(t -> event.annotation.containsIgnoreCase(t).or(event.description.containsIgnoreCase(t)))
                .ifPresent(predicate::and);

        Optional.ofNullable(categories)
                .filter(cat -> !cat.isEmpty())
                .ifPresent(cat -> predicate.and(event.category.id.in(cat)));

        Optional.ofNullable(paid).ifPresent(p -> predicate.and(event.paid.eq(p)));

        Optional.ofNullable(rangeStart)
                .map(start -> LocalDateTime.parse(start, FORMATTER))
                .ifPresent(start -> predicate.and(event.eventDate.after(start)));

        Optional.ofNullable(rangeEnd)
                .map(end -> LocalDateTime.parse(end, FORMATTER))
                .ifPresent(end -> predicate.and(event.eventDate.before(end)));

        if (rangeEnd == null && rangeStart == null) {
            predicate.and(event.eventDate.after(LocalDateTime.now()));
        }

        Optional.ofNullable(onlyAvailable)
                .ifPresent(av -> predicate.and(event.confirmedRequests.lt(event.participantLimit)));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by(sort.equalsIgnoreCase("VIEWS") ? "views" : "eventDate"));

        List<Event> events = eventRepository.findAll(predicate, pageable).toList();
        events.forEach(e -> e.setViews(statsManager.getEventHits(e)));
        statsManager.sendEventHit(uri, ip);

        return events.stream().map(eventMapper::toDto).toList();
    }
}
