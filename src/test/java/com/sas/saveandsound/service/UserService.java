package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.CreatorDto;
import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.dto.UserDto;
import com.sas.saveandsound.exception.UserNotFoundException;
// Убедитесь, что мапперы существуют и доступны
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.mapper.UserMapper;
import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// Для имитации статических мапперов, если нужно
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat; // Для удобных проверок коллекций
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*; // Используем статический импорт

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserService (v7 - Полная версия, БЕЗ Optional в моках)")
class UserServiceTest { // Изменено имя класса

    // --- ВАЖНОЕ ЗАМЕЧАНИЕ ---
    // ВСЕ тесты в этом классе мокируют findById так, будто он возвращает Entity/null.
    // Тесты для removeCreatorAndDeleteOrphanSound НЕ СООТВЕТСТВУЮТ коду сервиса и @Disabled.
    // Успех тестов updateUser со звуками ЗАВИСИТ от ИСПРАВЛЕНИЯ NPE В UserService.
    // --- КОНЕЦ ЗАМЕЧАНИЯ ---

    @Mock private UserRepository userRepository;
    @Mock private SoundService soundService;
    // @Mock private SoundMapper soundMapper; // Если бин

    @InjectMocks private UserService userService;

    @Captor private ArgumentCaptor<User> userCaptor;

    // Тестовые данные
    private User regularUser;
    private User creatorUser;
    private Sound sound1;
    private Sound sound2;
    private SoundDto soundDto1;
    private SoundDto soundDto2;

    // Константы для ID
    private static final long REGULAR_USER_ID = 1L;
    private static final long CREATOR_USER_ID = 3L;
    private static final long SOUND1_ID = 10L;
    private static final long SOUND2_ID = 20L;
    private static final long NEW_SOUND_ID_1 = 30L;
    private static final long NEW_SOUND_ID_2 = 40L;

    // --- Вспомогательный метод для имитации SoundMapper.toEntity ---
    // Возвращает null, если dto или dto.id null.
    private Sound mockMapToEntity(SoundDto dto) {
        if (dto == null || dto.getId() == null) return null;
        Sound entity = new Sound();
        entity.setId(dto.getId()); entity.setName(dto.getName()); entity.setCreators(new HashSet<>());
        return entity;
    }

    @BeforeEach
    void setUp() {
        regularUser = new User(); regularUser.setId(REGULAR_USER_ID); regularUser.setName("Regular User"); regularUser.setRole(false); regularUser.setSounds(new HashSet<>());
        creatorUser = new User(); creatorUser.setId(CREATOR_USER_ID); creatorUser.setName("Creator User"); creatorUser.setRole(true);
        sound1 = new Sound(); sound1.setId(SOUND1_ID); sound1.setName("Sound One");
        sound2 = new Sound(); sound2.setId(SOUND2_ID); sound2.setName("Sound Two");
        sound1.setCreators(new HashSet<>(Set.of(creatorUser)));
        sound2.setCreators(new HashSet<>(Set.of(creatorUser)));
        creatorUser.setSounds(new HashSet<>(Set.of(sound1, sound2)));

        // Статические мапперы предполагаются рабочими для создания DTO
        soundDto1 = SoundMapper.toDto(sound1);
        soundDto2 = SoundMapper.toDto(sound2);
    }

    // ==================== GET ALL USERS ====================
    @Nested @DisplayName("getAllUsers") class GetAllUsers {
        @Test @DisplayName("успешно возвращает список DTO") void shouldReturnUserList() {
            when(userRepository.findAll()).thenReturn(List.of(regularUser, creatorUser));
            List<UserDto> result = userService.getAllUsers();
            assertThat(result).hasSize(2);
            verify(userRepository).findAll();
        }
        @Test @DisplayName("выбрасывает исключение, если список пуст") void shouldThrowException_WhenNoUsersFound() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            assertThrows(UserNotFoundException.class, () -> userService.getAllUsers());
            verify(userRepository).findAll();
        }
    }
    // ==================== SEARCH USER BY ID ====================
    @Nested @DisplayName("searchUser (by ID)") class SearchUserById {
        @Test @DisplayName("успешно находит и возвращает UserDto") void shouldReturnUserDto_WhenRegularUserFound() {
            when(userRepository.findById(1L)).thenReturn(regularUser); // Entity/null
            UserDto result = userService.searchUser(1L);
            assertEquals("Regular User", result.getName());
            verify(userRepository).findById(1L);
        }
        @Test @DisplayName("успешно находит и возвращает CreatorDto") void shouldReturnCreatorDto_WhenCreatorFound() {
            when(userRepository.findById(3L)).thenReturn(creatorUser); // Entity/null
            UserDto result = userService.searchUser(3L);
            assertInstanceOf(CreatorDto.class, result);
            assertEquals("Creator User", result.getName());
            verify(userRepository).findById(3L);
        }
        @Test @DisplayName("выбрасывает исключение, если пользователь не найден") void shouldThrowException_WhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(null); // Entity/null
            assertThrows(UserNotFoundException.class, () -> userService.searchUser(99L));
            verify(userRepository).findById(99L);
        }
    }
    // ==================== SEARCH USER BY NAME ====================
    @Nested @DisplayName("search (by Name)") class SearchUserByName {
        @Test @DisplayName("успешно находит и возвращает UserDto") void shouldReturnUserDto_WhenRegularUserFound() {
            when(userRepository.findByName("Regular User")).thenReturn(regularUser); // Entity/null
            UserDto result = userService.search("Regular User");
            assertEquals("Regular User", result.getName());
            verify(userRepository).findByName("Regular User");
        }
        @Test @DisplayName("успешно находит и возвращает CreatorDto") void shouldReturnCreatorDto_WhenCreatorFound() {
            when(userRepository.findByName("Creator User")).thenReturn(creatorUser); // Entity/null
            UserDto result = userService.search("Creator User");
            assertEquals("Creator User", result.getName());
            verify(userRepository).findByName("Creator User");
        }
        @Test @DisplayName("выбрасывает исключение, если пользователь не найден") void shouldThrowException_WhenUserNotFound() {
            when(userRepository.findByName("Unknown")).thenReturn(null); // Entity/null
            assertThrows(UserNotFoundException.class, () -> userService.search("Unknown"));
            verify(userRepository).findByName("Unknown");
        }
    }
    // ==================== CREATE USER ====================
    @Nested @DisplayName("createUser") class CreateUser {
        @Test @DisplayName("успешно создает обычного пользователя") void shouldCreateRegularUser() {
            UserDto inputDto = new UserDto(); inputDto.setName("NewReg"); inputDto.setRole(false);
            User savedUser = new User(); savedUser.setId(5L); savedUser.setName("NewReg"); savedUser.setRole(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0); u.setId(5L); return u;
            });
            UserDto result = userService.createUser(inputDto);
            assertEquals(5L, result.getId());
            verify(userRepository).save(any(User.class));
            verifyNoInteractions(soundService);
        }
        @Test @DisplayName("успешно создает создателя без звуков") void shouldCreateCreatorWithoutSounds() {
            CreatorDto inputDto = new CreatorDto(); inputDto.setName("NewCre"); inputDto.setRole(true); inputDto.setSounds(Collections.emptySet());
            User savedUser = new User(); savedUser.setId(6L); savedUser.setName("NewCre"); savedUser.setRole(true); savedUser.setSounds(new HashSet<>());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0); u.setId(6L); return u;
            });
            UserDto result = userService.createUser(inputDto);
            assertEquals(6L, result.getId());
            assertTrue(result instanceof CreatorDto && ((CreatorDto) result).getSounds().isEmpty());
            verify(userRepository).save(any(User.class));
            verifyNoInteractions(soundService);
        }
        @Test @DisplayName("успешно создает создателя с новыми звуками")
        void shouldCreateCreatorWithNewSounds() {
            SoundDto newSoundDto1 = new SoundDto(); newSoundDto1.setName("NS1");
            CreatorDto inputDto = new CreatorDto(); inputDto.setName("CreWithSounds"); inputDto.setRole(true);
            inputDto.setSounds(Set.of(newSoundDto1));
            SoundDto createdDto1 = new SoundDto(); createdDto1.setId(NEW_SOUND_ID_1); createdDto1.setName("NS1");
            when(soundService.createSound(newSoundDto1)).thenReturn(createdDto1);
            User savedUser = new User(); savedUser.setId(7L); savedUser.setName("CreWithSounds"); savedUser.setRole(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0); u.setId(7L); return u;
            });
            // Имитируем РАБОЧИЙ SoundMapper.toEntity
            try (MockedStatic<SoundMapper> mockedMapper = Mockito.mockStatic(SoundMapper.class)) {
                Sound soundEntity = new Sound(); soundEntity.setId(NEW_SOUND_ID_1);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(createdDto1))).thenReturn(soundEntity);
                UserDto result = userService.createUser(inputDto); // Вызов внутри мока
                assertEquals(7L, result.getId());
                assertInstanceOf(CreatorDto.class, result);
            }
            verify(soundService).createSound(newSoundDto1);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(1);
        }
        @Test @DisplayName("Создание пользователя с null в DTO") void testCreateUser_WithNullFieldsInDto() {
            UserDto inputDto = new UserDto(); inputDto.setRole(false);
            User savedUser = new User(); savedUser.setId(11L); savedUser.setRole(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0); u.setId(11L); return u;
            });
            UserDto result = userService.createUser(inputDto);
            assertEquals(11L, result.getId());
            assertNull(result.getName());
            verify(userRepository).save(any(User.class));
            verifyNoInteractions(soundService);
        }

        @Test @DisplayName("Создание создателя с null звуком в сете DTO")
        void shouldCreateCreator_WithOneNullSoundInDtoSet() {
            SoundDto validSoundDto = new SoundDto(); validSoundDto.setName("ValidSound");
            CreatorDto inputDto = new CreatorDto(); inputDto.setName("CreatorWithNullSound"); inputDto.setRole(true);
            Set<SoundDto> soundsWithNull = new HashSet<>(); soundsWithNull.add(validSoundDto); soundsWithNull.add(null);
            inputDto.setSounds(soundsWithNull);
            SoundDto createdDto = new SoundDto(); createdDto.setId(NEW_SOUND_ID_1); createdDto.setName("ValidSound");
            when(soundService.createSound(validSoundDto)).thenReturn(createdDto);
            User savedUser = new User(); savedUser.setId(12L); savedUser.setName("CreatorWithNullSound"); savedUser.setRole(true);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            // Имитируем РАБОЧИЙ SoundMapper.toEntity
            try (var mockedMapper = mockStatic(SoundMapper.class)) {
                Sound soundEntity = new Sound(); soundEntity.setId(NEW_SOUND_ID_1);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(createdDto))).thenReturn(soundEntity);
                // Имитируем, что маппер вернет null для null DTO
                mockedMapper.when(() -> SoundMapper.toEntity(isNull())).thenReturn(null);
                UserDto result = userService.createUser(inputDto);
                assertEquals(12L, result.getId());
            }
            verify(soundService, times(1)).createSound(validSoundDto);
            verify(soundService, never()).createSound(isNull());
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(1); // null отфильтрован
        }
        @Test @DisplayName("Выбрасывает исключение, если UserMapper.toEntity возвращает null")
        void createUser_ThrowsWhenMapperReturnsNull() {
            UserDto inputDto = new UserDto();
            try (var mockedMapper = mockStatic(UserMapper.class)) {
                mockedMapper.when(() -> UserMapper.toEntity(eq(inputDto))).thenReturn(null);
                assertThrows(IllegalArgumentException.class, () -> userService.createUser(inputDto));
            }
            verifyNoInteractions(userRepository, soundService);
        }
    }

    // ==================== UPDATE USER (ожидает Entity/null от findById) ====================
    @Nested @DisplayName("updateUser (ожидает Entity/null от findById)")
    class UpdateUser {

        // --- Тесты, которые ДОЛЖНЫ проходить, если сервис ИСПРАВЛЕН (NPE и SoundMapper) ---
        @Test
        @DisplayName("успешно обновляет создателя с обновлением существующего звука")
        void shouldUpdateCreator_WithExistingSoundUpdate() {
            // Arrange
            SoundDto updatedSound1Dto = new SoundDto();
            updatedSound1Dto.setId(SOUND1_ID);
            updatedSound1Dto.setName("UpdatedName");

            CreatorDto updateDto = new CreatorDto();
            updateDto.setName("UpdCreSound");
            updateDto.setRole(true);
            updateDto.setSounds(Set.of(updatedSound1Dto, soundDto2)); // Обновляем 1, оставляем 2 DTO

            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.updateSound(eq(SOUND1_ID), any(SoundDto.class))).thenReturn(updatedSound1Dto); // Мок обновления звука
            when(soundService.updateSound(eq(SOUND2_ID), any(SoundDto.class))).thenReturn(soundDto2); // Мок "обновления" второго звука
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Мок сохранения юзера

            // Статический мок для SoundMapper.toEntity
            try (MockedStatic<SoundMapper> mockedMapper = Mockito.mockStatic(SoundMapper.class)) {
                Sound mappedSound1 = mockMapToEntity(updatedSound1Dto);
                Sound mappedSound2 = sound2; // Используем существующий
                mockedMapper.when(() -> SoundMapper.toEntity(any(SoundDto.class))).thenReturn(mappedSound1, mappedSound2);

                // Act
                UserDto result = userService.updateUser(3L, updateDto);

                // Assert
                assertEquals("UpdCreSound", result.getName());
                verify(userRepository).findById(3L);
                verify(soundService).updateSound(eq(SOUND1_ID), any(SoundDto.class));
                verify(soundService).updateSound(eq(SOUND2_ID), any(SoundDto.class));
                verify(userRepository).save(userCaptor.capture());

                assertThat(userCaptor.getValue().getSounds()).hasSize(2);
                assertThat(userCaptor.getValue().getSounds())
                        .extracting(Sound::getName)
                        .containsExactlyInAnyOrder("UpdatedName", "Sound Two");
            }
        }

        @Test @DisplayName("успешно обновляет создателя С добавлением нового звука")
        void shouldUpdateCreator_WithAddedSound() {
            SoundDto newSoundDto = new SoundDto(); newSoundDto.setName("New S3");
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("CreAddSound"); updateDto.setRole(true);
            updateDto.setSounds(Set.of(soundDto1, soundDto2, newSoundDto));
            SoundDto createdDto = new SoundDto(); createdDto.setId(NEW_SOUND_ID_1); createdDto.setName("New S3");
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.createSound(any(SoundDto.class))).thenReturn(createdDto); // Мок создания
            when(soundService.updateSound(SOUND1_ID, soundDto1)).thenReturn(soundDto1);
            when(soundService.updateSound(SOUND2_ID, soundDto2)).thenReturn(soundDto2);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            // Имитируем РАБОЧИЙ SoundMapper.toEntity
            try (MockedStatic<SoundMapper> mockedMapper = Mockito.mockStatic(SoundMapper.class)) {
                Sound mappedNew = mockMapToEntity(createdDto);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(createdDto))).thenReturn(mappedNew);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(soundDto1))).thenReturn(sound1);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(soundDto2))).thenReturn(sound2);
                // Act & Assert
                UserDto result = userService.updateUser(3L, updateDto);
                assertEquals("CreAddSound", result.getName());
            }
            verify(userRepository).findById(3L);
            verify(soundService).createSound(any(SoundDto.class));
            verify(soundService, times(2)).updateSound(anyLong(), any(SoundDto.class));
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(3);
        }

        @Test @DisplayName("Обновление создателя: удаление одного звука")
        void shouldUpdateCreator_RemovingOneSound() {
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("UserRemSound"); updateDto.setRole(true);
            updateDto.setSounds(Set.of(soundDto1)); // Оставляем только sound1
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.updateSound(SOUND1_ID, soundDto1)).thenReturn(soundDto1); // Вызов для sound1
            // updateSound для sound2 НЕ вызывается
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            // Имитируем маппер
            try (MockedStatic<SoundMapper> mockedMapper = Mockito.mockStatic(SoundMapper.class)) {
                mockedMapper.when(() -> SoundMapper.toEntity(eq(soundDto1))).thenReturn(sound1);
                // Act & Assert
                UserDto result = userService.updateUser(3L, updateDto);
                assertEquals("UserRemSound", result.getName());
            }
            verify(userRepository).findById(3L);
            verify(soundService).updateSound(SOUND1_ID, soundDto1);
            verify(soundService, never()).updateSound(eq(SOUND2_ID), any());
            verify(soundService, never()).createSound(any());
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(1); // Остался только sound1
        }

        @Test @DisplayName("Обновление создателя: замена всех звуков")
        void shouldUpdateCreator_ReplacingAllSounds() {
            // Arrange
            SoundDto newDtoA = new SoundDto(); newDtoA.setName("New A");
            SoundDto newDtoB = new SoundDto(); newDtoB.setName("New B");
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("UserReplaceSound"); updateDto.setRole(true);
            updateDto.setSounds(Set.of(newDtoA, newDtoB)); // Только новые

            SoundDto createdDtoA = new SoundDto(); createdDtoA.setId(NEW_SOUND_ID_1); createdDtoA.setName("New A");
            SoundDto createdDtoB = new SoundDto(); createdDtoB.setId(NEW_SOUND_ID_2); createdDtoB.setName("New B");

            when(userRepository.findById(3L)).thenReturn(creatorUser); // У него были sound1 и sound2

            // --- ИСПРАВЛЕННЫЕ УСТОЙЧИВЫЕ МОКИ для createSound ---
            when(soundService.createSound(argThat(s -> s != null && "New A".equals(s.getName()))))
                    .thenReturn(createdDtoA);
            when(soundService.createSound(argThat(s -> s != null && "New B".equals(s.getName()))))
                    .thenReturn(createdDtoB);
            // ----------------------------------------------------

            // updateSound НЕ должен вызываться для старых звуков
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Имитируем маппер
            try (var mockedMapper = mockStatic(SoundMapper.class)) {
                Sound mappedNewA = mockMapToEntity(createdDtoA);
                Sound mappedNewB = mockMapToEntity(createdDtoB);
                // Важно мокировать для DTO, которые возвращает soundService
                mockedMapper.when(() -> SoundMapper.toEntity(eq(createdDtoA))).thenReturn(mappedNewA);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(createdDtoB))).thenReturn(mappedNewB);

                // Act & Assert
                // Ожидаем успеха ПОСЛЕ исправления сервиса, если там были NPE
                assertDoesNotThrow(() -> {
                    UserDto result = userService.updateUser(3L, updateDto);
                    assertEquals("UserReplaceSound", result.getName());
                });
            }

            verify(userRepository).findById(3L);
            // Проверяем, что createSound был вызван ДВАЖДЫ с ЛЮБЫМ SoundDto
            verify(soundService, times(2)).createSound(any(SoundDto.class));
            // Убеждаемся, что были вызовы с конкретными DTO (если argThat сработал)
            verify(soundService, times(1)).createSound(newDtoA);
            verify(soundService, times(1)).createSound(newDtoB);
            verify(soundService, never()).updateSound(anyLong(), any()); // update не вызывался
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(2);
            assertThat(userCaptor.getValue().getSounds()).extracting(Sound::getId).containsOnly(NEW_SOUND_ID_1, NEW_SOUND_ID_2);

        }

        @Test @DisplayName("Обновление: soundService.updateSound возвращает null")
        void shouldHandleNullFromUpdateSound() {
            SoundDto updateDto1 = new SoundDto(); updateDto1.setId(SOUND1_ID);
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("UpdateFail"); updateDto.setRole(true);
            updateDto.setSounds(Set.of(updateDto1)); // Обновляем только sound1
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.updateSound(eq(SOUND1_ID), any())).thenReturn(null); // Сервис вернул null
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            // Имитируем маппер
            try (MockedStatic<SoundMapper> mockedMapper = Mockito.mockStatic(SoundMapper.class)) {
                // !!! Важно: имитируем возврат null от маппера при null DTO !!!
                mockedMapper.when(() -> SoundMapper.toEntity(isNull())).thenReturn(null);
                // Act & Assert
                assertDoesNotThrow(() -> userService.updateUser(3L, updateDto));
            }
            verify(userRepository).findById(3L);
            verify(soundService).updateSound(eq(SOUND1_ID), any());
            verify(userRepository).save(userCaptor.capture());
            // Звук должен был отфильтроваться (если сервис исправлен)
            assertThat(userCaptor.getValue().getSounds()).isEmpty();
        }

        @Test @DisplayName("Обновление: soundService.createSound возвращает null")
        void shouldHandleNullFromCreateSound() {
            SoundDto newDto = new SoundDto(); newDto.setName("TryCreate");
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("CreateFail"); updateDto.setRole(true);
            updateDto.setSounds(Set.of(newDto)); // Добавляем только новый
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.createSound(any())).thenReturn(null); // Сервис вернул null
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            // Имитируем маппер
            try (var mockedMapper = mockStatic(SoundMapper.class)) {
                mockedMapper.when(() -> SoundMapper.toEntity(isNull())).thenReturn(null);
                assertDoesNotThrow(() -> userService.updateUser(3L, updateDto));
            }
            verify(userRepository).findById(3L);
            verify(soundService).createSound(any());
            verify(userRepository).save(userCaptor.capture());
            // Звук должен был отфильтроваться (если сервис исправлен)
            assertThat(userCaptor.getValue().getSounds()).isEmpty();
        }

        @Test @DisplayName("Обновление создателя с null списком звуков в DTO")
        void shouldUpdateCreator_WithNullSoundListDto() {
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("Updated Creator Null Sounds"); updateDto.setRole(true); updateDto.setSounds(null);
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UserDto result = userService.updateUser(3L, updateDto);
            assertEquals("Updated Creator Null Sounds", result.getName());
            verify(userRepository).findById(3L);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).hasSize(2); // Звуки не изменились
            verifyNoInteractions(soundService);
        }

        @Test @DisplayName("Обновление создателя со списком звуков, содержащим null")
        void shouldUpdateCreator_WithNullInSoundListDto() {
            SoundDto updatedSound1Dto = new SoundDto(); updatedSound1Dto.setId(SOUND1_ID); updatedSound1Dto.setName("Updated S1");
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("UpdWithNullSound"); updateDto.setRole(true);
            Set<SoundDto> soundsWithNull = new HashSet<>(); soundsWithNull.add(updatedSound1Dto); soundsWithNull.add(null);
            updateDto.setSounds(soundsWithNull);
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(soundService.updateSound(eq(SOUND1_ID), any(SoundDto.class))).thenReturn(updatedSound1Dto);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            // Имитируем маппер
            try (var mockedMapper = mockStatic(SoundMapper.class)) {
                Sound mappedSound1 = mockMapToEntity(updatedSound1Dto);
                mockedMapper.when(() -> SoundMapper.toEntity(eq(updatedSound1Dto))).thenReturn(mappedSound1);
                mockedMapper.when(() -> SoundMapper.toEntity(isNull())).thenReturn(null); // Для null DTO
                assertDoesNotThrow(() -> userService.updateUser(3L, updateDto));
            }
            verify(userRepository).findById(3L);
            verify(soundService).updateSound(eq(SOUND1_ID), any(SoundDto.class)); // Вызван для валидного
            verify(userRepository).save(userCaptor.capture());
            // null должен был отфильтроваться
            assertThat(userCaptor.getValue().getSounds()).hasSize(1);
            assertThat(userCaptor.getValue().getSounds()).extracting(Sound::getId).containsOnly(SOUND1_ID);
        }

        @Test @DisplayName("Обновление пользователя с null полями (устойчивость)")
        void testUpdateUser_WithNullFieldsInDto() {
            UserDto updateDto = new UserDto(); updateDto.setRole(true); // Меняем роль user1
            when(userRepository.findById(1L)).thenReturn(regularUser);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            UserDto result = userService.updateUser(1L, updateDto);
            assertNotNull(result);
            assertTrue(result.getRole());
            assertEquals(regularUser.getName(), result.getName()); // Имя не изменилось
            verify(userRepository).findById(1L);
            verify(userRepository).save(any(User.class));
            verifyNoInteractions(soundService);
        }

        // --- Рабочие тесты updateUser ---
        @Test @DisplayName("успешно обновляет данные обычного пользователя")
        void shouldUpdateRegularUser() {
            UserDto updateDto = new UserDto(); updateDto.setName("Updated Regular"); updateDto.setRole(false);
            when(userRepository.findById(1L)).thenReturn(regularUser);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UserDto result = userService.updateUser(1L, updateDto);
            assertEquals("Updated Regular", result.getName());
            verify(userRepository).findById(1L);
            verify(userRepository).save(any(User.class));
            verifyNoInteractions(soundService);
        }
        @Test @DisplayName("обновляет создателя, очищая звуки (пустой sounds в DTO)")
        void shouldUpdateCreator_ClearSounds_WhenDtoSoundsEmpty() {
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("UpdCreEmptyS"); updateDto.setRole(true); updateDto.setSounds(Collections.emptySet());
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UserDto result = userService.updateUser(3L, updateDto);
            assertEquals("UpdCreEmptyS", result.getName());
            verify(userRepository).findById(3L);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getSounds()).isEmpty();
            verifyNoInteractions(soundService);
        }
        @Test
        @DisplayName("успешно меняет роль с Создателя на Обычного (не изменяет звуки)")
        void shouldChangeRoleCreatorToRegular() {
            // Arrange
            UserDto updateDto = new UserDto();
            updateDto.setName("WasCre");
            updateDto.setRole(false);

            // Мокаем репозитории
            when(userRepository.findById(3L)).thenReturn(creatorUser);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserDto result = userService.updateUser(3L, updateDto);

            // Assert
            assertNotNull(result, "Результат не должен быть null");
            assertFalse(result.getRole(), "Роль должна быть изменена на 'Обычный пользователь'");
            verify(userRepository).findById(3L);
            verify(userRepository).save(userCaptor.capture());

            // Проверяем, что роль пользователя изменилась
            User capturedUser = userCaptor.getValue();
            assertFalse(capturedUser.getRole(), "Роль пользователя должна быть false (обычный)");

            // Проверяем, что список звуков не изменился
            Set<Sound> sounds = capturedUser.getSounds();
            assertNotNull(sounds, "Список звуков не должен быть null");
            assertFalse(sounds.isEmpty(), "Список звуков не должен быть пустым");

            // Убедимся, что взаимодействий с soundService не было
            verifyNoInteractions(soundService);
        }


        @Test @DisplayName("успешно меняет роль с Обычного на Создателя")
        void shouldChangeRoleRegularToCreator() {
            CreatorDto updateDto = new CreatorDto(); updateDto.setName("BecameCre"); updateDto.setRole(true); updateDto.setSounds(Collections.emptySet());
            when(userRepository.findById(1L)).thenReturn(regularUser);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            UserDto result = userService.updateUser(1L, updateDto);
            assertTrue(result.getRole());
            verify(userRepository).findById(1L);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().getRole());
            verifyNoInteractions(soundService);
        }
        @Test @DisplayName("выбрасывает исключение, если пользователь для обновления не найден")
        void shouldThrow_WhenUserToUpdateNotFound() {
            UserDto updateDto = new UserDto();
            when(userRepository.findById(99L)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, updateDto));
            verify(userRepository).findById(99L);
            verify(userRepository, never()).save(any());
        }
    }

    // ==================== DELETE SINGLE USER ====================
    @Nested @DisplayName("deleteUser") class DeleteUser {
        @Test @DisplayName("успешно удаляет существующего пользователя") void shouldDeleteExistingUser() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);
            userService.deleteUser(1L);
            verify(userRepository).existsById(1L);
            verify(userRepository).deleteById(1L);
        }
        @Test @DisplayName("выбрасывает исключение при удалении несуществующего пользователя") void shouldThrowException_WhenDeletingNonExistentUser() {
            when(userRepository.existsById(99L)).thenReturn(false);
            assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
            verify(userRepository).existsById(99L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }
    // ==================== DELETE ALL USERS ====================
    @Nested @DisplayName("deleteUsers") class DeleteAllUsers {
        @Test @DisplayName("успешно вызывает deleteAll") void shouldCallDeleteAll() {
            doNothing().when(userRepository).deleteAll();
            userService.deleteUsers();
            verify(userRepository, times(1)).deleteAll();
        }
    }
    // ==================== UPDATE USERS (BULK) ====================
    @Nested @DisplayName("updateUsers (Bulk)") class UpdateUsersBulk {
        @Test @DisplayName("успешно обновляет несколько пользователей") void shouldUpdateMultipleUsers() {
            UserDto update1 = UserMapper.toDto(regularUser); update1.setName("Updated Reg Bulk"); update1.setId(1L);
            CreatorDto update2 = UserMapper.toCreatorDto(creatorUser); update2.setName("Updated Cre Bulk"); update2.setId(3L); update2.setSounds(Collections.emptySet());
            List<UserDto> updates = List.of(update1, update2);
            when(userRepository.findById(1L)).thenReturn(regularUser); // Entity/null
            when(userRepository.findById(3L)).thenReturn(creatorUser); // Entity/null
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            List<UserDto> results = userService.updateUsers(updates);
            assertThat(results).hasSize(2);
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(3L);
            verify(userRepository, times(2)).save(any(User.class));
        }
        @Test
        @DisplayName("updateUsers: успешно обновляет существующих и игнорирует несуществующего")
        void shouldUpdateExistingAndIgnoreNotFoundInBulk() { // Переименован для ясности
            // Arrange
            UserDto update1 = UserMapper.toDto(regularUser); update1.setName("Updated Reg Bulk"); update1.setId(1L);
            UserDto updateNonExistent = new UserDto(); updateNonExistent.setId(99L); updateNonExistent.setName("X");
            List<UserDto> updates = List.of(update1, updateNonExistent);

            when(userRepository.findById(1L)).thenReturn(regularUser); // Первый найден
            when(userRepository.findById(99L)).thenReturn(null); // Второй НЕ найден
            when(userRepository.save(regularUser)).thenReturn(regularUser); // Save для первого

            // Act & Assert
            // Ожидаем, что метод НЕ выбросит исключение
            List<UserDto> results = assertDoesNotThrow(() -> userService.updateUsers(updates));

            // Проверяем результат: должен содержать только первого пользователя
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(1L);
            assertThat(results.getFirst().getName()).isEqualTo("Updated Reg Bulk");

            // Проверяем вызовы findById для обоих
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(99L);
            // Проверяем, что save был вызван только для существующего
            verify(userRepository, times(1)).save(regularUser);
            verify(userRepository, never()).save(argThat(u -> u.getId() == null || u.getId() == 99L)); // Save для несуществующего не вызывался
        }
        @Test @DisplayName("updateUsers: пропускает null DTO в списке") void shouldSkipNullDtoInBulkUpdate() {
            UserDto update1 = UserMapper.toDto(regularUser); update1.setName("Bulk OK"); update1.setId(1L);
            List<UserDto> updates = new ArrayList<>(); updates.add(update1); updates.add(null);
            when(userRepository.findById(1L)).thenReturn(regularUser);
            when(userRepository.save(regularUser)).thenReturn(regularUser);
            List<UserDto> results = userService.updateUsers(updates);
            assertThat(results).hasSize(1); // Только один успешно обновлен
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, never()).findById(isNull());
            verify(userRepository, times(1)).save(regularUser);
        }
        @Test @DisplayName("updateUsers: падает с NPE при обработке DTO с null ID")
        void shouldFailWithNPE_WhenDtoWithNullIdInBulkUpdate() { // Переименован
            // Arrange
            UserDto update1 = UserMapper.toDto(regularUser); update1.setName("Bulk OK Again"); update1.setId(1L);
            UserDto dtoWithNullId = new UserDto(); dtoWithNullId.setName("No ID"); dtoWithNullId.setId(null); // null ID
            List<UserDto> updates = List.of(update1, dtoWithNullId);

            // Моки для первого, успешного вызова
            when(userRepository.findById(1L)).thenReturn(regularUser);
            when(userRepository.save(regularUser)).thenReturn(regularUser);

            // Act & Assert
            // Ожидаем NPE (или другую ошибку, которую бросает JVM/сервис при передаче null ID)
            assertThrows(Exception.class, // Можно уточнить до NPE или IllegalArgumentException
                    () -> userService.updateUsers(updates));

            // Проверяем, что для первого пользователя все было вызвано
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).save(regularUser);
            // Проверяем, что findById для null ID не вызывался (т.к. упали раньше)
            verify(userRepository, never()).findById(isNull());
        }
    }
}