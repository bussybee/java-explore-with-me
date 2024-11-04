package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.util.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.util.LocalDateTimeFormatter.parse;

@Service
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

    public UserDto createUser(UserDto userDto) {
        User savedUser = userRepository.save(userMapper.toEntity(userDto));
        log.info("User created: {}", savedUser);

        return userMapper.toDto(savedUser);
    }

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
        userRepository.delete(getUserById(id));
    }

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
        eventMapper.updateEventFromDto(eventDto, event);

        Optional.ofNullable(eventDto.getCategoryId()).ifPresent(catId -> event.setCategory(categoryRepository
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
                    Optional.ofNullable(eventDto.getEventDate()).ifPresent(date -> {
                        if (parse(date).isBefore(LocalDateTime.now().plusHours(1))) {
                            throw new IllegalArgumentException("Дата начала изменяемого события " +
                                    "должна быть не ранее чем за час от даты публикации");
                        }
                    });
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

    public List<FullEventDto> getEvents(List<Integer> users, List<String> states, List<Integer> categories,
                                        String rangeStart, String rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return eventRepository.findAllByParams(users, states, categories, parse(rangeStart),
                parse(rangeEnd), pageable).stream().map(eventMapper::toDto).toList();
    }

    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new IllegalArgumentException("Существуют события, связанные с категорией");
        }
        categoryRepository.deleteById(catId);
    }


    public CategoryDto editCategory(Long catId, CategoryDto categoryDto) {
        categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена"));
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(categoryDto)));
    }

    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = new Compilation();
        Optional.ofNullable(compilationDto.getTitle()).ifPresent(compilation::setTitle);
        compilation.setPinned(compilation.isPinned());
        Optional.ofNullable(compilationDto.getEvents())
                .ifPresent(events -> compilation.setEvents(events.stream()
                        .map(eventShortDto -> eventRepository.findById(eventShortDto.getId())
                                .orElseThrow(() -> new NotFoundException("Событие с" + eventShortDto.getId() + "не найдено")))
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
        return compilationMapper.toDto(compilation);
    }
}
