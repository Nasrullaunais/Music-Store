package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    List<Music> findByCategory(String category);

    List<Music> findByArtistUsername(String artistUsername);

    List<Music> findByGenre(String genre);

    List<Music> findByReleaseYear(Integer releaseYear);

    Page<Music> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Music> findByArtistUsernameContainingIgnoreCase(String artistUsername, Pageable pageable);

    Page<Music> findByGenreContainingIgnoreCase(String genre, Pageable pageable);

    Page<Music> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // Combined search
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String name, String artistUsername, String genre, Pageable pageable);

    // Missing methods needed by MusicService

    // Search by title or artist (for CustomerApiController search)
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCase(
            String name, String artistUsername, Pageable pageable);

    // Paginated version for better performance
    Page<Music> findByArtistUsername(String artistUsername, Pageable pageable);

    // Find by genre with pagination
    Page<Music> findByGenre(String genre, Pageable pageable);

    // Analytics methods for MusicService
    @Query("SELECT m.genre, COUNT(m) FROM Music m GROUP BY m.genre")
    List<Object[]> countByGenreGroupBy();

    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.music m")
    Double getAverageRating();

    // Additional search and filter methods
    List<Music> findByPriceBetween(Double minPrice, Double maxPrice);

    Page<Music> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    // Find music by multiple criteria
    @Query("SELECT m FROM Music m WHERE " +
           "(:genre IS NULL OR m.genre = :genre) AND " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR m.price <= :maxPrice)")
    Page<Music> findByMultipleCriteria(@Param("genre") String genre,
                                     @Param("category") String category,
                                     @Param("minPrice") Double minPrice,
                                     @Param("maxPrice") Double maxPrice,
                                     Pageable pageable);

    // Count methods for analytics
    long countByGenre(String genre);
    long countByCategory(String category);
    long countByArtistUsername(String artistUsername);
}