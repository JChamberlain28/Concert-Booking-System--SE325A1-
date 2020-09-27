package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.mapper.ConcertMapper;
import se325.assignment01.concert.service.mapper.ConcertSummaryMapper;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This resource class contains all endpoints related to getting concert information as well as updating / deleting
 * concerts.
 */

@Path("/concert-service/concerts")
public class ConcertResource {


    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    /**
     * This method is an endpoint that gets a concert by its id. It throws a WebApplicationException
     * if there is no concert with the id specified.
     * @param id - id of concert to retrieve
     * @return - A concert data transfer object (converted to json)
     */

    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveConcert(@PathParam("id") Long id) {




        Concert concert = null;
        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            // get matching concert
            concert = em.find(Concert.class, id);
            em.getTransaction().commit();


            if (concert == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                Response.ResponseBuilder builder = Response.ok(ConcertMapper.convertToDTO(concert));
                return builder.build();

            }
        } finally {
            em.close();
        }




    }


    /**
     * This method is an endpoint that gets all concerts.
     * @return - A list of all concepts in a data transfer object format (converted to json)
     */

    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveAllConcerts() {

        List<Concert> concerts;


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            concerts = getConcertsFromDB(em);
            // Converts all Concerts in the list of Concerts to ConcertDTO's and adds them to a list collection.
            // This is then wrapped in a GenericEntity.
            GenericEntity<List<ConcertDTO>> entity = new GenericEntity<List<ConcertDTO>>(concerts.stream()
                    .map(concert -> ConcertMapper.convertToDTO(concert)).collect(Collectors.toList())) {
            };
            Response.ResponseBuilder builder = Response.ok(entity);

            return builder.build();
        } finally {
            em.close();
        }


    }

    /**
     * This method is an endpoint that gets all concert summaries (image and title only).
     * @return - A list of concert summaries in a data transfer object format (converted to json)
     */

    @GET
    @Path("/summaries")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveAllConcertSummaries() {

        List<Concert> concerts;


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            concerts = getConcertsFromDB(em);

            // Converts all Concerts in the list of Concerts to ConcertSummaryDTO's and adds them to a list collection.
            // This is then wrapped in a GenericEntity.
            GenericEntity<List<ConcertSummaryDTO>> entity = new GenericEntity<List<ConcertSummaryDTO>>(concerts.stream()
                    .map(concert -> ConcertSummaryMapper.convertToDTO(concert)).collect(Collectors.toList())){};
            Response.ResponseBuilder builder = Response.ok(entity);


            return builder.build();
        } finally {
            em.close();
        }


    }

    /**
     * A helper method to get all concerts from the database. Used to reduce code duplication.
     * @param em - Entity manager being used by the calling method
     * @return - A list of Concert objects representing concerts at the theatre
     */


    private List<Concert> getConcertsFromDB(EntityManager em){
        em.getTransaction().begin();
        TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
        List<Concert> concerts = concertQuery.getResultList();
        em.getTransaction().commit();
        return concerts;
    }

}