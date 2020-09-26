package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Seat;
import se325.assignment01.concert.service.domain.User;
import se325.assignment01.concert.service.mapper.BookingMapper;
import se325.assignment01.concert.service.mapper.ConcertMapper;
import se325.assignment01.concert.service.mapper.ConcertSummaryMapper;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.awt.print.Book;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Path("/concert-service")
public class BookingResource {

    // ConcurrentHashMap as different threads share it when using their own instances of BookingResource
    private static Map<Long, List<Subscription>> subscriptions = new HashMap<Long, List<Subscription>>();

    private static Logger LOGGER = LoggerFactory.getLogger(se325.assignment01.concert.service.services.BookingResource.class);

    @POST
    @Path("/bookings")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response book(BookingRequestDTO br, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {

        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();

            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            // check concert exists and get it
            Concert concert = em.find(Concert.class, br.getConcertId());



            if (concert == null){
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }


            if (!concert.getDates().contains(br.getDate())){
                // date not existent for concert
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }


            //TODO might be slow cos we doing cartesian product of dates and labels in query
            //gets all seats from the booking request
            TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.label in :labels and s.date in :dates", Seat.class)
                    .setParameter("labels", br.getSeatLabels()).setParameter("dates", br.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC);
            List<Seat> seatsResults = seatsQuery.getResultList();

            if (seatsResults.isEmpty()){
                // no seats exist that match the requested seats
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            for (Seat s : seatsResults){
                if (s.getIsBooked()){
                    // a requested seat is booked so this is unauthorised
                    em.getTransaction().rollback();
                    throw new WebApplicationException(Response.Status.FORBIDDEN);
                } else {
                    s.setIsBooked(true);
                }

            }



            // If the code gets here, then the booking is valid
            Booking booking = new Booking(br.getConcertId(), br.getDate(), new HashSet<>(seatsResults));
            authUser.addUserBooking(booking);
            em.persist(booking);
            String bookingId = booking.getId().toString();

            em.getTransaction().commit();
            em.getTransaction().begin();
            notifySubscribers(em, br);
            em.getTransaction().commit();





            return Response.created(URI.create("/concert-service/bookings/" + bookingId)).build();

        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }




    }



    @GET
    @Path("/bookings/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveBooking(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {


        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();

            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            Booking booking = em.find(Booking.class, id);

            if (booking == null){
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }



            if (!authUser.hasBooking(booking)){
                // user is not allowed to access a booking that doesnt belong to them
                throw new WebApplicationException((Response.Status.FORBIDDEN));
            }

            Response.ResponseBuilder response = Response.ok(BookingMapper.convertToDTO(booking));
            return response.build();

        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }






    }


    @GET
    @Path("/bookings")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveBookingsForUser(@CookieParam(Config.CLIENT_COOKIE) Cookie auth) {


        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();

            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            List<Booking> bookings = new ArrayList<>(authUser.getUserBookings());
            GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(bookings.stream()
                    .map(booking -> BookingMapper.convertToDTO(booking)).collect(Collectors.toList())){};
            Response.ResponseBuilder response = Response.ok(entity);
            return response.build();

        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }






    }





    @POST
    @Path("/subscribe/concertInfo")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public void subscribe(ConcertInfoSubscriptionDTO concertSubscriptionInfoDTO,
                                    @Suspended AsyncResponse asyncResp, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {
        Long concertId = concertSubscriptionInfoDTO.getConcertId();
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();


            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            // check if concert exists
            Concert concert = em.find(Concert.class, concertId);
            if (concert == null) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            // check if concert contains date
            if (!concert.getDates().contains(concertSubscriptionInfoDTO.getDate())){
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            em.getTransaction().commit();

            Subscription subscription = new Subscription(concertSubscriptionInfoDTO, asyncResp);

            if (!subscriptions.containsKey(concertId)){
               subscriptions.put(concertId, new ArrayList<Subscription>());
            }
            subscriptions.get(concertId).add(subscription);



        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }




    }



    class Subscription {
        private AsyncResponse asyncResp;
        private ConcertInfoSubscriptionDTO infoDTO;

        public Subscription(ConcertInfoSubscriptionDTO infoDTO, AsyncResponse asyncResp){
            this.asyncResp = asyncResp;
            this.infoDTO = infoDTO;
        }

        public AsyncResponse getAsyncResp() {
            return asyncResp;
        }
        public ConcertInfoSubscriptionDTO getInfoDTO() {
            return infoDTO;
        }
    }



    // helper method

    /**
     * This is a helper method to get the user corresponding to an authorisation token. It throws a WebApplicationException
     * contianin a HTTP unauthorised code.
     * @param auth - cookie that identifies a user
     * @param em - entity manager currently open (being used in the transaction this method was called from)
     * @return A User object for the user corresponding to the authorisation cookie
     */
    private User getAuthUser(Cookie auth, EntityManager em) {
        User userResult;

        if (auth == null){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        TypedQuery<User> usersQuery = em.createQuery("select u from User u where u.userToken='" + auth.getValue()
                + "' and u.userToken IS NOT NULL", User.class);
        userResult = usersQuery.getSingleResult();


        if (userResult == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return userResult;
    }



    private void notifySubscribers(EntityManager em, BookingRequestDTO br){
        // get total num of seats, and number booked
        Long totalSeats = em.createQuery("select COUNT(s) from Seat s where s.date in :dates", Long.class)
                .setParameter("dates", br.getDate()).getSingleResult();

        Long bookedSeats = em.createQuery("select COUNT(s) from Seat s where s.date in :dates and s.isBooked=true", Long.class)
                .setParameter("dates", br.getDate()).getSingleResult();

        // calculate % booked
        double percentBookedOnDate = (((double)bookedSeats) / totalSeats)*100;

        // calculate remaining seats
        int remainingSeats = (int)(totalSeats - bookedSeats);

        // if there are any subscribers to any date of a concert
        if (subscriptions.containsKey(br.getConcertId())){
            List<Subscription> subs = subscriptions.get(br.getConcertId());
            //if (!subs.isEmpty()){
                for (Subscription s : subs){
                    if (s.getInfoDTO().getDate().equals(br.getDate())){
                        if (s.getInfoDTO().getPercentageBooked() <= percentBookedOnDate){
                            //If subscription is on the date of this new booking, and the percent booked threshold is
                            // reached, notify the subscriber
                            s.getAsyncResp().resume(new ConcertInfoNotificationDTO(remainingSeats));
                        }
                    }

                }
            //}
        }








    }



}
