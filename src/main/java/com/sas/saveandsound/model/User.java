package com.sas.saveandsound.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "role", columnDefinition = "boolean", nullable = false)
    private boolean role = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sounds_creators",
            joinColumns = @JoinColumn(name = "creator_id"),
            inverseJoinColumns = @JoinColumn(name = "sound_id")
    )
    @JsonManagedReference
    private Set<Sound> sounds = new HashSet<>();

    @Column(name = "email", columnDefinition = "text")
    private String email;

    @Column(name = "nickname", columnDefinition = "text")
    private String nickname;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}
