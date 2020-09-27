package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;
import se325.assignment01.concert.service.mapper.PerformerMapper;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/concert-service/performers")
public class PerformerResource {


    private static Logger LOGGER = LoggerFactory.getLogger(PerformerResource.class);



    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrievePerformer(@PathParam("id") long id) {




        Performer performer = null;
        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            performer = em.find(Performer.class, id);



            if (performer == null) {
                // no performer was found with the supplied ID
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                Response.ResponseBuilder builder = Response.ok(PerformerMapper.convertToDTO(performer));
                em.getTransaction().commit();

                return builder.build();

            }
        } finally {
            em.close();
        }




    }





    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveAllPerformers() {

        List<Performer> performers;


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            performers = getPerformersFromDB(em);
            // Converts all Performers in the list of Performers to PerformerDTO's and adds them to a list collection.
            // This is then wrapped in a GenericEntity.
            GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performers.stream()
                    .map(performer -> PerformerMapper.convertToDTO(performer)).collect(Collectors.toList())) {
            };
            Response.ResponseBuilder builder = Response.ok(entity);

            return builder.build();
        } finally {
            em.close();
        }


    }



    // helper method to get all performers from DB
    private List<Performer> getPerformersFromDB(EntityManager em){
        em.getTransaction().begin();
        TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
        List<Performer> performers = performerQuery.getResultList();
        em.getTransaction().commit();
        return performers; // TODO: am I comitting to early? cos performer list used elsewhere?
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

}