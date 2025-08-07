package com.pg128.musicstore.services;

import com.pg128.musicstore.models.Product;
import com.pg128.musicstore.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(Product product) {
        // Check if product exists
        productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + product.getId()));
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Category-specific methods
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Music-specific methods
    public List<Product> getProductsByArtist(String artist) {
        return productRepository.findByArtist(artist);
    }

    public List<Product> getProductsByGenre(String genre) {
        return productRepository.findByGenre(genre);
    }

    public List<Product> getProductsByReleaseYear(Integer releaseYear) {
        return productRepository.findByReleaseYear(releaseYear);
    }

    // Search methods
    public Page<Product> searchProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Product> searchProductsByArtist(String artist, Pageable pageable) {
        return productRepository.findByArtistContainingIgnoreCase(artist, pageable);
    }

    public Page<Product> searchProductsByGenre(String genre, Pageable pageable) {
        return productRepository.findByGenreContainingIgnoreCase(genre, pageable);
    }

    public Page<Product> searchProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryContainingIgnoreCase(category, pageable);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(
                query, query, query, pageable);
    }
}