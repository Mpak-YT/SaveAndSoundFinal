package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.dto.AlbumNameDto;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.AlbumNotFoundException;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private SoundService soundService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void testGetAllAlbums_ReturnsAlbumNameDtoList_WhenAlbumsExist() {
        // Arrange
        Album album = new Album();
        album.setName("Test Album");
        when(albumRepository.findAll()).thenReturn(List.of(album));

        // Act
        List<AlbumNameDto> result = albumService.getAllAlbums();

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals(1, result.size(), "The size of the result should be 1.");
        assertEquals("Test Album", result.getFirst().getName(), "The album name should match.");
        verify(albumRepository, times(1)).findAll();
    }

    @Test
    void testGetAllAlbums_ThrowsAlbumNotFoundException_WhenNoAlbumsExist() {
        // Arrange
        when(albumRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        AlbumNotFoundException exception = assertThrows(AlbumNotFoundException.class, () -> albumService.getAllAlbums());
        assertEquals("No albums found.", exception.getMessage(), "Exception message should match.");
        verify(albumRepository, times(1)).findAll();
    }

    @Test
    void testSearchAlbumById_ReturnsAlbumDto_WhenAlbumExists() {
        // Arrange
        Album album = new Album();
        album.setName("Test Album");
        when(albumRepository.findById(1L)).thenReturn(album);

        // Act
        AlbumDto result = albumService.search(1L);

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals("Test Album", result.getName(), "The album name should match.");
        verify(albumRepository, times(1)).findById(1L);
    }

    @Test
    void testSearchAlbumById_ThrowsAlbumNotFoundException_WhenAlbumDoesNotExist() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        AlbumNotFoundException exception = assertThrows(AlbumNotFoundException.class, () -> albumService.search(1L));
        assertEquals("Album with ID 1 not found.", exception.getMessage(), "Exception message should match.");
        verify(albumRepository, times(1)).findById(1L);
    }

    @Test
    void testSearchAlbumByName_ReturnsAlbumDtoList_WhenAlbumsExist() {
        // Arrange
        Album album = new Album();
        album.setName("Test Album");
        when(albumRepository.findByName("Test Album")).thenReturn(List.of(album));

        // Act
        List<AlbumDto> result = albumService.search("Test Album");

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals(1, result.size(), "The size of the result should be 1.");
        assertEquals("Test Album", result.getFirst().getName(), "The album name should match.");
        verify(albumRepository, times(1)).findByName("Test Album");
    }

    @Test
    void testSearchAlbumByName_ThrowsAlbumNotFoundException_WhenNoAlbumsExist() {
        // Arrange
        when(albumRepository.findByName("Nonexistent Album")).thenReturn(List.of());

        // Act & Assert
        AlbumNotFoundException exception = assertThrows(AlbumNotFoundException.class, () -> albumService.search("Nonexistent Album"));
        assertEquals("No albums found with the name 'Nonexistent Album'.", exception.getMessage(), "Exception message should match.");
        verify(albumRepository, times(1)).findByName("Nonexistent Album");
    }

    @Test
    void testCreateAlbum_SuccessfullyCreatesAlbum() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName("New Album");

        Album album = new Album();
        album.setName("New Album");

        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumDto result = albumService.createAlbum(albumDto);

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals("New Album", result.getName(), "The album name should match.");
        verify(albumRepository, times(1)).save(any(Album.class));
    }

    @Test
    void testUpdateAlbum_SuccessfullyUpdatesAlbum_WhenAlbumExists() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName("Updated Album");

        Album album = new Album();
        when(albumRepository.findById(1L)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);

        // Act
        AlbumDto result = albumService.updateAlbum(1L, albumDto);

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals("Updated Album", result.getName(), "The album name should match.");
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).save(album);
    }

    @Test
    void testDeleteAlbum_SuccessfullyDeletesAlbum_WhenAlbumExists() {
        // Arrange
        Album album = new Album();
        when(albumRepository.findById(1L)).thenReturn(album);

        // Act
        albumService.deleteAlbum(1L);

        // Assert
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).delete(album);
    }

    @Test
    void testDeleteAllAlbums_SuccessfullyDeletesAllAlbums() {
        // Act
        albumService.deleteAllAlbums();

        // Assert
        verify(albumRepository, times(1)).deleteAll();
    }

    @Test
    void testCreateAlbum_HandlesNullSounds() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName("Album Without Sounds");

        Album album = new Album();
        album.setName("Album Without Sounds");

        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumDto result = albumService.createAlbum(albumDto);

        // Assert
        assertNotNull(result);
        assertEquals("Album Without Sounds", result.getName());
        verify(albumRepository, times(1)).save(any(Album.class));
    }

    @Test
    void testCreateAlbum_HandlesEmptyName() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName(null); // Пустое имя

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> albumService.createAlbum(albumDto));
        assertEquals("Album name cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testCreateAlbum_WithSounds_SuccessfullyCreatesAlbum() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName("Album with Sounds");
        albumDto.setSounds(Set.of(new SoundDto())); // Добавляем звуки в DTO

        Album album = new Album();
        album.setName("Album with Sounds");

        // Возвращаем SoundDto из мока soundService
        SoundDto mockSoundDto = new SoundDto();
        mockSoundDto.setId(1L);
        when(soundService.createSound(any(SoundDto.class))).thenReturn(mockSoundDto);

        // Настраиваем возврат альбома
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        // Act
        AlbumDto result = albumService.createAlbum(albumDto);

        // Assert
        assertNotNull(result, "Result should not be null.");
        assertEquals("Album with Sounds", result.getName(), "The album name should match.");
        verify(soundService, times(1)).createSound(any(SoundDto.class)); // Проверяем вызов soundService
        verify(albumRepository, times(1)).save(any(Album.class)); // Проверяем вызов репозитория
    }





    @Test
    void testCreateAlbum_ThrowsException_WhenNameIsEmpty() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName(""); // Пустая строка

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> albumService.createAlbum(albumDto));
        assertEquals("Album name cannot be null or empty.", exception.getMessage(), "Exception message should match.");
    }

    @Test
    void testUpdateAlbum_WithPartialFields_SuccessfullyUpdates() {
        // Arrange
        AlbumDto albumDto = new AlbumDto();
        albumDto.setName("Updated Name"); // Частичное обновление
        albumDto.setDescription(null); // Отсутствует описание

        Album existingAlbum = new Album();
        existingAlbum.setName("Old Name");
        existingAlbum.setDescription("Old Description");

        when(albumRepository.findById(1L)).thenReturn(existingAlbum);
        when(albumRepository.save(existingAlbum)).thenReturn(existingAlbum);

        // Act
        AlbumDto result = albumService.updateAlbum(1L, albumDto);

        // Assert
        assertNotNull(result, "The result should not be null.");
        assertEquals("Updated Name", result.getName());
        assertNull(existingAlbum.getDescription(), "The description should remain null.");
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).save(existingAlbum);
    }

    @Test
    void testDeleteAllAlbums_NoAlbumsExist() {
        // Arrange
        doNothing().when(albumRepository).deleteAll();

        // Act
        albumService.deleteAllAlbums();

        // Assert
        verify(albumRepository, times(1)).deleteAll();
    }



}
