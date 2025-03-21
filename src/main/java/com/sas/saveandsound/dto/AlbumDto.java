package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class AlbumDto {

    private Long id;

    private String name;

    private Set<SoundDto> sounds = new HashSet<>();

    private String description;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<SoundDto> getSounds() {
        return sounds;
    }

    public void setSounds(Set<SoundDto> sounds) {
        this.sounds = sounds;
    }
}