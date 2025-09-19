package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Album;
import com.music.musicstore.models.users.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Find albums by artist
    List<Album> findByArtist(Artist artist);

    // Find albums by genre
    List<Album> findByGenre(String genre);

    // Find albums by title containing text (case insensitive)
    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Find albums by artist username
    @Query("SELECT a FROM Album a WHERE a.artist.userName = :artistUsername")
    Page<Album> findByArtistUsername(@Param("artistUsername") String artistUsername, Pageable pageable);

    // Count albums by artist username
    @Query("SELECT COUNT(a) FROM Album a WHERE a.artist.userName = :artistUsername")
    long countByArtistUsername(@Param("artistUsername") String artistUsername);

    // Find albums by genre (paginated)
    Page<Album> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    // Find albums by artist and genre
    List<Album> findByArtistAndGenre(Artist artist, String genre);

    // Custom query to find albums with their track count
    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.tracks WHERE a.id = :id")
    Album findByIdWithTracks(@Param("id") Long id);
}
