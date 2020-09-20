package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.Concert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
@Path("/concerts")
public class ConcertResource {


    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    private Map<Long, Concert> concertDB = new ConcurrentHashMap<>();
    private AtomicLong idCounter = new AtomicLong();


    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveConcert(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {


//

        Concert concert = null;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        if (concert == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            Response.ResponseBuilder builder = Response.ok(concert);
            addCookie(builder, clientId);
            return builder.build();

        }


    }

    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveConcerts(@QueryParam("start") long start, @QueryParam("size") int size, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        // The Response object should store an ArrayList<Concert> entity. The
        // ArrayList can be empty depending on the start and size arguments,
        // and Concerts stored.
        //
        // Because of type erasure with Java Generics, any generically typed
        // entity needs to be wrapped by a javax.ws.rs.core.GenericEntity that
        // stores the generic type information. Hence to add an ArrayList as a
        // Response object's entity, you should use the following code:
        //
        List<Concert> concerts;


        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where c.id >= " + start + " and c.id < " + size + start, Concert.class);
            concerts = concertQuery.getResultList();
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        GenericEntity<List<Concert>> entity = new GenericEntity<List<Concert>>(concerts) {
        };
        Response.ResponseBuilder builder = Response.ok(entity);
        addCookie(builder, clientId);

        return builder.build();
    }

    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})

    public Response createConcert(Concert concert, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {

        // Reminder: You won't need to annotate the "concert" argument above - arguments without annotations are
        // assumed by JAX-RS to come from the HTTP request body.
        //Long count = idCounter.getAndIncrement();
        //concertDB.put(count, concert);
        String concertId;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            em.persist(concert);
            em.flush();
            concertId = concert.getId().toString();
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }


        Response.ResponseBuilder builder = Response.created(URI.create("/concerts/" + concertId));
        addCookie(builder, clientId);
        return builder.build(); // TODO: check content of concert before adding, throw error

    }

    @DELETE
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response delete(@PathParam("id") long id) {

        Concert concert = null;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);
            em.remove(concert);
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        if (concert == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Response.ResponseBuilder builder = Response.noContent();
        //addCookie(builder, clientId);
        return builder.build(); // TODO: check, throw error
    }

    @PUT
    public Response updateConcerts(Concert concert) {


        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            em.merge(concert);
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        Response.ResponseBuilder builder = Response.noContent();
        //addCookie(builder, clientId);
        return builder.build(); // TODO: check, throw error
    }

    @DELETE
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response deleteAllConcerts(@CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {


        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            for (Concert c : concerts) {
                em.remove(c);
            }
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        Response.ResponseBuilder builder = Response.noContent();
        addCookie(builder, clientId);
        return builder.build(); // TODO: check, throw error
    }


    // helper method
    private void addCookie(Response.ResponseBuilder builder, Cookie clientId) {
        NewCookie nCookie = makeCookie(clientId);
        if (nCookie != null) { // dont have a cookie so one was made
            builder.cookie(nCookie); // add cookie to response
        } else { // TODO: idk if this is needed
            builder.cookie(new NewCookie(clientId));
        }
    }

    /**
     * Helper method that can be called from every service method to generate a
     * NewCookie instance, if necessary, based on the clientId parameter.
     *
     * @param clientId the Cookie whose name is Config.CLIENT_COOKIE, extracted
     *                 from a HTTP request message. This can be null if there was no cookie
     *                 named Config.CLIENT_COOKIE present in the HTTP request message.
     * @return a NewCookie object, with a generated UUID value, if the clientId
     * parameter is null. If the clientId parameter is non-null (i.e. the HTTP
     * request message contained a cookie named Config.CLIENT_COOKIE), this
     * method returns null as there's no need to return a NewCookie in the HTTP
     * response message.
     */
    private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie = null;

        if (clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }
}