package com.sas.saveandsound.dto;

import java.util.List;

public class SoundDto {
    private Long id;
    private String name;
    private List<String> creators;

    public SoundDto() {
    }

    public SoundDto(Long id, String name, List<String> creators) {
        this.id = id;
        this.name = name;
        this.creators = creators;
    }

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

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }
}
