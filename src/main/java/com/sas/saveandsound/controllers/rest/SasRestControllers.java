package com.sas.saveandsound.controllers.rest;

import com.sas.saveandsound.models.SoundEntity;
import com.sas.saveandsound.service.SoundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/SAS")
public class SasRestControllers {

    private final SoundService soundService;

    public SasRestControllers(SoundService soundService) {
        this.soundService = soundService;
    }

    // GET-запрос с Query Parameters: /SAS/search?name=SongTitle
    @GetMapping("/search")
    public ResponseEntity<SoundEntity> requestName(@RequestParam(value = "name") String name) {
        SoundEntity result = soundService.search(name);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    // GET-запрос с Path Parameters: /SAS/search/SongTitle
    @GetMapping("/search/{name}")
    public ResponseEntity<SoundEntity> pathSong(@PathVariable String name) {
        SoundEntity result = soundService.search(name);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
}
