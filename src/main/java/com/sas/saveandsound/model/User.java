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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "text", nullable = false)
    private String name;

    @Column(name = "role", columnDefinition = "boolean", nullable = false)
    private boolean role = false;

    @ManyToMany
    @JoinTable(
            name = "sounds_creators",
            joinColumns = @JoinColumn(name = "creator_id"),
            inverseJoinColumns = @JoinColumn(name = "sound_id")
    )
    private Set<Sound> sounds = new HashSet<>();

    public User() {}

    public User(String name, boolean role) {
        this.name = name;
        this.role = role;
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

    public boolean getRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }

    public Set<Sound> getSounds() {
        return sounds;
    }

    public void setSounds(Set<Sound> sounds) {
        this.sounds = sounds;
    }
}
