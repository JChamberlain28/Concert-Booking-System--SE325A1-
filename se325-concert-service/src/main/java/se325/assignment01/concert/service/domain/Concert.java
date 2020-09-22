package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.common.jackson.LocalDateTimeDeserializer;
import se325.assignment01.concert.common.jackson.LocalDateTimeSerializer;

@Entity
@Table(name ="CONCERTS")
public class Concert {

    // TODO Implement this class.

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE")
    private String title;
    @Column(name = "IMAGE_NAME")
    private String imageName;
    @Column(name = "BLURB", length = 2048)
    private String blurb;


    @ElementCollection
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>(); //TODO: should autogen CONCERT_DATES table (check tat hashset makes date the pk)


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}) // TODO: is it many to many, and oes cascade delete delete a performer even if it is also referd to by another concert
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns=@JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns=@JoinColumn(name = "PERFORMER_ID"))
    private Set<Performer> performers = new HashSet<>(); //TODO: change back to arraylist?
    public Concert() {
    }

    public Concert(Long id, String title, String imageName, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
    }

    public Concert(String title, String imageName) {
        this.title = title;
        this.imageName = imageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }


    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) { this.performers = performers; }




}
