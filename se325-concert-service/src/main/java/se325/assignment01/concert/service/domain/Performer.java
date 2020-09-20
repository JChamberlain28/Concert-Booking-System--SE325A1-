package se325.assignment01.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.common.types.Genre;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name ="PERFORMERS")
public class Performer implements Comparable<Performer> {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;
    @Column(name = "IMAGE_NAME")
    private String imageName;
    @Column(name = "GENRE")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Column(name = "BLURB", length = 2048)
    private String blurb;

    @ManyToMany(mappedBy = "performers")
    //private Set<Concert> concerts = new HashSet<>();
    private List<Concert> concerts = new ArrayList<>();

    public Performer() {
    }

    public Performer(Long id, String name, String imageName, Genre genre, String blurb) {
        this.id = id;
        this.name = name;
        this.imageName = imageName;
        this.genre = genre;
        this.blurb = blurb;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public List<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(List<Concert> concerts) {
        this.concerts = concerts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Performer that = (Performer) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(imageName, that.imageName)
                .append(genre, that.genre)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(imageName)
                .append(genre)
                .toHashCode();
    }

    @Override
    public int compareTo(Performer other) {
        return other.getName().compareTo(getName());
    }


}
