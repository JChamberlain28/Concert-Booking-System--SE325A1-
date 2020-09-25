package se325.assignment01.concert.service.services;

import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.mapper.ConcertMapper;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

public class SubscriptionResource {




    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response retrieveConcert(@PathParam("id") Long id, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {




        Concert concert = null;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);
            em.getTransaction().commit();


            if (concert == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            } else {
                Response.ResponseBuilder builder = Response.ok(ConcertMapper.convertToDTO(concert));
                //addCookie(builder, clientId);
                return builder.build();

            }
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }




    }



}
