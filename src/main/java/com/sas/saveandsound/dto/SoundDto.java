package com.sas.saveandsound.dto;

import com.sas.saveandsound.model.Album;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

public class SoundDto {
    private Long id;
    private String name;
    private Set<UserDto> creators = new HashSet<>();
    private Album album = new Album();
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

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
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
