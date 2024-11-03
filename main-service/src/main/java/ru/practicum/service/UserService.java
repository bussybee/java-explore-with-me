package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.FullEventDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.util.RequestStatus;
import ru.practicum.util.State;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.util.LocalDateTimeFormatter.parse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    EventRepository eventRepository;
    EventMapper eventMapper;

    AdminService adminService;
    PublicService publicService;

    CategoryMapper categoryMapper;

    RequestRepository requestRepository;
    RequestMapper requestMapper;

    LocationRepository locationRepository;

    public FullEventDto createEvent(Long userId, NewEventDto eventDto) {
        User initiator = adminService.getUserById(userId);

        if (parse(eventDto.getEventDate()).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }

        Event event = eventMapper.newEventToEntity(eventDto);

        Optional.ofNullable(eventDto.getCategoryId()).ifPresent(catId -> {
            CategoryDto category = publicService.getCategoryById(catId);
            event.setCategory(categoryMapper.toEntity(category));
        });

        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        Optional<Location> location = Optional.ofNullable(locationRepository.findByLatAndLon(
                event.getLocation().getLat(), event.getLocation().getLon()));
        if (location.isPresent()) {
            event.setLocation(location.get());
        } else {
            locationRepository.save(event.getLocation());
        }

        Event createdEvent = eventRepository.save(event);
        log.info("Created event: {}", createdEvent);

        return eventMapper.toDto(createdEvent);
    }


    public FullEventDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        Event event = getEventById(userId, eventId);

        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    if (!(event.getState().equals(State.PENDING))) {
                        throw new IllegalArgumentException("Изменить можно только события в состоянии ожидания модерации");
                    }
                    eventMapper.updateEventFromDto(eventDto, event);
                    break;
                case CANCEL_REVIEW:
                    if (!(event.getState().equals(State.CANCELED))) {
                        throw new IllegalArgumentException("Изменить можно только отмененные события");
                    }
                    event.setState(State.CANCELED);
                    break;
            }
        }

        return eventMapper.toDto(event);
    }

    public FullEventDto getEvent(Long userId, Long eventId) {
        return eventMapper.toDto(getEventById(userId, eventId));
    }

    public Event getEventById(Long userId, Long eventId) {
        adminService.getUserById(userId);
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
    }

    public List<FullEventDto> getEvents(Long userId, int from, int size) {
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from > 0 ? from / size : 0, size)).stream()
                .map(eventMapper::toDto).toList();
    }

    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        User requester = adminService.getUserById(userId);

        if (event.getInitiator().equals(requester)) {
            throw new IllegalArgumentException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new IllegalArgumentException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new IllegalArgumentException("У события достигнут лимит запросов на участие");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new IllegalArgumentException("Нельзя добавлять повторный запрос");
        }

        Request request = Request.builder()
                .requester(requester)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toDto(requestRepository.save(request));
    }

    public List<ParticipationRequestDto> getRequests(Long userId) {
        adminService.getUserById(userId);
        return requestRepository.findAllByRequesterId(userId).stream().map(requestMapper::toDto).toList();
    }


    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByRequesterIdAndEventId(userId, requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        request.setStatus(RequestStatus.CANCELLED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    public EventRequestStatusUpdateResult editRequestStatus(Long userId, Long eventId,
                                                            EventRequestStatusUpdateRequest eventRequest) {
        Event event = getEventById(userId, eventId);

        List<Request> requests = requestRepository.findAllById(eventRequest.getRequestsIds());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<Request> confRequests = new ArrayList<>();
        List<Request> rejectRequests = new ArrayList<>();

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new IllegalArgumentException("Статус можно изменить только у заявок, " +
                        "находящихся в состоянии ожидания");
            }

            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                request.setStatus(RequestStatus.CANCELLED);
                throw new IllegalArgumentException("У события достигнут лимит запросов на участие");
            }

            if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                break;
            }

            if (eventRequest.getRequestStatus().equals(RequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                event.incrementConfirmedRequests();
                confRequests.add(request);
            } else if (eventRequest.getRequestStatus().equals(RequestStatus.REJECTED)) {
                request.setStatus(RequestStatus.REJECTED);
                rejectRequests.add(request);
            }
        }

        requestRepository.saveAll(confRequests);
        requestRepository.saveAll(rejectRequests);

        result.setConfirmedRequests(confRequests.stream().map(requestMapper::toDto).toList());
        result.setRejectedRequests(rejectRequests.stream().map(requestMapper::toDto).toList());

        return result;
    }

    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        return requestRepository.findAllByEvent(userId, eventId).stream().map(requestMapper::toDto).toList();
    }
}