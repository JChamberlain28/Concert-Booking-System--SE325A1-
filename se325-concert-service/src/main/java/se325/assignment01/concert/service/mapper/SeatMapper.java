package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Seat;

public class SeatMapper {
    public static SeatDTO convertToDTO(Seat seat){

        // create dto using field info contained in a seat object
        return new SeatDTO(seat.getLabel(), seat.getPrice());

    }
}
