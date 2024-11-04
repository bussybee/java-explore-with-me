package ru.practicum.dto.event;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.model.Location;
import ru.practicum.util.AdminStateAction;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {
    @Size(min = 3, max = 120)
    String title;
    @Size(min = 20, max = 2000)
    String annotation;
    @Size(min = 20, max = 7000)
    String description;
    Location location;
    Long category;
    String eventDate;
    boolean paid = false;
    int participantLimit = 0;
    boolean requestModeration = true;
    AdminStateAction stateAction;
}
