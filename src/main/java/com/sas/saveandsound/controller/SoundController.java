package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.service.SoundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SoundController {

    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @GetMapping("/search")
    public ResponseEntity<SoundDto> searchByName(@RequestParam(value = "name") String name) {
        SoundDto result = soundService.search(name);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/sound/{id}")
    public ResponseEntity<SoundDto> pathSong(@PathVariable long id) {
        SoundDto result = soundService.search(id);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

}