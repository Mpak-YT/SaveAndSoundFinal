package com.sas.saveandsound.dto;

import java.util.HashSet;
import java.util.Set;

public class CreatorDto extends UserDto {

    private Set<SoundDto> sounds = new HashSet<>();

    public Set<SoundDto> getSounds() {
        return sounds;
    }

    public void setSounds(Set<SoundDto> sounds) {
        this.sounds = sounds;
    }
}
