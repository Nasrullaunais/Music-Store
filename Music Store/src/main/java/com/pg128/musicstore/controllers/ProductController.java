package com.pg128.musicstore.controllers;

import com.pg128.musicstore.models.Product;
import com.pg128.musicstore.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ProductController {

    private final ProductService productService;
    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Customer-facing product pages
    @GetMapping("/products")
    public String listProducts(Model model, 
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String artist,
                              @RequestParam(required = false) String genre,
                              @RequestParam(required = false) String search) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> productPage;
        
        if (search != null && !search.isEmpty()) {
            productPage = productService.searchProducts(search, pageRequest);
            model.addAttribute("searchTerm", search);
        } else if (category != null && !category.isEmpty()) {
            productPage = productService.searchProductsByCategory(category, pageRequest);
            model.addAttribute("selectedCategory", category);
        } else if (artist != null && !artist.isEmpty()) {
            productPage = productService.searchProductsByArtist(artist, pageRequest);
            model.addAttribute("selectedArtist", artist);
        } else if (genre != null && !genre.isEmpty()) {
            productPage = productService.searchProductsByGenre(genre, pageRequest);
            model.addAttribute("selectedGenre", genre);
        } else {
            productPage = productService.getAllProducts(pageRequest);
        }
        
        model.addAttribute("products", productPage);
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            
            // Get related products (same artist or genre)
            if (product.get().getArtist() != null) {
                List<Product> relatedByArtist = productService.getProductsByArtist(product.get().getArtist());
                relatedByArtist.removeIf(p -> p.getId().equals(id));
                model.addAttribute("relatedByArtist", relatedByArtist.stream().limit(4).toList());
            }
            
            if (product.get().getGenre() != null) {
                List<Product> relatedByGenre = productService.getProductsByGenre(product.get().getGenre());
                relatedByGenre.removeIf(p -> p.getId().equals(id));
                model.addAttribute("relatedByGenre", relatedByGenre.stream().limit(4).toList());
            }
            
            return "products/view";
        } else {
            return "redirect:/products";
        }
    }

    // Admin product management
    @GetMapping("/admin/products")
    public String listProductsAdmin(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String search) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> productPage;
        
        if (search != null && !search.isEmpty()) {
            productPage = productService.searchProducts(search, pageRequest);
            model.addAttribute("searchTerm", search);
        } else {
            productPage = productService.getAllProducts(pageRequest);
        }
        
        model.addAttribute("products", productPage);
        return "admin/products/list";
    }

    @GetMapping("/admin/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/products/form";
    }

    @PostMapping("/admin/products/save")
    public String saveProduct(@Valid Product product, BindingResult result,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/products/form";
        }
        
        try {
            // Handle image upload if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName));
                product.setImageUrl("/images/products/" + fileName);
            }
            
            if (product.getId() == null) {
                productService.createProduct(product);
                redirectAttributes.addFlashAttribute("success", "Product created successfully");
            } else {
                productService.updateProduct(product);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            }
            
            return "redirect:/admin/products";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error uploading image: " + e.getMessage());
            return "admin/products/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
            return "admin/products/form";
        }
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "admin/products/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get product to delete its image if exists
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent() && product.get().getImageUrl() != null) {
                String imagePath = product.get().getImageUrl().replace("/images/products/", "");
                Path filePath = Paths.get(UPLOAD_DIR + imagePath);
                Files.deleteIfExists(filePath);
            }
            
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}