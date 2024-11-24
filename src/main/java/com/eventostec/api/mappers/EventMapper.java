package com.eventostec.api.mappers;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.Date;

@Mapper(componentModel = "spring")
public interface EventMapper {

        @Mappings({
                @Mapping(target = "id", ignore = true),
                @Mapping(source = "dto.title", target = "title"),
                @Mapping(source = "dto.description", target = "description"),
                @Mapping(target = "imgUrl", source = "imgUrl"),
                @Mapping(source = "dto.eventUrl", target = "eventUrl"),
                @Mapping(source = "dto.date", target = "date", qualifiedByName = "epochToDate"),
                @Mapping(source = "dto.remote", target = "remote"),
        })
        Event toEntity(EventRequestDTO dto, String imgUrl);

        @Mappings({
                @Mapping(source = "entity.title", target = "title"),
                @Mapping(source = "entity.description", target = "description"),
                @Mapping(source = "entity.eventUrl", target = "eventUrl"),
                @Mapping(source = "entity.date", target = "date", qualifiedByName = "dateToEpoch"),
                @Mapping(source = "entity.remote", target = "remote"),
        })
        EventRequestDTO toDto(Event entity);

    @Named("epochToDate")
    default Date epochToDate(Long timestamp) {
        return timestamp != null ? new Date(timestamp) : null;
    }

    @Named("dateToEpoch")
    default Long dateToEpoch(Date date) {
        return date != null ? date.getTime() : null;
    }
}
