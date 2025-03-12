package com.sas.saveandsound.common;

import com.sas.saveandsound.model.User;

import java.util.HashSet;
import java.util.Set;

public abstract class SoundBase {
    protected Long id;
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
}
