package com.music.musicstore.models;

import jakarta.persistence.*;

@Entity
@Table(name = "music")
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String artist;
    private String genre;
    @Column(nullable = false)
    private double price;
    private String album;
    private String cover;

    public Music() {
    }

    public Music(String title, String artist, String genre, double price, String album, String cover) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.price = price;
        this.album = album;
        this.cover = cover;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", price=" + price +
                ", album='" + album + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }
}
