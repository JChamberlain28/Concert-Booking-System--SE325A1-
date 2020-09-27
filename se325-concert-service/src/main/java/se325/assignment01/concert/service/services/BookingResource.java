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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This resource class contains all endpoints related to bookings and notifying subscribed users
 * about concert booking status.
 */

@Path("/concert-service")
public class BookingResource {

    // ConcurrentHashMap as different threads share it when using their own instances of BookingResource (thread safe)
    private static Map<Long, List<Subscription>> subscriptions = new ConcurrentHashMap<Long, List<Subscription>>();

    private static Logger LOGGER = LoggerFactory.getLogger(se325.assignment01.concert.service.services.BookingResource.class);

    /**
     * This method is an endpoint that allows a user to book seats for a concert on a specific day. The user must
     * be authenticated (logged in) to use this endpoint. Upon creating a booking, all current subscriptions for the
     * concert on the date specified will be checked. If the new booking results in the % seats booked being at the
     * threshold for a subscription, users holding such subscriptions are notified.
     * @param br - A BookingRequestDTO that contains information on the concert, date and seats to book
     * @param auth - An authorisation token for use in identifying the user
     * @return - Response containing the URI of the newly created booking
     */

    @POST
    @Path("/bookings")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response book(BookingRequestDTO br, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {


        // check DTO is valid (including duplicate seat labels)
        if ((br.getDate() == null) || (br.getSeatLabels().isEmpty()) || (br.getSeatLabels().size() != (new HashSet(br.getSeatLabels())).size())){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }


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

            if (br.getSeatLabels().isEmpty()){
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }


            if (!concert.getDates().contains(br.getDate())){
                // date not existent for concert
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }


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
            em.close();
        }




    }

    /**
     * This method is an endpoint that gets a booking specified by the path parameter "id". This method requires
     * authorisation and users may only retrieve bookings that they have made.
     * @param id - id of the booking to retrieve
     * @param auth - An authorisation token for use in identifying the user
     * @return - A Booking data transfer object (converted to json)
     */

    @GET
    @Path("/bookings/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveBooking(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            Booking booking = em.find(Booking.class, id);


            // this is done first to hide if a booking exists but doesnt belong to the user
            if (!authUser.hasBooking(booking)){
                // user is not allowed to access a booking that doesnt belong to them
                em.getTransaction().rollback();
                throw new WebApplicationException((Response.Status.FORBIDDEN));
            }


            if (booking == null){
                // the booking id does not match any booking
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            Response.ResponseBuilder response = Response.ok(BookingMapper.convertToDTO(booking));

            em.getTransaction().commit();

            return response.build();

        } finally {
            em.close();
        }






    }

    /**
     * This method is an endpoint that gets all bookings for a user. The user in question is identified using an
     * authentication token. If there are no bookings, an empty list is returned (if it was not required to pass
     * certain tests, a 404 error would have been thrown instead)
     * @param auth - An authorisation token for use in identifying the user
     * @return - A list of bookings belonging to the user (using data transfer objects, converted to json)
     */

    @GET
    @Path("/bookings")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveBookingsForUser(@CookieParam(Config.CLIENT_COOKIE) Cookie auth) {


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            List<Booking> bookings = new ArrayList<>(authUser.getUserBookings());
            GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(bookings.stream()
                    .map(booking -> BookingMapper.convertToDTO(booking)).collect(Collectors.toList())){};
            Response.ResponseBuilder response = Response.ok(entity);

            em.getTransaction().commit();

            return response.build();

        } finally {
            em.close();
        }






    }


    /**
     * This method is an endpoint that allows users to subscribe for notifications for a concert on a specific date.
     * These notifications are based on the % seats booked. This endpoint utilises asyncronous communication to prevent
     * blocking while a notification has not been sent.
     * @param concertSubscriptionInfoDTO - Data transfer object containing information on the subscription such as
     *                                   threshold % seats booked to trigger a notification, and the concert details
     * @param asyncResp - Used to send a notification back to the user at a later time (without blocking)
     * @param auth - An authorisation token for use in identifying the user
     */


    @POST
    @Path("/subscribe/concertInfo")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public void subscribe(ConcertInfoSubscriptionDTO concertSubscriptionInfoDTO,
                                    @Suspended AsyncResponse asyncResp, @CookieParam(Config.CLIENT_COOKIE) Cookie auth) {

        Long concertId = concertSubscriptionInfoDTO.getConcertId();

        // check DTO is valid
        if ((concertId == null) || (concertSubscriptionInfoDTO.getDate() == null)){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();


            // checks cookie authorisation and returns the user that is logged in
            User authUser = getAuthUser(auth, em);

            // check if concert exists
            Concert concert = em.find(Concert.class, concertId);
            if (concert == null) {
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            // check if concert contains date
            if (!concert.getDates().contains(concertSubscriptionInfoDTO.getDate())){
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            em.getTransaction().commit();


            // create a subscription and store it in a list accessible to all client requests
            Subscription subscription = new Subscription(concertSubscriptionInfoDTO, asyncResp);

            if (!subscriptions.containsKey(concertId)){
               subscriptions.put(concertId, new ArrayList<Subscription>());
            }
            subscriptions.get(concertId).add(subscription);



        } finally {
            em.close();
        }




    }

    /**
     * This class represents a subscription. It contains the definition of the subscription in a
     * ConcertInfoSubscriptionDTO and an AsyncResponse that can be used to send a notification regarding
     * the subscription
     */

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




    /**
     * This is a helper method to get the user corresponding to an authorisation token. It throws a
     * WebApplicationException containing the HTTP unauthorised code if a user cannot be found
     * (authorisation failed).
     * @param auth - cookie that identifies a user
     * @param em - entity manager currently open (being used in the transaction this method was called from)
     * @return A User object for the user corresponding to the authorisation cookie
     */
    private User getAuthUser(Cookie auth, EntityManager em) {
        List<User> userResult;

        if (auth == null){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        TypedQuery<User> usersQuery = em.createQuery("select u from User u where u.userToken='" + auth.getValue()
                + "' and u.userToken IS NOT NULL", User.class);


        userResult = usersQuery.getResultList();




        if (userResult.isEmpty()) {
            em.getTransaction().rollback();
            // This is not FORBIDDEN, as the only case where no User is found, is when a non-authentic / non-valid
            // auth token is supplied. Hence it is equivalent to not providing an auth token at all.
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return userResult.get(0);
    }


    /**
     * This is a helper method that notifies a subscriber if the % seats booked for a concert / date, that has just had
     * a booking, is over the subscription threshold. It does this check for all subscriptions for the concert / date
     * that has just had a booking. This method is called from the book endpoint when a booking has just been completed.
     * This is because this is when a change in seats booked is first apparent.
     * @param em - entity manager currently open (this method is called within a transaction)
     * @param br - BookingRequestDTO representing the booking request that was just fulfilled
     *           (used to get the relevant subscriptions to update)
     */

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

            for (Subscription s : subs){
                if (s.getInfoDTO().getDate().equals(br.getDate())){
                    if (s.getInfoDTO().getPercentageBooked() <= percentBookedOnDate){
                        //If subscription is on the date of this new booking, and the percent booked threshold is
                        // reached, notify the subscriber
                        s.getAsyncResp().resume(new ConcertInfoNotificationDTO(remainingSeats));
                    }
                }
            }
        }








    }



}
