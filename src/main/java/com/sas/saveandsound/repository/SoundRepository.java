package com.sas.saveandsound.repository;

import com.sas.saveandsound.model.Sound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

    List<Sound> findByName(String name);

    Sound findById(long id);
}
