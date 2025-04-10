package com.sas.saveandsound.dto;

import jakarta.validation.constraints.NotBlank;

public class AlbumNameDto {

    private Long id;

    @NotBlank(message = "Album name cannot be null, empty, or contain only spaces")
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