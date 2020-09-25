package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.ws.rs.core.Cookie;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name ="USERS")
public class User {


    private String username;
    private String password;

    @Id
    private Long id;


    private int version;

    @Column(name = "TOKEN")
    private String userToken;

    @OneToMany
    Set<Booking> usersBookings = new HashSet<>();

    protected User() {
    }

    public User(Long id, String username, String password, int version) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    // not following java beans but dont need to as this is not a DTO, so abstracting token check and set

    public void setToken(Cookie token){
        userToken = token.getValue();
    }

    public void addUserBooking(Booking booking){
        this.usersBookings.add(booking);
    }

    public Set<Booking> getUserBookings(){
        return this.usersBookings;
    }

    public boolean hasBooking(Booking booking){
        for (Booking b : usersBookings){
            if (b.getId().equals(booking.getId())){
                return true;
            }
        }
        return false;
    }


//    // returns true if the provided the cookie token is associated with this user
//    public boolean tokenMatch(Cookie token){
//        if (userToken == null){
//            return false;
//        }
//        return userToken.equals(token.getValue());
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new EqualsBuilder()
                .append(username, user.username)
                .append(password, user.password)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(username)
                .append(password)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
