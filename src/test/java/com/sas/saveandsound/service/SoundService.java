package com.sas.saveandsound.service;

import com.sas.saveandsound.cashe.SoundCache;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.SoundRepository;
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
class SoundServiceTest {

    @Mock
    private SoundRepository soundRepository;

    @Mock
    private SoundCache soundCache;

    @InjectMocks
    private SoundService soundService;

    @Test
    void testGetAllSounds_ReturnsSoundDtoList_WhenSoundsExist() {
        // Arrange
        Sound sound = new Sound();
        sound.setName("Sound 1");
        when(soundRepository.findAll()).thenReturn(List.of(sound));

        // Act
        List<SoundDto> result = soundService.getAllSounds();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sound 1", result.getFirst().getName());
        verify(soundRepository, times(1)).findAll();
    }

    @Test
    void testGetAllSounds_ThrowsSoundNotFoundException_WhenNoSoundsExist() {
        // Arrange
        when(soundRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, soundService::getAllSounds);
        assertEquals("No sounds found.", exception.getMessage());
        verify(soundRepository, times(1)).findAll();
    }

    @Test
    void testSearchById_ReturnsSoundDto_WhenSoundExists() {
        // Arrange
        Sound sound = new Sound();
        sound.setName("Sound by ID");
        when(soundRepository.findById(1L)).thenReturn(sound);

        // Act
        SoundDto result = soundService.search(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Sound by ID", result.getName());
        verify(soundRepository, times(1)).findById(1L);
    }

    @Test
    void testSearchById_ThrowsSoundNotFoundException_WhenSoundDoesNotExist() {
        // Arrange
        when(soundRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, () -> soundService.search(1L));
        assertEquals("Sound with ID 1 not found.", exception.getMessage());
        verify(soundRepository, times(1)).findById(1L);
    }

    @Test
    void testSearchByName_ReturnsSoundDtoList_WhenSoundsExist() {
        // Arrange
        Sound sound = new Sound();
        sound.setName("Test Sound");
        when(soundRepository.findByName("Test Sound")).thenReturn(List.of(sound));

        // Act
        List<SoundDto> result = soundService.search("Test Sound");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Sound", result.getFirst().getName());
        verify(soundRepository, times(1)).findByName("Test Sound");
    }

    @Test
    void testSearchByName_ThrowsSoundNotFoundException_WhenNoSoundsExist() {
        // Arrange
        when(soundRepository.findByName("Unknown Sound")).thenReturn(List.of());

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, () -> soundService.search("Unknown Sound"));
        assertEquals("No sounds found with the name 'Unknown Sound'.", exception.getMessage());
        verify(soundRepository, times(1)).findByName("Unknown Sound");
    }

    @Test
    void testGetSoundsByUserName_ReturnsSoundDtoList_WhenCacheHit() {
        // Arrange
        String userName = "TestUser";
        String sanitizedUserName = "TestUser";
        Sound sound = new Sound();
        sound.setName("Cached Sound");

        when(soundCache.containsKey(sanitizedUserName)).thenReturn(true);
        when(soundCache.get(sanitizedUserName)).thenReturn(List.of(sound));

        // Act
        List<SoundDto> result = soundService.getSoundsByUserName(userName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cached Sound", result.getFirst().getName());
        verify(soundCache, times(1)).containsKey(sanitizedUserName);
        verify(soundCache, times(1)).get(sanitizedUserName);
    }

    @Test
    void testGetSoundsByUserName_ThrowsSoundNotFoundException_WhenNoSoundsExistInCacheOrDatabase() {
        // Arrange
        String userName = "NonexistentUser";
        String sanitizedUserName = "NonexistentUser";

        when(soundCache.containsKey(sanitizedUserName)).thenReturn(false);
        when(soundRepository.findSoundsByUserNameJPQL(userName)).thenReturn(List.of());

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, () -> soundService.getSoundsByUserName(userName));
        assertEquals("No sounds found from NonexistentUser.", exception.getMessage());
        verify(soundRepository, times(1)).findSoundsByUserNameJPQL(userName);
    }

    @Test
    void testCreateSound_SuccessfullyCreatesSound() {
        // Arrange
        SoundDto soundDto = new SoundDto();
        soundDto.setName("New Sound");

        Sound savedSound = new Sound();
        savedSound.setName("New Sound");

        // Настраиваем мок для репозитория
        when(soundRepository.save(any(Sound.class))).thenReturn(savedSound);

        // Act
        SoundDto result = soundService.createSound(soundDto);

        // Assert
        assertNotNull(result);
        assertEquals("New Sound", result.getName());
        verify(soundRepository, times(1)).save(any(Sound.class));
    }





    @Test
    void testUpdateSound_SuccessfullyUpdatesSound() {
        // Arrange
        Sound existingSound = new Sound();
        existingSound.setName("Old Sound");

        SoundDto soundDto = new SoundDto();
        soundDto.setName("Updated Sound");
        soundDto.setAlbum(null); // Указываем, что альбом не передаётся

        when(soundRepository.findById(1L)).thenReturn(existingSound);
        when(soundRepository.save(any(Sound.class))).thenReturn(existingSound);

        // Act
        SoundDto result = soundService.updateSound(1L, soundDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Sound", result.getName());
        verify(soundRepository, times(1)).findById(1L);
        verify(soundRepository, times(1)).save(any(Sound.class));
    }


    @Test
    void testDeleteSound_SuccessfullyDeletesSound() {
        // Arrange
        Sound sound = new Sound();

        when(soundRepository.findById(1L)).thenReturn(sound);

        // Act
        soundService.deleteSound(1L);

        // Assert
        verify(soundRepository, times(1)).findById(1L);
        verify(soundRepository, times(1)).delete(sound);
    }

    @Test
    void testDeleteSounds_SuccessfullyDeletesAllSounds() {
        // Act
        soundService.deleteSounds();

        // Assert
        verify(soundRepository, times(1)).deleteAll();
    }

    @Test
    void testCreateSound_ThrowsIllegalArgumentException_WhenNameIsNull() {
        // Arrange
        SoundDto soundDto = new SoundDto(); // Создаём DTO без имени

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> soundService.createSound(soundDto));
        assertEquals("Sound name cannot be null, empty, or contain only spaces", exception.getMessage());
    }

    @Test
    void testGetSoundsByUserName_ThrowsException_WhenNoResults() {
        // Arrange
        String userName = "UnknownUser";

        when(soundCache.containsKey(userName)).thenReturn(false);
        when(soundRepository.findSoundsByUserNameJPQL(userName)).thenReturn(List.of());

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, () -> soundService.getSoundsByUserName(userName));
        assertEquals("No sounds found from UnknownUser.", exception.getMessage());
        verify(soundRepository, times(1)).findSoundsByUserNameJPQL(userName);
    }

    @Test
    void testCreateSound_HandlesNullCreatorsAndAlbum() {
        // Arrange
        SoundDto soundDto = new SoundDto();
        soundDto.setName("Sound Without Creators");
        soundDto.setCreators(null); // Creators отсутствуют
        soundDto.setAlbum(null); // Album отсутствует

        Sound savedSound = new Sound();
        savedSound.setName("Sound Without Creators");
        savedSound.setCreators(null); // Эмулируем отсутствие creators
        savedSound.setAlbum(null); // Эмулируем отсутствие album

        when(soundRepository.save(any(Sound.class))).thenReturn(savedSound);

        // Act
        SoundDto result = soundService.createSound(soundDto);

        // Assert
        assertNotNull(result);
        assertEquals("Sound Without Creators", result.getName());
        assertNull(result.getCreators());
        assertNull(result.getAlbum());
    }

    @Test
    void testUpdateSound_AllowsNullCreatorsAndAlbum() {
        // Arrange
        SoundDto soundDto = new SoundDto();
        soundDto.setName("Updated Sound");
        soundDto.setCreators(null); // Creators отсутствуют
        soundDto.setAlbum(null); // Album отсутствует

        Sound existingSound = new Sound();
        existingSound.setName("Existing Sound");
        existingSound.setCreators(Set.of(new User())); // Изначально поле Creators заполнено
        existingSound.setAlbum(new Album()); // Изначально поле Album заполнено

        when(soundRepository.findById(1L)).thenReturn(existingSound);
        when(soundRepository.save(any(Sound.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Возвращаем сохранённый объект

        // Act
        SoundDto result = soundService.updateSound(1L, soundDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Sound", result.getName());

        // Проверка, что поле Creators осталось неизменённым
        assertEquals(1, result.getCreators().size(), "Creators должен остаться неизменённым, если null передан в SoundDto");

        verify(soundRepository, times(1)).findById(1L);
        verify(soundRepository, times(1)).save(any(Sound.class));
    }





    @Test
    void testDeleteSounds_CallsDeleteAll_WhenThereAreNoSounds() {
        // Arrange
        doNothing().when(soundRepository).deleteAll();

        // Act
        soundService.deleteSounds();

        // Assert
        verify(soundRepository, times(1)).deleteAll();
    }

    @Test
    void testGetSoundsByAlbumName_ReturnsSoundDtoList_WhenCacheHit() {
        // Arrange
        String albumName = "AlbumName";
        String sanitizedAlbumName = albumName.replaceAll("[\\n\\r\\t]", "_");
        Sound sound = new Sound();
        sound.setName("Cached Sound");

        when(soundCache.containsKey(sanitizedAlbumName)).thenReturn(true);
        when(soundCache.get(sanitizedAlbumName)).thenReturn(List.of(sound));

        // Act
        List<SoundDto> result = soundService.getSoundsByAlbumName(albumName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cached Sound", result.getFirst().getName());
        verify(soundCache, times(1)).containsKey(sanitizedAlbumName);
        verify(soundCache, times(1)).get(sanitizedAlbumName);
    }

    @Test
    void testGetSoundsByAlbumName_ThrowsException_WhenNoSoundsExistInCacheOrDatabase() {
        // Arrange
        String albumName = "NonexistentAlbum";
        String sanitizedAlbumName = albumName.replaceAll("[\\n\\r\\t]", "_");

        when(soundCache.containsKey(sanitizedAlbumName)).thenReturn(false);
        when(soundRepository.findSoundsByAlbumNameNative(albumName)).thenReturn(List.of());

        // Act & Assert
        SoundNotFoundException exception = assertThrows(SoundNotFoundException.class, () -> soundService.getSoundsByAlbumName(albumName));
        assertEquals("No sounds found for album NonexistentAlbum.", exception.getMessage());
        verify(soundRepository, times(1)).findSoundsByAlbumNameNative(albumName);
    }



}
