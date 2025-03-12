package com.sas.saveandsound.model;

import com.sas.saveandsound.common.SoundBase;
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
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sounds")
public class Sound extends SoundBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public Long getId() {
        return super.getId(); // Получение id из базового класса
    }

    @Column(name = "name", columnDefinition = "text", nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sounds_creators",
            joinColumns = @JoinColumn(name = "sound_id"),
            inverseJoinColumns = @JoinColumn(name = "creator_id")
    )
    private Set<User> creators = new HashSet<>();

}


