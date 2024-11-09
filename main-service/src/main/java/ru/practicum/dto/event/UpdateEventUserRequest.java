package ru.practicum.dto.event;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.model.Location;
import ru.practicum.util.UserStateAction;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequest {
    @Size(min = 3, max = 120)
    String title;
    @Size(min = 20, max = 2000)
    String annotation;
    @Size(min = 20, max = 7000)
    String description;
    Location location;
    Long category;
    String eventDate;
    Boolean paid;
    @PositiveOrZero
    Integer participantLimit;
    Boolean requestModeration;
    UserStateAction stateAction;
}
