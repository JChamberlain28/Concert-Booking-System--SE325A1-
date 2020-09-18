package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.common.jackson.LocalDateTimeDeserializer;
import se325.assignment01.concert.common.jackson.LocalDateTimeSerializer;

@Entity
public class Concert {

    // TODO Implement this class.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String imageName;
    private String blurb;


    @ElementCollection
    private Set<LocalDateTime> dates = new HashSet<>(); //TODO: should autogen CONCERT_DATES table (check tat hashset makes date the pk)

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}) // TODO: is it many to many, and oes cascade delete delete a performer even if it is also referd to by another concert
    private List<Performer> performers = new ArrayList<>();

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

    public List<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Performer> performers) {
        this.performers = performers;
    }



}
