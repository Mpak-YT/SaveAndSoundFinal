package com.sas.saveandsound.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("CREATOR")
public class CreatorEntity extends UserEntity {

    @ManyToMany(mappedBy = "creators")
    private Set<SoundEntity> sounds = new HashSet<>();

    // Конструктор по умолчанию
    public CreatorEntity() {
        super(); // Вызов конструктора родительского класса
        setPosition("CREATOR"); // Устанавливаем роль "CREATOR"
    }

    // Конструктор с параметрами
    public CreatorEntity(String name) {
        super(name); // Вызов конструктора родительского класса с указанием имени
        setPosition("CREATOR"); // Устанавливаем роль как "CREATOR"
    }

    public Set<SoundEntity> getSounds() {
        return sounds;
    }

    public void setSounds(Set<SoundEntity> sounds) {
        this.sounds = sounds;
    }
}
