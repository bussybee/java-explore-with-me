package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.model.Location;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @Size(min = 3, max = 120)
    String title;
    @Size(min = 20, max = 2000)
    @NotBlank
    String annotation;
    @Size(min = 20, max = 7000)
    @NotBlank
    String description;
    Location location;
    Long categoryId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    String eventDate;
    boolean paid;
    boolean requestModeration;
    @PositiveOrZero
    int participantLimit;
}
