package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.service.SoundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sounds")
public class SoundController {

    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @GetMapping
    public ResponseEntity<List<SoundDto>> getAllSounds() {
        List<SoundDto> results = soundService.getAllSounds();
        if (results.isEmpty()) {
            throw new SoundNotFoundException("No sounds found.");
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoundDto> getSoundById(@PathVariable long id) {
        SoundDto result = soundService.search(id);
        if (result == null) {
            throw new SoundNotFoundException("Sound with ID " + id + " not found.");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SoundDto>> searchByName(@RequestParam(value = "name") String name) {
        List<SoundDto> results = soundService.search(name);
        if (results.isEmpty()) {
            throw new SoundNotFoundException("No sounds found with the name '" + name + "'.");
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-user")
    public ResponseEntity<List<SoundDto>> getSoundsByUserName(@RequestParam String userName) {
        List<SoundDto> results = soundService.getSoundsByUserName(userName);
        if (results.isEmpty()) {
            throw new SoundNotFoundException("No sounds found for user '" + userName + "'.");
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-album")
    public ResponseEntity<List<SoundDto>> getSoundsByAlbumName(@RequestParam String albumName) {
        List<SoundDto> results = soundService.getSoundsByAlbumName(albumName);
        if (results.isEmpty()) {
            throw new SoundNotFoundException("No sounds found for album '" + albumName + "'.");
        }
        return ResponseEntity.ok(results);
    }

    @PostMapping("/add")
    public ResponseEntity<SoundDto> createSound(@RequestBody Map<String, Object> soundData) {
        SoundDto createdSound = soundService.createSound(soundData);
        return ResponseEntity.ok(createdSound);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoundDto> updateSound(@PathVariable long id,
                                                @RequestBody Map<String, Object> soundData) {
        SoundDto updatedSound = soundService.updateSound(id, soundData);
        if (updatedSound == null) {
            throw new SoundNotFoundException("Unable to update sound with ID " + id + ". Sound not found.");
        }
        return ResponseEntity.ok(updatedSound);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSound(@PathVariable long id) {
        soundService.deleteSound(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteAll() {
        soundService.deleteSounds();
        return ResponseEntity.noContent().build();
    }
}
