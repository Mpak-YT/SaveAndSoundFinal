package com.sas.saveandsound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

public class SoundDto {
    private Long id;

    private Set<UserDto> creators = new HashSet<>();

    @NotBlank(message = "Sound name cannot be null, empty, or contain only spaces")
    @Size(min = 2, max = 100, message = "Sound name must be between 2 and 100 characters")
    private String name;

    private AlbumNameDto album = new AlbumNameDto();

    @PastOrPresent(message = "Date cannot be in the future")
    private Date date;

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
