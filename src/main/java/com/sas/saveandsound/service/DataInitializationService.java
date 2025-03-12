package com.sas.saveandsound.service;

import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;

    public DataInitializationService(UserRepository userRepository,
        SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
    }

    public void initializeData() {
        // Создание пользователей
        User user1 = new User("Alice", true);
        User user2 = new User("Bob", true);
        User user3 = new User("Charlie", true);

        // Сохранение пользователей
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Создание песен
        Sound sound1 = new Sound("Song 1");
        Sound sound2 = new Sound("Song 2");
        Sound sound3 = new Sound("Song 3");

        // Привязка создателей к песням
        sound1.getCreators().add(user1);
        sound1.getCreators().add(user2);

        sound2.getCreators().add(user1);
        sound2.getCreators().add(user3);

        sound3.getCreators().add(user2);

        // Сохранение песен
        soundRepository.save(sound1);
        soundRepository.save(sound2);
        soundRepository.save(sound3);
    }
}
