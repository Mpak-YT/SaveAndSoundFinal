package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class CreatorDto {

    private Long id;
    private String name;
    private Set<SoundDto> sounds = new HashSet<>();
    private String email;
    private String nickname;

    public long getId() {
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

    public Set<SoundDto> getSounds() {
        return sounds;
    }

    public void setSounds(Set<SoundDto> sounds) { // исправленный сеттер
        this.sounds = sounds;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
