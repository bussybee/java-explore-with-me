package ru.practicum.dto.event;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.dto.category.CategoryDto;
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
    CategoryDto category;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String eventDate;
    boolean paid;
    int participantLimit;
    boolean requestModeration;
    UserStateAction stateAction;
}
