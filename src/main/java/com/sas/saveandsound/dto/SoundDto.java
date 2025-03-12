package com.sas.saveandsound.dto;

import com.sas.saveandsound.model.User;

import java.util.HashSet;
import java.util.Set;

public class SoundDto {

    private Long id;
    private String name;
    private Set<User> creators = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getCreators() {
        return creators;
    }

    public void setCreators(Set<User> creators) {
        this.creators = creators;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
