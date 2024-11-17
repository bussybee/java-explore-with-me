package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "comments")
@Entity
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String content;
    @JoinColumn(name = "author_id")
    @ManyToOne
    User author;
    @Column(name = "event_id")
    Long eventId;
    LocalDateTime created;
    LocalDateTime modified;
    @Column(name = "edited")
    Boolean isEdited;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", eventId=" + eventId +
                '}';
    }
}