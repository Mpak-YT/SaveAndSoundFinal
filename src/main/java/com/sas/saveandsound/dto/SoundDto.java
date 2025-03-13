package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class SoundDto {
    private Long id;
    private String name;
    private Set<Long> creatorIds = new HashSet<>();

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

    public Set<Long> getCreatorIds() {
        return creatorIds;
    }

    public void setCreatorIds(Set<Long> creatorIds) {
        this.creatorIds = creatorIds;
    }

}
