package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class UserDto {

    private Long id;
    private String name;
    private boolean role;
    private Set<Long> soundIds = new HashSet<>();

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

    public boolean getRole() {
        return role;
    }

    public void setRole(boolean role) {
        this.role = role;
    }

    public Set<Long> getSoundIds() {
        return soundIds;
    }

    public void setSoundIds(Set<Long> soundIds) { // исправленный сеттер
        this.soundIds = soundIds;
    }
}
