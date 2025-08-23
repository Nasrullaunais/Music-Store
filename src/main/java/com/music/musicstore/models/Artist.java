package com.music.musicstore.models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
@Entity
@Table(name = "artists")
public class Artist implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Music> music;

    @Column(nullable = false)
    private String role = "ROLE_ARTIST";

    @Column
    private String cover;

    public Artist() {}
    public Artist(String name, String cover) {
        this.userName = name;
        this.cover = cover;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public List<Music> getMusic() {
        return music;
    }
    public void setMusic(List<Music> music) {
        this.music = music;
    }
    public String getCover() {
        return cover;
    }
    public void setCover(String cover) {
        this.cover = cover;
    }
    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", music=" + music +
                ", cover='" + cover + '\'' +
                '}';
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public void setName(String newName) {
        this.userName = newName;
    }

}
