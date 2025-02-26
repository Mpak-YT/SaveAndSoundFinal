package com.sas.saveandsound.service;

import com.sas.saveandsound.models.SoundEntity;
import org.springframework.stereotype.Service;

@Service
public class SoundService {

    private final SoundEntity[] sounds = new SoundEntity[]{
        new SoundEntity("a1", "A1", "A2"),
        new SoundEntity("a1", "B2"),
        new SoundEntity("b1", "A1")
    };

    public SoundEntity search(String query) {
        for (SoundEntity sound : sounds) {
            if (sound.getName().equals(query) || query.isEmpty()) {
                return sound;
            }
        }
        return null;
    }
}
