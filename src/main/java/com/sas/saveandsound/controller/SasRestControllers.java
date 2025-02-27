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
@RequestMapping("/api/SAS")
public class SasRestControllers {

    private final SoundService soundService;

    public SasRestControllers(SoundService soundService) {
        this.soundService = soundService;
    }

    @GetMapping("/search")
    public ResponseEntity<SoundDto> requestName(@RequestParam(value = "name") String name) {
        return searchByName(name);
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<SoundDto> pathSong(@PathVariable String name) {
        return searchByName(name);
    }

    private ResponseEntity<SoundDto> searchByName(String name) {
        SoundDto result = soundService.search(name);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
}