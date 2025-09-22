package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Search by title or artist (for CustomerApiController search)
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCase(
            String name, String artistUsername, Pageable pageable);


    Optional<Music> findByName(String name);
    Optional<Music> findByNameContainingIgnoreCase(String name);

    // Paginated version for better performance
    Page<Music> findByArtistUsername(String artistUsername, Pageable pageable);

    // Find by genre with pagination
    Page<Music> findByGenre(String genre, Pageable pageable);

    // Count methods for analytics
    long countByArtistUsername(String artistUsername);

    @Query("SELECT COUNT(m) FROM Music m WHERE m.genre = ?1")
    long countByGenre(String genre);

    @Query("SELECT m.genre, COUNT(m) FROM Music m GROUP BY m.genre")
    List<Object[]> countByGenreGroupBy();

    @Query("SELECT AVG(m.averageRating) FROM Music m WHERE m.averageRating IS NOT NULL")
    Double getAverageRating();

    // NEW: Flagged content methods
    Page<Music> findByIsFlaggedTrue(Pageable pageable);

    long countByIsFlaggedTrue();

    @Query("SELECT AVG(m.averageRating) FROM Music m WHERE m.averageRating IS NOT NULL")
    BigDecimal getAverageRatingAcrossAll();

    @Query("SELECT m FROM Music m WHERE m.averageRating IS NOT NULL ORDER BY m.averageRating DESC")
    List<Music> findTopRatedMusic(Pageable pageable);

    @Query("SELECT new map(m.genre as genre, COUNT(m) as count) FROM Music m GROUP BY m.genre")
    Map<String, Long> countByGenreGrouped();

    @Query("SELECT new map(m.category as category, COUNT(m) as count) FROM Music m GROUP BY m.category")
    Map<String, Long> countByCategoryGrouped();

    @Query("SELECT new map(m.artistUsername as artist, COUNT(m) as totalTracks, AVG(m.averageRating) as avgRating, SUM(m.totalReviews) as totalReviews) FROM Music m GROUP BY m.artistUsername")
    List<Map<String, Object>> getArtistPerformanceStats();
}