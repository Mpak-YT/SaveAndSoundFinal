package com.sas.saveandsound.service;

import com.sas.saveandsound.dto.SoundDto;
import com.sas.saveandsound.mapper.SoundMapper;
import com.sas.saveandsound.repository.SoundRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoundService {

    private final SoundRepository soundRepository;

    public SoundService(SoundRepository soundRepository) {
        this.soundRepository = soundRepository;
    }

    public List<SoundDto> search(String name) {
        return soundRepository.findByName(name)
                .stream()
                .map(SoundMapper::toDto)
                .toList(); // Конвертируем список Sound в список SoundDto
    }

    public SoundDto search(long id) {
        return SoundMapper.toDto(soundRepository.findById(id));
    }
}
