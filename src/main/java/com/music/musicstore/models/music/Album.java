package com.music.musicstore.models.music;
import jakarta.persistence.*;

@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String artist;
    @Column(nullable = false)
    private String genre;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private String cover;
    @ManyToOne()
    private Music music;

    public Album() {
    }

    public Album(String title, String artist, String genre, double price, String cover) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.price = price;
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

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
