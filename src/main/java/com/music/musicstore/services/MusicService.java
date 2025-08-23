package com.music.musicstore.services;

import com.music.musicstore.models.Music;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MusicService {
    private final MusicRepository musicRepository;
    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }


    public void saveMusic(Music music){
        musicRepository.save(music);
    }

    public void deleteMusic(Long id){
        musicRepository.deleteById(id);
    }
    public void updateMusic(Music music){
        musicRepository.save(music);
    }


    public List<Music> getAllMusic(){
        return musicRepository.findAll();
    }
    public Optional<Music> getMusicById(Long id){
        return musicRepository.findById(id);
    }

    public List<Music> getMusicByGenre(String genre){
        return musicRepository.findByGenre(genre);
    }
    public List<Music> getMusicByReleaseYear(Integer releaseYear){
        return musicRepository.findByReleaseYear(releaseYear);
    }
    // Search methods
    public Page<Music> searchProductsByName(String name, Pageable pageable) {
        return musicRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Music> searchProductsByArtist(String artist, Pageable pageable) {
        return musicRepository.findByArtistContainingIgnoreCase(artist, pageable);
    }

    public Page<Music> searchProductsByGenre(String genre, Pageable pageable) {
        return musicRepository.findByGenreContainingIgnoreCase(genre, pageable);
    }

    public Page<Music> searchProductsByCategory(String category, Pageable pageable) {
        return musicRepository.findByCategoryContainingIgnoreCase(category, pageable);
    }

    public Page<Music> searchProducts(String query, Pageable pageable) {
        return musicRepository.findByNameContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(
                query, query, query, pageable);
    }

    public Page<Music> getAllMusic(Pageable pageable){
        return musicRepository.findAll(pageable);
    }

}
