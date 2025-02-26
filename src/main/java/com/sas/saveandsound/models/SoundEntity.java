package com.sas.saveandsound.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "sounds")
public class SoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "text", nullable = false)
    private String name;

    @ElementCollection
    @Column(name = "creators", columnDefinition = "text[]", nullable = false)
    private List<String> creators;

    public SoundEntity() {
    }

    public SoundEntity(String name, String... creators) {
        this.name = name;
        this.creators = List.of(creators);
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

    public List<String> getCreators() { // Исправлено
        return creators;
    }

    public void setCreators(List<String> creators) { // Исправлено
        this.creators = creators;
    }

}
