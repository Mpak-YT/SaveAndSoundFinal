package com.sas.saveandsound.repository;

import com.sas.saveandsound.model.Sound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

    List<Sound> findAll();

    List<Sound> findByName(String name);

    Sound findById(long id);

    @Query("SELECT s FROM Sound s JOIN s.creators u WHERE u.name = :userName")
    List<Sound> findSoundsByUserNameJPQL(@Param("userName") String userName);

    // Нативный SQL запрос
    @Query(value = "SELECT s.* FROM sounds s JOIN albums a ON s.album_id = a.id WHERE a.name = :albumName",
            nativeQuery = true)
    List<Sound> findSoundsByAlbumNameNative(@Param("albumName") String albumName);
}
