package com.music.musicstore.services;

import com.music.musicstore.models.music.Music;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.dto.MusicDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.math.BigDecimal;

@Service
public class MusicService {
    private final MusicRepository musicRepository;
    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }


    public Music saveMusic(Music music){
        return musicRepository.save(music);
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

    // Missing methods for artist functionality
    public Music uploadMusic(String title, String genre, Double price, String artist,
                           MultipartFile musicFile, MultipartFile coverFile, String username) {
        // Placeholder implementation - would handle file uploads
        Music music = new Music();
        music.setName(title);
        music.setGenre(genre);
        music.setPrice(BigDecimal.valueOf(price));
        // For now, we'll use the category field for artist info until proper Artist relationship is set up
        music.setCategory(artist);
        return musicRepository.save(music);
    }

    public List<Music> getMusicByArtist(String artistName) {
        // Since we're using category field temporarily for artist info
        return musicRepository.findByCategory(artistName);
    }

    // Helper method to get artist name from Music entity
    public String getArtistName(Music music) {
        if (music.getArtist() != null) {
            return music.getArtist().getUserName();
        }
        return music.getCategory(); // Fallback to category field
    }

    public Music updateMusic(Long id, MusicDto musicDto, String username) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Music not found"));
        // Update fields from DTO
        music.setName(musicDto.getName());
        music.setGenre(musicDto.getGenre());
        music.setPrice(musicDto.getPrice());
        return musicRepository.save(music);
    }

    public void deleteMusic(Long id, String username) {
        musicRepository.deleteById(id);
    }

    // Missing methods for admin functionality
    public Page<Music> getAllMusicForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return musicRepository.findAll(pageable);
    }

    public void deleteMusicAsAdmin(Long id) {
        musicRepository.deleteById(id);
    }

    public Music updateMusicStatus(Long id, String status) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Music not found"));
        // Assuming there's a status field - this is a placeholder
        return musicRepository.save(music);
    }

    public long getTotalMusicCount() {
        return musicRepository.count();
    }

    public Map<String, Object> getMusicAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalMusic", getTotalMusicCount());
        analytics.put("newMusic", 0);
        return analytics;
    }

    public List<Music> getMostPopularMusic(LocalDate startDate, LocalDate endDate) {
        // Placeholder implementation
        return musicRepository.findAll().stream().limit(10).toList();
    }

    // Missing methods for customer functionality
    public List<Music> getDownloadableMusic(String username) {
        // Return music that the user has purchased
        return List.of(); // Placeholder
    }

    public ResponseEntity<Resource> downloadMusic(Long musicId, String username) {
        // Implementation would handle file download
        throw new UnsupportedOperationException("Download not implemented yet");
    }

    public List<Object> getUserPlaylists(String username) {
        // Placeholder implementation
        return List.of();
    }

    public Object createPlaylist(String name, String description, String username) {
        // Placeholder implementation
        return new HashMap<String, Object>();
    }

    public void addToPlaylist(Long playlistId, Long musicId, String username) {
        // Placeholder implementation
    }

    public void removeFromPlaylist(Long playlistId, Long musicId, String username) {
        // Placeholder implementation
    }

    // Missing methods for staff functionality
    public Map<String, Object> getArtistSalesAnalytics(String artistName) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalSales", 0);
        analytics.put("revenue", 0.0);
        return analytics;
    }

    public Map<String, Object> getMusicPerformanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPlays", 0);
        report.put("topMusic", List.of());
        return report;
    }

}
