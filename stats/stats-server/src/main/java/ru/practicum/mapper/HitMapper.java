package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface HitMapper {
    @Mapping(target = "requestTime", source = "hitDto.timestamp", qualifiedByName = "stringToDate")
    @Mapping(target = "id", ignore = true)
    EndpointHit toEntity(EndpointHitDto hitDto);

    @Named("stringToDate")
    static LocalDateTime convertStringToLocalDateTime(String date){
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
