package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;


@Entity
@Table(name ="CONCERTS")
public class Concert {

    @Id
    private Long id;

    @Column(name = "TITLE")
    private String title;
    @Column(name = "IMAGE_NAME")
    private String imageName;
    @Column(name = "BLURB", length = 2048)
    private String blurb;


    @ElementCollection
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>();

    // dont need to cascade remove/persist as the API doesnt support removing or adding a performer
    @ManyToMany
    // defines the table linking Concert and Performer, as specified in db-init.sql
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns=@JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns=@JoinColumn(name = "PERFORMER_ID"))
    private Set<Performer> performers = new HashSet<>();
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
