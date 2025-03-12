package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class SoundDto {
    private Long id;
    private String name;
    private Set<UserDto> creators = new HashSet<>();

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

    public Set<UserDto> getCreators() {
        return creators;
    }

    public void setCreators(Set<UserDto> creators) {
        this.creators = creators;
    }
}
