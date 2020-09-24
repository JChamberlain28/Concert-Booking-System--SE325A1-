package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.UserDTO;
import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/concert-service/login")
public class LoginResource {

    private static Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response login(UserDTO user) {


        String username = user.getUsername();
        String password = user.getPassword();

        List<User> userResults = null;
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {// dont need transaction begin and commit as its only reading DB
            em.getTransaction().begin();
            TypedQuery<User> usersQuery = em.createQuery("select u from User u where u.username='" + username
                    + "' and u.password='" + password + "'", User.class);
            userResults = usersQuery.getResultList();




            if (userResults.isEmpty()) {
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            } else {
                NewCookie cookie = makeCookie();


                Response.ResponseBuilder builder = Response.ok();
                if (cookie != null) { // this is entered if the client didnt have a cookie (clientId = null)
                    builder.cookie(cookie);
                    // persist record of cookie/token in user class in database
                    userResults.get(0).setToken(cookie);

                }
                em.getTransaction().commit();

                return builder.build();

            }
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }




    }

    // Makes a cookie regardless if one existed already
    private NewCookie makeCookie() {
        NewCookie newCookie = null;

            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());

        return newCookie;
    }

}
