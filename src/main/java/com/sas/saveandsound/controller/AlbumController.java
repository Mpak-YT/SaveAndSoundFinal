package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.service.AlbumService;
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
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumNameDto>> getAllAlbums() {
        List<AlbumNameDto> results = albumService.getAllAlbums();
        if (results.isEmpty()) {
            throw new AlbumNotFoundException("No albums found.");
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> getAlbumById(@PathVariable long id) {
        AlbumDto result = albumService.search(id);
        if (result == null) {
            throw new AlbumNotFoundException("Album with ID " + id + " not found.");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AlbumDto>> searchByName(@RequestParam(value = "name") String name) {
        List<AlbumDto> result = albumService.search(name);
        if (result == null || result.isEmpty()) {
            throw new AlbumNotFoundException("No albums found with the name '" + name + "'.");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody Map<String, Object> albumData) {
        AlbumDto createdAlbum = albumService.createAlbum(albumData);
        return ResponseEntity.ok(createdAlbum);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumDto> updateAlbum(@PathVariable long id,
                                                @RequestBody Map<String, Object> albumData) {
        AlbumDto updatedAlbum = albumService.updateAlbum(id, albumData);
        if (updatedAlbum == null) {
            throw new AlbumNotFoundException("Unable to update album with ID " + id + ". Album not found.");
        }
        return ResponseEntity.ok(updatedAlbum);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteAllAlbums() {
        albumService.deleteAllAlbums();
        return ResponseEntity.noContent().build();
    }
}
