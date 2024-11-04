package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.event.*;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "eventDate", source = "event.eventDate", qualifiedByName = "dateToString")
    @Mapping(target = "createdOn", source = "event.createdOn", qualifiedByName = "dateToString")
    @Mapping(target = "publishedOn", source = "event.publishedOn", qualifiedByName = "dateToString")
    FullEventDto toDto(Event event);

    @Mapping(target = "eventDate", source = "eventDto.eventDate", qualifiedByName = "stringToDate")
    Event newEventToEntity(NewEventDto eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "eventDate", source = "dto.eventDate", qualifiedByName = "stringToDate")
    void updateEventFromDto(UpdateEventAdminRequest dto, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "eventDate", source = "dto.eventDate", qualifiedByName = "stringToDate")
    void updateEventFromDto(UpdateEventUserRequest dto, @MappingTarget Event event);

    @Named("stringToDate")
    static LocalDateTime convertStringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Named("dateToString")
    static String convertLocalDateToString(LocalDateTime localDateTime) {
        if (Optional.ofNullable(localDateTime).isPresent()) {
            return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return null;
    }
}
