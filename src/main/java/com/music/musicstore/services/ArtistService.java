package com.music.musicstore.services;

import com.music.musicstore.models.Artist;
import com.music.musicstore.repositories.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ArtistService {
    ArtistRepository artistRepository;
    PasswordEncoder passwordEncoder;

    @Autowired
    public ArtistService(ArtistRepository artistRepository, PasswordEncoder passwordEncoder) {
        this.artistRepository = artistRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails loadUserByUsername(String username) {
        return artistRepository.findByUserName(username).orElseThrow(() -> new UsernameNotFoundException("Artist not found with username: " + username));
    }

    public void registerArtist(String name, String rawPassword) {
        if (artistRepository.findByUserName(name).isPresent()) {
            throw new RuntimeException("Artist already exists!");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        artistRepository.save(new Artist(name, encodedPassword));
    }

    public  void deleteArtistByName(String name){
        artistRepository.deleteByName(name);
    }

    public void updateArtist(Artist artist) {
        artistRepository.findById(artist.getId())
                .orElseThrow(() -> new IllegalArgumentException("Artist not found with id: " + artist.getId()));
        if (artist.getPassword() != null && !artist.getPassword().isEmpty()) {
            artist.setPassword(passwordEncoder.encode(artist.getPassword()));
        }
        artistRepository.save(artist);
    }

    public void updateArtistUsername(Long id, String newName) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found with id: " + id));
        artist.setName(newName);
        artistRepository.save(artist);
    }
}
