package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConcertMapper {
    public static ConcertDTO convertToDTO(Concert concert){

        ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(),
                concert.getImageName(), concert.getBlurb());

        concertDTO.setDates(concert.getDates().stream().collect(Collectors.toList()));

        // converts each performer to a performerDTO and then collects them in a list, then adding them
        // to the ConcertDTO
        concertDTO.setPerformers(concert.getPerformers().stream()
                .map(performer -> PerformerMapper.convertToDTO(performer)).collect(Collectors.toList()));

        return concertDTO;

    }
}
