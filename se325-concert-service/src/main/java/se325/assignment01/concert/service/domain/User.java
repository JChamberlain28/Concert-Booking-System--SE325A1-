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

    // versioning for optimistic locking
    @Version
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
        // using overridden equals() method in the User class
        if (booking == null){
            return false;
        }
        if (usersBookings.contains(booking)){
            return true;
        }
        return false;
    }



}
