package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoundService {

    private final SoundDto[] sounds = new SoundDto[]{
        new SoundDto(1L, "name1", List.of("creator1", "creator2")),
        new SoundDto(2L, "name2", List.of("creator1", "creator3")),
        new SoundDto(3L, "name3", List.of("creator2"))
    };

    public SoundDto search(String query) {
        for (SoundDto sound : sounds) {
            if (sound.getName().equals(query) || query.isEmpty()) {
                return sound;
            }
        }
        return null;
    }
}
