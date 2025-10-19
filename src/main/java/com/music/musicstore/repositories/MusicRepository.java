package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    // Soft delete aware queries - exclude deleted music by default
    List<Music> findByCategoryAndIsDeletedFalse(String category);

    List<Music> findByArtistUsernameAndIsDeletedFalse(String artistUsername);

    List<Music> findByGenreAndIsDeletedFalse(String genre);

    List<Music> findByReleaseYearAndIsDeletedFalse(Integer releaseYear);

    Page<Music> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    Page<Music> findByArtistUsernameContainingIgnoreCaseAndIsDeletedFalse(String artistUsername, Pageable pageable);

    Page<Music> findByGenreContainingIgnoreCaseAndIsDeletedFalse(String genre, Pageable pageable);

    Page<Music> findByCategoryContainingIgnoreCaseAndIsDeletedFalse(String category, Pageable pageable);

    // Combined search - exclude deleted
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseOrGenreContainingIgnoreCaseAndIsDeletedFalse(
            String name, String artistUsername, String genre, Pageable pageable);

    // Search by title or artist (for CustomerApiController search) - exclude deleted
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseAndIsDeletedFalse(
            String name, String artistUsername, Pageable pageable);

    Optional<Music> findByNameAndIsDeletedFalse(String name);
    Optional<Music> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    // Paginated version for better performance - exclude deleted
    Page<Music> findByArtistUsernameAndIsDeletedFalse(String artistUsername, Pageable pageable);

    // Find by genre with pagination - exclude deleted
    Page<Music> findByGenreAndIsDeletedFalse(String genre, Pageable pageable);

    // Keep original methods for backwards compatibility (they will return all including deleted)
    List<Music> findByCategory(String category);
    List<Music> findByArtistUsername(String artistUsername);
    List<Music> findByGenre(String genre);
    List<Music> findByReleaseYear(Integer releaseYear);
    Page<Music> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Music> findByArtistUsernameContainingIgnoreCase(String artistUsername, Pageable pageable);
    Page<Music> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
    Page<Music> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String name, String artistUsername, String genre, Pageable pageable);
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCase(
            String name, String artistUsername, Pageable pageable);
    Optional<Music> findByName(String name);
    Optional<Music> findByNameContainingIgnoreCase(String name);
    Page<Music> findByArtistUsername(String artistUsername, Pageable pageable);
    Page<Music> findByGenre(String genre, Pageable pageable);

    // Missing methods for AdminApiController functionality

    // Count methods for analytics
    long countByArtistUsername(String artistUsername);

    // Flagged music methods
    Page<Music> findByIsFlaggedTrue(Pageable pageable);
    long countByIsFlaggedTrue();

    // Analytics methods
    @Query("SELECT AVG(m.averageRating) FROM Music m WHERE m.averageRating IS NOT NULL")
    Double getAverageRating();

    @Query("SELECT m.genre, COUNT(m) FROM Music m WHERE m.genre IS NOT NULL GROUP BY m.genre")
    List<Object[]> countByGenreGroupBy();

    @Query("SELECT m.category, COUNT(m) FROM Music m WHERE m.category IS NOT NULL GROUP BY m.category")
    List<Object[]> countByCategoryGroupBy();

    // Top-rated music for "top selling" placeholder
    Page<Music> findAllByOrderByAverageRatingDesc(Pageable pageable);

    // Artist performance analytics
    @Query("SELECT m.artistUsername, COUNT(m), AVG(m.averageRating), SUM(m.totalReviews) " +
           "FROM Music m WHERE m.artistUsername IS NOT NULL " +
           "GROUP BY m.artistUsername")
    List<Object[]> getArtistPerformanceStats();

    // Delete from join table for hard delete
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "DELETE FROM customer_purchased_music WHERE music_id = :musicId", nativeQuery = true)
    void deleteFromCustomerPurchasedMusic(@org.springframework.data.repository.query.Param("musicId") Long musicId);
}
