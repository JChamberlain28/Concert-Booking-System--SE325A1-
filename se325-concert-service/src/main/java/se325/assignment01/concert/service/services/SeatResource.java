package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.common.types.BookingStatus;
import se325.assignment01.concert.service.domain.Seat;
import se325.assignment01.concert.service.jaxrs.LocalDateTimeParam;
import se325.assignment01.concert.service.mapper.SeatMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/concert-service/seats")
public class SeatResource {

    private static Logger LOGGER = LoggerFactory.getLogger(SeatResource.class);


    @GET
    @Path("/{date}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveSeats(@QueryParam("status") @DefaultValue("Any") BookingStatus bookingStatus, @PathParam("date") String dateString){ //, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {

        LocalDateTime dateObj = new LocalDateTimeParam(dateString).getLocalDateTime();



        String query = "select s from Seat s where s.date='" + dateObj + "'";
        if (bookingStatus.toString().equals("Any")){
            // add nothing to query string
        } else if (bookingStatus.toString().equals("Booked")){
            query += " and isBooked = true";
        } else if (bookingStatus.toString().equals("Unbooked")){

            query += " and isBooked = false";

        } else {
            // TODO is this bad request if unknown status put in
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }



        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery = em.createQuery(query, Seat.class);
            List<Seat> seats = seatQuery.getResultList();
            em.getTransaction().commit();

            // convert Seat objects into SeatDTO objects for use in data transfer
            GenericEntity<List<SeatDTO>> entity = new GenericEntity<List<SeatDTO>>(seats.stream()
                        .map(seat -> SeatMapper.convertToDTO(seat)).collect(Collectors.toList())){};
            Response.ResponseBuilder builder = Response.ok(entity);
            return builder.build();


        } finally {

            em.close();
        }




    }



}
