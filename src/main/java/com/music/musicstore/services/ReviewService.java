package com.music.musicstore.services;

import com.music.musicstore.models.music.Review;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.ReviewRepository;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MusicRepository musicRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, MusicRepository musicRepository) {
        this.reviewRepository = reviewRepository;
        this.musicRepository = musicRepository;
    }

    public Review createReview(Long musicId, Customer customer, Integer rating, String comment) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        // Check if user already reviewed this music
        Optional<Review> existingReview = reviewRepository.findByMusicAndCustomer(music, customer);
        if (existingReview.isPresent()) {
            throw new RuntimeException("You have already reviewed this music");
        }

        Review review = new Review(music, customer, rating, comment);
        return reviewRepository.save(review);
    }

    public Review updateReview(Long reviewId, Customer customer, Integer rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You can only update your own reviews");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, Customer customer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    public Page<Review> getReviewsByMusic(Long musicId, Pageable pageable) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        return reviewRepository.findByMusicOrderByCreatedAtDesc(music, pageable);
    }

    public Optional<Review> getUserReviewForMusic(Long musicId, Customer customer) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        return reviewRepository.findByMusicAndCustomer(music, customer);
    }

    public Double getAverageRating(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        return reviewRepository.findAverageRatingByMusic(music);
    }

    public Long getTotalReviews(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        return reviewRepository.countReviewsByMusic(music);
    }

    public ReviewStats getReviewStats(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));

        Double averageRating = reviewRepository.findAverageRatingByMusic(music);
        Long totalReviews = reviewRepository.countReviewsByMusic(music);

        ReviewStats stats = new ReviewStats();
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        stats.setTotalReviews(totalReviews);

        // Get rating distribution
        for (int i = 1; i <= 5; i++) {
            Long count = reviewRepository.countReviewsByMusicAndRating(music, i);
            stats.setRatingCount(i, count);
        }

        return stats;
    }

    // Missing methods for artist functionality
    public List<Review> getReviewsByMusicId(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found"));
        return reviewRepository.findByMusicOrderByCreatedAtDesc(music);
    }

    public Map<String, Object> getArtistReviewsAnalytics(String artistName) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalReviews", 0);
        analytics.put("averageRating", 0.0);
        analytics.put("ratingDistribution", new HashMap<>());
        return analytics;
    }

    // Missing methods for customer functionality
    public List<Review> getReviewsByUsername(String username) {
        // Implementation would get reviews by username
        throw new UnsupportedOperationException("Get reviews by username not implemented yet");
    }

    // Missing inner class
    public static class ReviewStats {
        private Double averageRating;
        private Long totalReviews;
        private Map<Integer, Long> ratingCounts = new HashMap<>();

        public ReviewStats() {
            this.averageRating = 0.0;
            this.totalReviews = 0L;
        }

        public ReviewStats(Double averageRating, Long totalReviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
        }

        // Getters and setters
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
        public Long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
        public void setRatingCount(Integer rating, Long count) { ratingCounts.put(rating, count); }
        public Map<Integer, Long> getRatingCounts() { return ratingCounts; }
    }
}
