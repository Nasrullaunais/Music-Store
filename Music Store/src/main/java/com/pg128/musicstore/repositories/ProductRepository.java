package com.pg128.musicstore.repositories;

import com.pg128.musicstore.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByArtist(String artist);
    
    List<Product> findByGenre(String genre);
    
    List<Product> findByReleaseYear(Integer releaseYear);
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Product> findByArtistContainingIgnoreCase(String artist, Pageable pageable);
    
    Page<Product> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
    
    Page<Product> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    
    // Combined search
    Page<Product> findByNameContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String name, String artist, String genre, Pageable pageable);
}