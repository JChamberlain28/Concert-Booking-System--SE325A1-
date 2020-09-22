package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Seat;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static BookingDTO convertToDTO(Booking booking){

        // convert a set of seats to a list of seatDTO's
        List<SeatDTO> seats = booking.getSeats().stream()
                .map(seat -> SeatMapper.convertToDTO(seat)).collect(Collectors.toList());


        return new BookingDTO(booking.getConcertId(), booking.getDate(), seats);

    }
}
