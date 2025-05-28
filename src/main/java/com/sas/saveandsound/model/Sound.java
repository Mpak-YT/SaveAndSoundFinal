package com.sas.saveandsound.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sounds")
public class Sound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE , CascadeType.PERSIST})
    @JoinTable(
            name = "sounds_creators",
            joinColumns = @JoinColumn(name = "sound_id"),
            inverseJoinColumns = @JoinColumn(name = "creator_id")
    )
    @JsonBackReference
    private Set<User> creators = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(name = "text", columnDefinition = "text")
    private String text;

    @Column(name = "date", columnDefinition = "date")
    private Date date;

    public Sound() {}

    public Sound(String name) {
        this.name = name;
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

    public Set<User> getCreators() {
        return creators;
    }

    public void setCreators(Set<User> creators) {
        this.creators = creators;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sound sound = (Sound) o;
        return id != null && id.equals(sound.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
