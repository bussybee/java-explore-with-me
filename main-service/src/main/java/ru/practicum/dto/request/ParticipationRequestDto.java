package ru.practicum.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.util.RequestStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    Long id;
    Long requester;
    Long event;
    String created;
    RequestStatus status;
}
