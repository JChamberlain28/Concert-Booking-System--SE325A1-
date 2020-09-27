package se325.assignment01.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.UserDTO;
import se325.assignment01.concert.service.common.Config;
import se325.assignment01.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * This resource class contains the endpoint for logging in to the web service
 */

@Path("/concert-service/login")
public class LoginResource {

    private static Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    /**
     * This method is an endpoint that handles login requests. If a username and password matches one in the database,
     * ana authentication token, identifying the user is sent back for use in other endpoint calls. If the user does
     * not exist, a HTTP unauthorised response is returned.
     * @param user - UserDTO object containing the username and password for the login attempt
     * @return - A HTTP response containing the authentication token (if login was successful)
     */

    @POST
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response login(UserDTO user) {


        String username = user.getUsername();
        String password = user.getPassword();

        // check DTO was valid
        if ((username == null) || (password == null)){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        List<User> userResults = null;
        // get EntityManager for transaction
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            // Using optimistic locking due to potential for a user to login at the same time on 2 different devices
            // (only one should be successful). Locking is needed as the updated auth token associated with the user
            // is stored in the User object, hence there are writes that need protecting.
            TypedQuery<User> usersQuery = em.createQuery("select u from User u where u.username='" + username
                    + "' and u.password='" + password + "'", User.class)
                    .setLockMode(LockModeType.OPTIMISTIC);
            userResults = usersQuery.getResultList();




            if (userResults.isEmpty()) {
                // login failed
                em.getTransaction().rollback();
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            } else {
                // create new auth token for user
                NewCookie cookie = makeCookie();


                Response.ResponseBuilder builder = Response.ok();

                // add auth token to HTTP response
                builder.cookie(cookie);
                // persist record of cookie/token in user class in database
                userResults.get(0).setToken(cookie);
                em.merge(userResults.get(0));


                em.getTransaction().commit();

                return builder.build();

            }
        } finally {
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
