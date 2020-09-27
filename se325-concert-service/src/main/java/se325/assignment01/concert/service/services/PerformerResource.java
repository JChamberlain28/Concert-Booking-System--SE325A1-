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

/**
 * This resource class contains endpoints for getting performers
 */

@Path("/concert-service/performers")
public class PerformerResource {


    private static Logger LOGGER = LoggerFactory.getLogger(PerformerResource.class);

    /**
     * This method is an endpoint that gets a performer based on an "id" path parameter.
     * It returns a 404 NOT FOUND HTTP code if there is no performer with the specified id.
     * @param id - The id of the performer to retrieve
     * @return - A performer data transfer object with performer details in it (converted to json)
     */

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


    /**
     * This method is an endpoint that gets all performers.
     * @return - List of PerformerDTO objects (converted to json)
     */

    @GET
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveAllPerformers() {

        List<Performer> performers;


        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            em.getTransaction().begin();
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
            performers = performerQuery.getResultList();




            // Converts all Performers in the list of Performers to PerformerDTO's and adds them to a list collection.
            // This is then wrapped in a GenericEntity.
            GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performers.stream()
                    .map(performer -> PerformerMapper.convertToDTO(performer)).collect(Collectors.toList())) {
            };

            em.getTransaction().commit();
            Response.ResponseBuilder builder = Response.ok(entity);


            return builder.build();
        } finally {
            em.close();
        }


    }




}