package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Concert;

public class ConcertSummaryMapper {


    public static ConcertSummaryDTO convertToDTO(Concert concert){

        // create concert summary dto using field info contained in a concert object
        return new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());

    }

}
