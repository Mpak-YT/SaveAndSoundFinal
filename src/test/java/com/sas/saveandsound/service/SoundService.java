package com.sas.saveandsound.service;

import com.sas.saveandsound.cashe.SoundCache;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.SoundNotFoundException;
import com.sas.saveandsound.model.Album;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    @Mock // Добавьте эти моки в начало класса, если их еще нет
    private UserRepository userRepository;

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

    // ==================== Добавить в SoundServiceTest ====================



    // --- Тесты для getSoundsByUserName ---


    @Test
    @DisplayName("getSoundsByUserName: обрабатывает спецсимволы в имени")
    void getSoundsByUserName_HandlesSpecialChars() {
        // Arrange
        String userNameWithChars = "User\nWith\tChars";
        String sanitizedUserName = "User_With_Chars"; // Ожидаемое санитайзером
        Sound sound = new Sound(); sound.setName("Special Char Sound");
        List<Sound> sounds = List.of(sound);

        when(soundCache.containsKey(sanitizedUserName)).thenReturn(true); // Попадание в кэш с санитайз. именем
        when(soundCache.get(sanitizedUserName)).thenReturn(sounds);

        // Act
        List<SoundDto> result = soundService.getSoundsByUserName(userNameWithChars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(soundCache).containsKey(sanitizedUserName);
        verify(soundCache).get(sanitizedUserName);
        verifyNoInteractions(soundRepository); // Репозиторий не должен вызываться
    }


    // --- Тесты для getSoundsByAlbumName ---


    @Test
    @DisplayName("getSoundsByAlbumName: обрабатывает спецсимволы в имени")
    void getSoundsByAlbumName_HandlesSpecialChars() {
        // Arrange
        String albumNameWithChars = "Album\nWith\tChars";
        String sanitizedAlbumName = "Album_With_Chars";
        Sound sound = new Sound(); sound.setName("Special Char Album Sound");
        List<Sound> sounds = List.of(sound);

        when(soundCache.containsKey(sanitizedAlbumName)).thenReturn(true);
        when(soundCache.get(sanitizedAlbumName)).thenReturn(sounds);

        // Act
        List<SoundDto> result = soundService.getSoundsByAlbumName(albumNameWithChars);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(soundCache).containsKey(sanitizedAlbumName);
        verify(soundCache).get(sanitizedAlbumName);
        verifyNoInteractions(soundRepository);
    }


    // --- Тесты для createSound ---


    @Test
    @DisplayName("createSound: выбрасывает исключение, если создатель не найден")
    void createSound_Throws_WhenCreatorNotFound() {
        // Arrange
        UserDto nonExistentCreatorDto = new UserDto(); nonExistentCreatorDto.setId(99L); // Несуществующий ID
        SoundDto inputDto = new SoundDto();
        inputDto.setName("Sound With Bad Creator");
        inputDto.setCreators(Set.of(nonExistentCreatorDto));

        // Мокируем userRepository, чтобы он вернул null
        when(userRepository.findById(99L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> soundService.createSound(inputDto));
        assertTrue(exception.getMessage().contains("Creator with ID 99 not found"));

        verify(userRepository).findById(99L);
        verify(soundRepository, never()).save(any()); // Сохранение звука не должно быть вызвано
    }


    @Test @DisplayName("createSound: обрабатывает пустой сет создателей")
    void createSound_HandlesEmptyCreatorSet() {
        SoundDto inputDto = new SoundDto(); inputDto.setName("NoCreators"); inputDto.setCreators(Collections.emptySet());
        Sound savedSound = new Sound(); savedSound.setId(15L); savedSound.setName("NoCreators"); savedSound.setCreators(new HashSet<>());
        when(soundRepository.save(any(Sound.class))).thenReturn(savedSound);

        SoundDto result = soundService.createSound(inputDto);

        assertNotNull(result);
        assertEquals("NoCreators", result.getName());
        assertTrue(result.getCreators() == null || result.getCreators().isEmpty()); // Проверяем пустоту
    }


    // --- Тесты для updateSound ---

    @Test
    @DisplayName("updateSound: выбрасывает исключение, если звук для обновления не найден")
    void updateSound_Throws_WhenSoundNotFound() {
        // Arrange
        SoundDto updateDto = new SoundDto(); updateDto.setName("Update Non Existent");
        when(soundRepository.findById(99L)).thenReturn(null); // Звук не найден

        // Act & Assert
        assertThrows(SoundNotFoundException.class, () -> soundService.updateSound(99L, updateDto));
        verify(soundRepository).findById(99L);
        verify(soundRepository, never()).save(any());
    }


    @Test
    @DisplayName("updateSound: выбрасывает исключение при попытке установить несуществующего создателя")
    void updateSound_Throws_WhenCreatorNotFound() {
        // Arrange
        UserDto nonExistentCreatorDto = new UserDto(); nonExistentCreatorDto.setId(99L);
        SoundDto updateDto = new SoundDto(); updateDto.setName("Sound With Bad Creator Update");
        updateDto.setCreators(Set.of(nonExistentCreatorDto)); // Пытаемся установить несуществующего создателя

        Sound existingSound = new Sound(); existingSound.setId(1L); existingSound.setName("Old Sound");

        when(soundRepository.findById(1L)).thenReturn(existingSound); // Звук для обновления найден
        // Мокируем поиск создателя, возвращаем null
        when(userRepository.findById(99L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> soundService.updateSound(1L, updateDto));
        assertTrue(exception.getMessage().contains("Creator with ID 99 not found"));

        verify(soundRepository).findById(1L);
        verify(userRepository).findById(99L);
        verify(soundRepository, never()).save(any());
    }



    @Test
    @DisplayName("updateSound: выбрасывает исключение, если имя звука null или пустое")
    void updateSound_Throws_WhenNameIsNullOrEmpty() {
        SoundDto updateDtoWithNull = new SoundDto(); updateDtoWithNull.setName(null);
        SoundDto updateDtoWithEmpty = new SoundDto(); updateDtoWithEmpty.setName("   ");
        Sound existingSound = new Sound(); existingSound.setId(1L);

        when(soundRepository.findById(1L)).thenReturn(existingSound);

        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> soundService.updateSound(1L, updateDtoWithNull));
        assertEquals("Sound name cannot be null, empty, or contain only spaces", exNull.getMessage());

        IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class, () -> soundService.updateSound(1L, updateDtoWithEmpty));
        assertEquals("Sound name cannot be null, empty, or contain only spaces", exEmpty.getMessage());

        verify(soundRepository, times(2)).findById(1L); // Вызывался дважды
        verify(soundRepository, never()).save(any());
    }


    // --- Тесты для deleteSound ---

    @Test
    @DisplayName("deleteSound: выбрасывает исключение, если звук не найден")
    void deleteSound_Throws_WhenNotFound() {
        when(soundRepository.findById(99L)).thenReturn(null); // Звук не найден
        assertThrows(SoundNotFoundException.class, () -> soundService.deleteSound(99L));
        verify(soundRepository).findById(99L);
        verify(soundRepository, never()).delete(any());
    }


    // --- Дополнительные тесты для покрытия ---
}


