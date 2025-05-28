package com.sas.saveandsound.repository;

import com.sas.saveandsound.model.Album;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.EntityGraph; // New import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // findById returns Optional

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @EntityGraph(attributePaths = "sounds")
    @NonNull
    List<Album> findAll();

    List<Album> findByName(String name);

    @EntityGraph(attributePaths = "sounds")
    Optional<Album> findById(long id); // Change return type to Optional

}
