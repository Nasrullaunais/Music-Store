package com.music.musicstore.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.music.musicstore.models.Music;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    Music findByTitle(String title);
    Music findByAlbum(String album);
    Music findByArtist(String artist);
    Music findByGenre(String genre);
    List<Music> findAllByArtist(String artist);
    List<Music> findAllByGenre(String genre);
    List<Music> findAllByAlbum(String album);
    List<Music> findAllByTitleIsContainingIgnoreCase(String title);
}
