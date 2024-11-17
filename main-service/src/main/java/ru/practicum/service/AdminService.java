package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.IncorrectDataException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.practicum.util.LocalDateTimeFormatter;
import ru.practicum.util.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.util.LocalDateTimeFormatter.parse;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService {
    UserRepository userRepository;
    UserMapper userMapper;

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    EventRepository eventRepository;
    EventMapper eventMapper;

    CompilationRepository compilationRepository;
    CompilationMapper compilationMapper;

    LocationService locationService;

    CommentRepository commentRepository;

    public UserDto createUser(UserDto userDto) {
        User savedUser = userRepository.save(userMapper.toEntity(userDto));
        log.info("User created: {}", savedUser);

        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            users = userRepository.findAll(pageable).getContent();
        }

        return users.stream().map(userMapper::toDto).toList();
    }

    public void deleteById(Long id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        Category createdCategory = categoryRepository.save(categoryMapper.toEntity(categoryDto));
        log.info("Category created: {}", createdCategory);
        return categoryMapper.toDto(createdCategory);
    }

    public FullEventDto editEvent(Long eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Optional.ofNullable(eventDto.getEventDate()).ifPresent(date -> {
            if (parse(date).isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IncorrectDataException("Дата начала изменяемого события " +
                        "должна быть не ранее чем за час от даты публикации");
            }
        });

        eventMapper.updateEventFromDto(eventDto, event);

        Optional.ofNullable(eventDto.getCategory()).ifPresent(catId -> event.setCategory(categoryRepository
                .findById(catId).orElseThrow()));

        if (Optional.ofNullable(eventDto.getLocation()).isPresent()) {
            locationService.save(event);
        }

        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (!event.getState().equals(State.PENDING)) {
                        throw new IllegalArgumentException("Событие можно публиковать только если оно " +
                                "в состоянии ожидания публикации");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState().equals(State.PUBLISHED)) {
                        throw new IllegalArgumentException("Событие можно отклонить только если оно еще не опубликовано");
                    }
                    event.setState(State.CANCELED);
                    break;
                default:
                    throw new IllegalArgumentException("Событие не удовлетворяет правилам редактирования");
            }
        }

        eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Transactional(readOnly = true)
    public List<FullEventDto> getEvents(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, int from, int size) {
        QEvent event = QEvent.event;
        BooleanBuilder predicate = new BooleanBuilder();

        Optional.ofNullable(users)
                .map(event.initiator.id::in)
                .ifPresent(predicate::and);

        Optional.ofNullable(states)
                .map(s -> s.stream().map(State::valueOf).toList())
                .map(event.state::in)
                .ifPresent(predicate::and);

        Optional.ofNullable(categories)
                .map(event.category.id::in)
                .ifPresent(predicate::and);

        Optional.ofNullable(rangeStart)
                .map(LocalDateTimeFormatter::parse)
                .ifPresent(start -> predicate.and(event.eventDate.after(start)));

        Optional.ofNullable(rangeEnd)
                .map(LocalDateTimeFormatter::parse)
                .ifPresent(end -> predicate.and(event.eventDate.before(end)));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return eventRepository.findAll(predicate, pageable).stream().map(eventMapper::toDto).toList();
    }

    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new IllegalArgumentException("Существуют события, связанные с категорией");
        }
        categoryRepository.deleteById(catId);
    }


    public CategoryDto editCategory(Long catId, NewCategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        category.setName(categoryDto.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = new Compilation();
        Optional.ofNullable(compilationDto.getTitle()).ifPresent(compilation::setTitle);
        Optional.ofNullable(compilationDto.getPinned()).ifPresent(compilation::setPinned);
        Optional.ofNullable(compilationDto.getEvents())
                .ifPresent(events -> compilation.setEvents(events.stream()
                        .map(eventId -> eventRepository.findById(eventId).orElseThrow())
                        .toList()));

        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    public void deleteCompilation(Long id) {
        compilationRepository.deleteById(id);
    }

    public CompilationDto editCompilation(Long id, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        compilationMapper.updateCompFromDto(compilationDto, compilation);
        Optional.ofNullable(compilationDto.getEvents())
                .ifPresent(events -> compilation.setEvents(eventRepository.findAllById(events)));
        compilationRepository.save(compilation);
        return compilationMapper.toDto(compilation);
    }

    public void deleteComment(Long comId) {
        commentRepository.findById(comId).orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        commentRepository.deleteById(comId);
    }
}
