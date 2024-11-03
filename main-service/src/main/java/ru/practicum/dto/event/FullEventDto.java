package ru.practicum.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Location;
import ru.practicum.util.State;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FullEventDto {
    Long id;
    String title;
    String annotation;
    String description;
    Location location;
    CategoryDto category;
    UserShortDto initiator;
    String eventDate;
    String createdOn;
    String publishedOn;
    int participantLimit;
    int confirmedRequests;
    int views;
    boolean paid;
    boolean requestModeration;
    State state;
}
