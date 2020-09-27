package se325.assignment01.concert.service.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import se325.assignment01.concert.common.jackson.LocalDateTimeDeserializer;
import se325.assignment01.concert.common.jackson.LocalDateTimeSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name ="BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    private Long id;

    private long concertId;
    private LocalDateTime date;


    // Eager fetch here as we access properties of all seats to set to booked
    // (This assumes that the original query to get seats is eager due to it then being put into this set)
    @OneToMany(fetch = FetchType.EAGER)
    private Set<Seat> seats = new HashSet<>();
    public Booking() {
    }

    public Booking(long concertId, LocalDateTime date, Set<Seat> seats) {
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
    }


    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public Long getId(){ return id; }


    // bookings are equal if they have the same ID
    // (used for .contains() on a booking set to find if authorised user has a certain booking)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        return new EqualsBuilder()
                .append(id, booking.id)
                .isEquals();
    }
}
