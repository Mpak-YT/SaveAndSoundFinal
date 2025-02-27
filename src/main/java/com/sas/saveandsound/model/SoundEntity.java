package com.sas.saveandsound.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sounds")
public class SoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "text", nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "sounds_creators",
            joinColumns = @JoinColumn(name = "sound_id"),
            inverseJoinColumns = @JoinColumn(name = "creator_id")
    )
    private Set<CreatorEntity> creators = new HashSet<>();

    public SoundEntity() {}

    public SoundEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CreatorEntity> getCreators() {
        return creators;
    }

    public void addCreator(CreatorEntity creator) {
        this.creators.add(creator);
        creator.getSounds().add(this);
    }

    public void removeCreator(CreatorEntity creator) {
        this.creators.remove(creator);
        creator.getSounds().remove(this);
    }
}
