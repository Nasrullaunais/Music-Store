package com.music.musicstore.services;

import com.music.musicstore.models.Music;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    //CRUD methods
    public List<Music> getAllMusic(){
        return musicRepository.findAll();
    }
    public Optional<Music> getMusicById(Long id){
        return musicRepository.findById(id);
    }
    public Music saveMusic(Music music){
        return musicRepository.save(music);
    }
    public void deleteMusicById(Long id){
        musicRepository.deleteById(id);
    }

    //Custom methods
    public List<Music> findAllByAlbum(String album){
        return musicRepository.findAllByAlbum(album);
    }
    public List<Music> findAllByTitleIsContainingIgnoreCase(String title){
        return musicRepository.findAllByTitleIsContainingIgnoreCase(title);
    }
    public List<Music> findAllByArtist(String artist){
        return musicRepository.findAllByArtist(artist);
    }
    public List<Music> findAllByGenre(String genre){
        return musicRepository.findAllByGenre(genre);
    }
    public Music findByTitle(String title){
        return musicRepository.findByTitle(title);
    }

}
