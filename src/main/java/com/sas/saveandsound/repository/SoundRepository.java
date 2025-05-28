package com.sas.saveandsound.repository;

import com.sas.saveandsound.model.Sound;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {

    @NonNull
    List<Sound> findAll();

    List<Sound> findByName(String name);

    @Query("SELECT s FROM Sound s WHERE s.name = :name " +
            "AND (:albumId IS NULL AND s.album IS NULL OR s.album.id = :albumId)")
    Optional<Sound> findByNameAndAlbumId(@Param("name") String name, @Param("albumId") Long albumId);

    Sound findById(long id);

    @Query("SELECT s FROM Sound s JOIN s.creators u WHERE u.name = :userName")
    List<Sound> findSoundsByUserNameJPQL(@Param("userName") String userName);

    // Нативный SQL запрос
    @Query(value = "SELECT s.* FROM sounds s JOIN albums a ON s.album_id = a.id WHERE a.name = :albumName",
            nativeQuery = true)
    List<Sound> findSoundsByAlbumNameNative(@Param("albumName") String albumName);

}
