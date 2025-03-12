package com.sas.saveandsound.dto;

import com.sas.saveandsound.model.Sound;

import java.util.HashSet;
import java.util.Set;

public class UserDto {

    private Long id;
    private String name;
    private boolean role;
    private Set<SoundDto> sounds = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SoundDto> getSounds() {
        return sounds;
    }

    public void setSounds(Set<SoundDto> sounds) {
        this.sounds = sounds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }
}
