package com.sas.saveandsound.service;

import com.sas.saveandsound.model.Sound;
import com.sas.saveandsound.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.sas.saveandsound.repository.SoundRepository;
import com.sas.saveandsound.repository.UserRepository;



@Service
public class UserService {

    private final UserRepository userRepository;
    private final SoundRepository soundRepository;

    public UserService(UserRepository userRepository, SoundRepository soundRepository) {
        this.userRepository = userRepository;
        this.soundRepository = soundRepository;
    }

    @Transactional
    public void removeCreatorAndDeleteOrphanSound(Long soundId, Long creatorId) {
        // Получить песню
        Sound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new RuntimeException("Sound not found"));

        // Получить создателя
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        // Удалить создателя из коллекции
        sound.getCreators().remove(creator);

        // Проверка: остались ли другие создатели у песни
        if (sound.getCreators().isEmpty()) {
            // Удаляем песню, если больше нет создателей
            soundRepository.delete(sound);
        } else {
            // Сохраняем изменения, если у песни остались создатели
            soundRepository.save(sound);
        }
    }

}
