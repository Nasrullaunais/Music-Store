package com.music.musicstore.repositories;

import com.music.musicstore.models.users.Artist;
import com.music.musicstore.models.music.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    List<Music> findByCategory(String category);

    List<Music> findByArtist(Artist artist);

    List<Music> findByGenre(String genre);

    List<Music> findByReleaseYear(Integer releaseYear);

    Page<Music> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Music> findByArtistContainingIgnoreCase(String artist, Pageable pageable);

    Page<Music> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    Page<Music> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // Combined search
    Page<Music> findByNameContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String name, String artist, String genre, Pageable pageable);


}