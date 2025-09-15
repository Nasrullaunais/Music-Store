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
import java.util.List;
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

    public static class ReviewStats {
        private Double averageRating;
        private Long totalReviews;
        private Long oneStarCount = 0L;
        private Long twoStarCount = 0L;
        private Long threeStarCount = 0L;
        private Long fourStarCount = 0L;
        private Long fiveStarCount = 0L;

        // Getters and setters
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }

        public Long getOneStarCount() { return oneStarCount; }
        public void setOneStarCount(Long oneStarCount) { this.oneStarCount = oneStarCount; }

        public Long getTwoStarCount() { return twoStarCount; }
        public void setTwoStarCount(Long twoStarCount) { this.twoStarCount = twoStarCount; }

        public Long getThreeStarCount() { return threeStarCount; }
        public void setThreeStarCount(Long threeStarCount) { this.threeStarCount = threeStarCount; }

        public Long getFourStarCount() { return fourStarCount; }
        public void setFourStarCount(Long fourStarCount) { this.fourStarCount = fourStarCount; }

        public Long getFiveStarCount() { return fiveStarCount; }
        public void setFiveStarCount(Long fiveStarCount) { this.fiveStarCount = fiveStarCount; }

        public void setRatingCount(int rating, Long count) {
            switch (rating) {
                case 1: oneStarCount = count; break;
                case 2: twoStarCount = count; break;
                case 3: threeStarCount = count; break;
                case 4: fourStarCount = count; break;
                case 5: fiveStarCount = count; break;
            }
        }
    }
}
