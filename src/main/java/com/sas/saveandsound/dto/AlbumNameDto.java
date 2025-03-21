package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class AlbumNameDto {

    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }
}