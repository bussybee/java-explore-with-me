package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.util.State;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events")
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String title;
    String annotation;
    String description;
    @JoinColumn(name = "location_id")
    @ManyToOne
    Location location;
    @JoinColumn(name = "category_id")
    @ManyToOne
    Category category;
    @JoinColumn(name = "initiator_id")
    @ManyToOne
    User initiator;
    @Column(name = "event_date")
    LocalDateTime eventDate;
    @Column(name = "created_on")
    LocalDateTime createdOn;
    @Column(name = "published_on")
    LocalDateTime publishedOn;
    @Column(name = "participant_limit")
    int participantLimit;
    @Column(name = "confirmed_requests")
    int confirmedRequests;
    int views;
    boolean paid;
    @Column(name = "request_moderation")
    boolean requestModeration;
    @Enumerated(EnumType.STRING)
    State state;

    public void incrementConfirmedRequests() {
        confirmedRequests++;
    }
}
