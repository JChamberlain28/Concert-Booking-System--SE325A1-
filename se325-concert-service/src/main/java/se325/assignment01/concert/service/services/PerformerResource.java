package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Performer;
import se325.assignment01.concert.service.mapper.ConcertMapper;
import se325.assignment01.concert.service.mapper.ConcertSummaryMapper;
import se325.assignment01.concert.service.mapper.PerformerMapper;

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
import java.util.stream.Collectors;

@Path("/concert-service/performers")
public class PerformerResource {


    private static Logger LOGGER = LoggerFactory.getLogger(PerformerResource.class);



    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrievePerformer(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {




        Performer performer = null;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            performer = em.find(Performer.class, id);
            em.getTransaction().commit();


            if (performer == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                Response.ResponseBuilder builder = Response.ok(PerformerMapper.convertToDTO(performer));
                //addCookie(builder, clientId);
                return builder.build();

            }
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }




    }





    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"}) // TODO: may not need cookie
    public Response retrieveAllPerformers(@CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {

        List<Performer> performers;


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            performers = getPerformersFromDB(em);
            // Converts all concerts in the list of concerts to concertDTO's and adds them to a list collection.
            // This is then wrapped in a GenericEntity.
            GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performers.stream()
                    .map(performer -> PerformerMapper.convertToDTO(performer)).collect(Collectors.toList())) {
            };
            Response.ResponseBuilder builder = Response.ok(entity);
            //addCookie(builder, clientId);

            return builder.build();
        } finally {
            em.close();
        }


    }



    // helper method
    private List<Performer> getPerformersFromDB(EntityManager em){
        em.getTransaction().begin();
        TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
        List<Performer> performers = performerQuery.getResultList();
        em.getTransaction().commit();
        return performers;
    }


//    @POST
//    @Produces({"application/json"})
//    @Consumes({"application/json"})
//    public Response createPerformer(Performer performer, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
//
//        String performerId;
//        // Acquire an EntityManager (creating a new persistence context).
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        try {// dont need transaction begin and commit as its only reading DB
//            em.getTransaction().begin();
//            em.persist(performer);
//            em.flush();
//            performerId = performer.getId().toString();
//            em.getTransaction().commit();
//        } finally {
//            // When you're done using the EntityManager, close it to free up resources.
//            em.close();
//        }
//
//
//        Response.ResponseBuilder builder = Response.created(URI.create("/performers/" + performerId));
//        //addCookie(builder, clientId);
//        return builder.build(); // TODO: check content of concert before adding, throw error
//
//    }

//    @DELETE
//    @Path("/{id}")
//    @Produces({"application/json"})
//    @Consumes({"application/json"})
//    public Response delete(@PathParam("id") long id) {
//
//        Performer performer = null;
//        // Acquire an EntityManager (creating a new persistence context).
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        try {// dont need transaction begin and commit as its only reading DB
//            em.getTransaction().begin();
//            performer = em.find(Performer.class, id);
//            em.remove(performer);
//            em.getTransaction().commit();
//        } finally {
//            // When you're done using the EntityManager, close it to free up resources.
//            em.close();
//        }
//        if (performer == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//        Response.ResponseBuilder builder = Response.noContent();
//        //addCookie(builder, clientId);
//        return builder.build(); // TODO: check, throw error
//    }
//
//    @PUT
//    public Response updatePerformer(Performer performer) {
//
//
//        // Acquire an EntityManager (creating a new persistence context).
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        try {// dont need transaction begin and commit as its only reading DB
//            em.getTransaction().begin();
//            em.merge(performer);
//            em.getTransaction().commit();
//        } finally {
//            // When you're done using the EntityManager, close it to free up resources.
//            em.close();
//        }
//
//        Response.ResponseBuilder builder = Response.noContent();
//        //addCookie(builder, clientId);
//        return builder.build(); // TODO: check, throw error
//    }
//
//    @DELETE
//    @Produces({"application/json"})
//    @Consumes({"application/json"})
//    public Response deleteAllPerformers(@CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
//
//
//        // Acquire an EntityManager (creating a new persistence context).
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        try {// dont need transaction begin and commit as its only reading DB
//            em.getTransaction().begin();
//            TypedQuery<Performer> concertQuery = em.createQuery("select p from Performer p", Performer.class);
//            List<Performer> performers = concertQuery.getResultList();
//
//            for (Performer c : performers) {
//                em.remove(c);
//            }
//            em.getTransaction().commit();
//        } finally {
//            // When you're done using the EntityManager, close it to free up resources.
//            em.close();
//        }
//
//        Response.ResponseBuilder builder = Response.noContent();
//        addCookie(builder, clientId);
//        return builder.build(); // TODO: check, throw error
//    }


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