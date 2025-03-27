package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.SoundDto;
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
        return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoundDto> getSoundById(@PathVariable long id) {
        SoundDto result = soundService.search(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SoundDto>> searchByName(@RequestParam(value = "name") String name) {
        List<SoundDto> results = soundService.search(name);
        return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results);
    }

    @GetMapping("/by-user")
    public List<SoundDto> getSoundsByUserName(@RequestParam String userName) {
        return soundService.getSoundsByUserName(userName);
    }

    @GetMapping("/by-album")
    public List<SoundDto> getSoundsByAlbumName(@RequestParam String albumName) {
        return soundService.getSoundsByAlbumName(albumName);
    }

    @PostMapping("/add")
    public SoundDto createSound(@RequestBody Map<String, Object> soundData) {
        return soundService.createSound(soundData);
    }

    @PutMapping("/{id}")
    public SoundDto updateSound(@PathVariable long id, @RequestBody Map<String, Object> soundData) {
        return soundService.updateSound(id, soundData);
    }

    @DeleteMapping("/{id}")
    public void deleteSound(@PathVariable long id) {
        soundService.deleteSound(id);
    }

    @DeleteMapping("")
    public void deleteAll() {
        soundService.deleteSounds();
    }
}