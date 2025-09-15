package com.music.musicstore.dto;

public class CreateReviewRequest {
     private Long musicId;
    private Integer rating;
    private String comment;

    public CreateReviewRequest() {}

    public CreateReviewRequest(Long musicId, Integer rating, String comment) {
        this.musicId = musicId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getMusicId() { return musicId; }
    public void setMusicId(Long musicId) { this.musicId = musicId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
