package com.sas.saveandsound.repository;

import com.sas.saveandsound.dto.AlbumDto;
import com.sas.saveandsound.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findAll();

    List<Album> findByName(String name);

    Album findById(long id);

}
