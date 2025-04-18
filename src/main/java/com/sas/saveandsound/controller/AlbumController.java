package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/albums")
@Tag(name = "Album API", description = "API for managing albums")
public class AlbumController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @Operation(summary = "Get all albums", description = "Fetch all available albums.")
    @GetMapping
    public ResponseEntity<List<AlbumNameDto>> getAllAlbums() {
        logger.info("Fetching all albums...");
        List<AlbumNameDto> albums = albumService.getAllAlbums();
        if (albums.isEmpty()) {
            logger.warn("No albums found.");
            throw new AlbumNotFoundException("No albums found.");
        }
        logger.info("Found {} albums.", albums.size());
        return ResponseEntity.ok(albums);
    }

    @Operation(summary = "Get album by ID", description = "Fetch an album by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> getAlbumById(
            @Parameter(description = "ID of the album to fetch") @PathVariable long id) {
        logger.info("Fetching album with ID: {}", id);
        AlbumDto album = albumService.search(id);
        if (album == null) {
            logger.error("Album with ID {} not found.", id);
            throw new AlbumNotFoundException("Album with ID " + id + " not found.");
        }
        logger.info("Album with ID {} retrieved successfully.", id);
        return ResponseEntity.ok(album);
    }

    @Operation(summary = "Search albums by name", description = "Search for albums by name.")
    @GetMapping("/search")
    public ResponseEntity<List<AlbumDto>> searchByName(
            @Parameter(description = "Name of the album to search for")
            @RequestParam(value = "name") String name) {
        logger.info("Searching for albums with name: {}", name);
        List<AlbumDto> albums = albumService.search(name);
        if (albums.isEmpty()) {
            logger.warn("No albums found with the name '{}'.", name);
            throw new AlbumNotFoundException("No albums found with the name '" + name + "'.");
        }
        logger.info("Found {} albums with the name '{}'.", albums.size(), name);
        return ResponseEntity.ok(albums);
    }

    @Operation(summary = "Create a new album", description = "Add a new album with the provided details.")
    @PostMapping("/add")
    public ResponseEntity<AlbumDto> createAlbum(
            @Valid @RequestBody AlbumDto albumDto) {
        logger.info("Creating a new album with data: {}", albumDto);
        AlbumDto createdAlbum = albumService.createAlbum(albumDto);
        logger.info("Album successfully created with ID: {}", createdAlbum.getId());
        return ResponseEntity.ok(createdAlbum);
    }

    @Operation(summary = "Update an album", description = "Update an existing album by its ID.")
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDto> updateAlbum(
            @Parameter(description = "ID of the album to update") @PathVariable long id,
            @RequestBody AlbumDto albumDto) {
        logger.info("Updating album with ID: {} and data: {}", id, albumDto);
        AlbumDto updatedAlbum = albumService.updateAlbum(id, albumDto);
        if (updatedAlbum == null) {
            logger.error("Failed to update album with ID {}. Album not found.", id);
            throw new AlbumNotFoundException("Unable to update album with ID " + id + ". Album not found.");
        }
        logger.info("Album with ID {} successfully updated.", id);
        return ResponseEntity.ok(updatedAlbum);
    }

    @Operation(summary = "Delete an album by ID", description = "Delete a specific album by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(
            @Parameter(description = "ID of the album to delete") @PathVariable long id) {
        logger.info("Deleting album with ID: {}", id);
        albumService.deleteAlbum(id);
        logger.info("Album with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all albums", description = "Delete all albums from the database.")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteAllAlbums() {
        logger.info("Deleting all albums...");
        albumService.deleteAllAlbums();
        logger.info("All albums deleted successfully.");
        return ResponseEntity.noContent().build();
    }
}

