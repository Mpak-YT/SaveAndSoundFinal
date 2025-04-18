package com.sas.saveandsound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

public class SoundDto {
    private Long id;
    @NotEmpty(message = "Creators list cannot be empty")
    private Set<UserDto> creators = new HashSet<>();

    @NotBlank(message = "Sound name cannot be null, empty, or contain only spaces")
    @Size(max = 255, message = "Sound name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Album must be specified")
    private AlbumNameDto album = new AlbumNameDto();

    @NotNull(message = "Date cannot be null")
    private Date date;

    @NotBlank(message = "Text cannot be null, empty, or contain only spaces")
    private String text;

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

    public AlbumNameDto getAlbum() {
        return album;
    }

    public void setAlbum(AlbumNameDto album) {
        this.album = album;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date creationDate) {
        this.date = creationDate;
    }

    public Set<UserDto> getCreators() {
        return creators;
    }

    public void setCreators(Set<UserDto> creators) {
        this.creators = creators;
    }
}
