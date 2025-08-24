package com.music.musicstore.repositories;

import com.music.musicstore.models.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserName(String name);

    Optional<Artist> findAllByNameContainingIgnoreCase(String name);

    default void deleteByName(String name) {

    }

    Optional<Artist> findById(Long id);
}
