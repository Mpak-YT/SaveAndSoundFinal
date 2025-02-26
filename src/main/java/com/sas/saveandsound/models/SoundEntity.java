package com.sas.saveandsound.models;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public SoundEntity(String name, String... creator) {
        this.name = name;
        this.creators = List.of(creator);  // Преобразуем массив в JSON-строку
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

    public List<String> getCreator() {
        return creators;
    }

    public void setCreator(String... creator) {
        this.creators = List.of(creator);
    }
/*
    @Override
    public String toString() {
        return "SoundEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creator=" + creators +
                '}';
    }
*/
}
