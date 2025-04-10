package com.sas.saveandsound.controller;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.service.SoundService;
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
import java.util.Map;



@RestController
@RequestMapping("/api/sounds")
@Tag(name = "Sound API", description = "API for managing sounds")
public class SoundController {

    private static final String SPECIAL_CHAR_PATTERN = "[\\n\\r\\t]";

    private static final Logger logger = LoggerFactory.getLogger(SoundController.class);

    private final SoundService soundService;

    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @Operation(summary = "Get all sounds",
               description = "Fetch all available sounds.")
    @GetMapping
    public ResponseEntity<List<SoundDto>> getAllSounds() {
        logger.info("Fetching all sounds...");
        List<SoundDto> sounds = soundService.getAllSounds();
        if (sounds.isEmpty()) {
            logger.warn("No sounds found.");
            throw new SoundNotFoundException("No sounds found.");
        }
        logger.info("Found {} sounds.", sounds.size());
        return ResponseEntity.ok(sounds);
    }

    @Operation(summary = "Get sound by ID",
               description = "Fetch a sound by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<SoundDto> getSoundById(
            @Parameter(description = "ID of the sound to fetch")
            @PathVariable long id
    ) {
        logger.info("Fetching sound with ID: {}", id);
        SoundDto sound = soundService.search(id);
        if (sound == null) {
            logger.error("Sound with ID {} not found.", id);
            throw new SoundNotFoundException("Sound with ID " + id + " not found.");
        }
        logger.info("Sound with ID {} retrieved successfully.", id);
        return ResponseEntity.ok(sound);
    }

    @Operation(summary = "Search sounds by name",
               description = "Search for sounds by their name.")
    @GetMapping("/search")
    public ResponseEntity<List<SoundDto>> searchByName(
            @Parameter(description = "Name of the sound to search for")
            @RequestParam(value = "name") String name
    ) {
        name = name.replaceAll(SPECIAL_CHAR_PATTERN, "_");
        logger.info("Searching for sounds with name: {}", name);
        List<SoundDto> results = soundService.search(name);
        if (results.isEmpty()) {
            logger.warn("No sounds found with the name '{}'.", name);
            throw new SoundNotFoundException("No sounds found with the name '" + name + "'.");
        }
        logger.info("Found {} sounds with the name '{}'.", results.size(), name);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get sounds by creator name",
               description = "Fetch all sounds created by a some creator.")
    @GetMapping("/by-creator")
    public ResponseEntity<List<SoundDto>> getSoundsByCreatorName(
            @Parameter(description = "Creator name associated with the sounds")
            @RequestParam String creatorName
    ) {
        creatorName = creatorName.replaceAll(SPECIAL_CHAR_PATTERN, "_");
        logger.info("Fetching sounds from {}", creatorName);
        List<SoundDto> sounds = soundService.getSoundsByUserName(creatorName);
        if (sounds.isEmpty()) {
            logger.warn("No sounds found for user '{}'.", creatorName);
            throw new SoundNotFoundException("No sounds found from " + creatorName + ".");
        }
        logger.info("Found {} sounds from {}.", sounds.size(), creatorName);
        return ResponseEntity.ok(sounds);
    }

    @Operation(summary = "Get sounds by album name",
               description = "Fetch all sounds associated with a specific album.")
    @GetMapping("/by-album")
    public ResponseEntity<List<SoundDto>> getSoundsByAlbumName(
            @Parameter(description = "Album name associated with the sounds")
            @RequestParam String albumName
    ) {
        albumName = albumName.replaceAll(SPECIAL_CHAR_PATTERN, "_");
        logger.info("Fetching sounds for album {}", albumName);
        List<SoundDto> sounds = soundService.getSoundsByAlbumName(albumName);
        if (sounds.isEmpty()) {
            logger.warn("No sounds found for album {}.", albumName);
            throw new SoundNotFoundException("No sounds found for album " + albumName + ".");
        }
        logger.info("Found {} sounds for album {}.", sounds.size(), albumName);
        return ResponseEntity.ok(sounds);
    }

    @Operation(summary = "Create a new sound",
            description = "Add a new sound with the provided details.")
    @PostMapping("/add")
    public ResponseEntity<SoundDto> createSound(
            @Valid @RequestBody Map<String, Object> soundData) {
        logger.info("Creating a new sound with data: {}", soundData);
        SoundDto createdSound = soundService.createSound(soundData);
        logger.info("Sound successfully created with ID: {}", createdSound.getId());
        return ResponseEntity.ok(createdSound);
    }

    @Operation(summary = "Update a sound",
            description = "Update an existing sound by its ID.")
    @PutMapping("/{id}")
    public ResponseEntity<SoundDto> updateSound(
            @Parameter(description = "ID of the sound to update") @PathVariable long id,
            @RequestBody Map<String, Object> soundData) {
        logger.info("Updating sound with ID: {} and data: {}", id, soundData);
        SoundDto updatedSound = soundService.updateSound(id, soundData);
        if (updatedSound == null) {
            logger.error("Failed to update sound with ID {}. Sound not found.", id);
            throw new SoundNotFoundException("Unable to update sound with ID " + id + ". Sound not found.");
        }
        logger.info("Sound with ID {} successfully updated.", id);
        return ResponseEntity.ok(updatedSound);
    }

    @Operation(summary = "Delete a sound by ID", description = "Delete a specific sound by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSound(
            @Parameter(description = "ID of the sound to delete") @PathVariable long id) {
        soundService.deleteSound(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all sounds", description = "Delete all sounds from the database.")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteAll() {
        soundService.deleteSounds();
        return ResponseEntity.noContent().build();
    }
}
