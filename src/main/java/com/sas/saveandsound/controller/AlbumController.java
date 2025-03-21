package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.service.AlbumService;
import com.sas.saveandsound.service.UserService;
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
    public ResponseEntity<List<AlbumNameDto>> getAllUsers() {
        List<AlbumNameDto> results = albumService.getAllAlbums();
        return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> getUserById(@PathVariable long id) {
        AlbumDto result = albumService.search(id);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AlbumDto>> searchByName(@RequestParam(value = "name") String name) {
        List<AlbumDto> result = albumService.search(name);
        return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public AlbumDto createAlbum(@RequestBody Map<String, Object> albumData) {
        return albumService.createAlbum(albumData);
    }

    @PutMapping("/{id}")
    public AlbumDto updateAlbum(@PathVariable long id, @RequestBody Map<String, Object> albumData) {
        return albumService.updateAlbum(id, albumData);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        albumService.deleteAlbum(id);
    }

    @DeleteMapping("")
    public void deleteAll() {
        albumService.deleteAllAlbums();
    }
}