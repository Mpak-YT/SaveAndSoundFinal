package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.mapper.AlbumMapper;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.repository.AlbumRepository;
import com.sas.saveandsound.repository.SoundRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.HashSet; // Added import
import com.sas.saveandsound.exception.SoundNotFoundException; // New import

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final SoundRepository soundRepository;
    // Removed SoundMapper and SoundService as they are no longer directly used in AlbumService's updateAlbum logic

    public AlbumService(AlbumRepository albumRepository, SoundRepository soundRepository) {
        this.albumRepository = albumRepository;
        this.soundRepository = soundRepository;
    }

    public AlbumDto search(long id) {
        Album album = albumRepository.findById(id)
                                    .orElseThrow(() -> new AlbumNotFoundException("Album with ID " +
                                            id + " not found."));
        return AlbumMapper.toDto(album);
    }

    public List<AlbumDto> search(String name) {
        List<Album> albums = albumRepository.findByName(name);
        if (albums == null || albums.isEmpty()) {
            throw new AlbumNotFoundException("No albums found with the name '" + name + "'.");
        }
        return albums.stream().map(AlbumMapper::toDto).toList();
    }

    public AlbumDto createAlbum(AlbumDto albumDto) {
        Album album = AlbumMapper.toEntity(albumDto);
        Album saved = albumRepository.save(album);
        return AlbumMapper.toDto(saved);
    }

    public AlbumDto updateAlbum(long id, AlbumDto albumDto) {
        Album album = albumRepository.findById(id)
                                    .orElseThrow(() -> new AlbumNotFoundException("Album not found."));
        if (albumDto.getName() != null) {
            album.setName(albumDto.getName());
        }
        album.setDescription(albumDto.getDescription());

        // Update sounds association
        if (albumDto.getSounds() != null) {
            // Create a set of sounds that should be associated with the album based on the DTO
            Set<Sound> incomingSounds = new HashSet<>();
            for (SoundDto soundDtoItem : albumDto.getSounds()) {
                if (soundDtoItem.getId() == null) {
                    throw new IllegalArgumentException("Cannot add a new sound directly to an album. "
                        + "Please create the sound first.");
                }
                Sound sound = soundRepository.findById(soundDtoItem.getId())
                    .orElseThrow(() -> new SoundNotFoundException("Sound with ID " + soundDtoItem.getId() +
                            " not found."));
                incomingSounds.add(sound);
            }

            // Handle disassociations: remove sounds from the album's current collection
            // that are not in the incoming DTO, and set their album to null.
            // Iterate over a copy to avoid ConcurrentModificationException
            Set<Sound> soundsToDisassociate = new HashSet<>(album.getSounds());
            soundsToDisassociate.removeAll(incomingSounds); // These are the sounds to remove

            for (Sound sound : soundsToDisassociate) {
                sound.setAlbum(null); // Disassociate from this album
            }

            // Handle associations: add sounds from the incoming DTO to the album's collection
            // and set their album to this album.
            // Iterate over a copy to avoid ConcurrentModificationException
            Set<Sound> soundsToAssociate = new HashSet<>(incomingSounds);
            soundsToAssociate.removeAll(album.getSounds()); // These are the new sounds to add

            for (Sound sound : soundsToAssociate) {
                sound.setAlbum(album); // Associate with this album
            }

            // Finally, update the album's in-memory collection.
            // This is crucial for Hibernate to detect changes and cascade them.
            album.getSounds().clear();
            album.getSounds().addAll(incomingSounds);
        }

        // Save the album. Due to CascadeType.ALL, changes to the 'sounds' collection
        // and the 'album' field on associated Sound entities should be persisted.
        Album savedAlbum = albumRepository.save(album);

        // Explicitly reload the album to ensure its 'sounds' collection is fresh from the database
        // This is often necessary for inverse side collections in bidirectional relationships
        Album reloadedAlbum = albumRepository.findById(savedAlbum.getId())
                                            .orElseThrow(() -> new AlbumNotFoundException("Album with ID " +
                                                    savedAlbum.getId() + " not found after update."));

        return AlbumMapper.toDto(reloadedAlbum); // Use static method call
    }

    public void deleteAlbum(long id) {
        Album album = albumRepository.findById(id)
                                    .orElseThrow(() -> new AlbumNotFoundException("Album not found."));
        for (Sound sound : album.getSounds()) {
            sound.setAlbum(null);
        }
        soundRepository.saveAll(album.getSounds());
        albumRepository.delete(album);
    }

    public void deleteAllAlbums() {
        albumRepository.deleteAll();
    }

    public List<AlbumDto> getAllAlbumsDto() {
        List<Album> albums = albumRepository.findAll();
        if (albums.isEmpty()) throw new AlbumNotFoundException("No albums found.");
        return albums.stream().map(AlbumMapper::toDto).toList();
    }
}
