package com.music.musicstore.configs;

import com.music.musicstore.models.*;
import com.music.musicstore.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            MusicRepository musicRepository,
            AdminRepository adminRepository,
            CustomerRepository customerRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            PasswordEncoder encoder
    ) {
        return args -> {
            // Seed admin if not present
            if (!adminRepository.existsByUsername("admin")) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                adminRepository.save(admin);
            }

            // Seed a demo customer
            Customer customer = customerRepository.findByUsername("jane").orElseGet(() -> {
                Customer c = new Customer();
                c.setUsername("jane");
                c.setFirstName("Jane");
                c.setLastName("Doe");
                c.setEmail("jane@example.com");
                c.setRole("ROLE_CUSTOMER");
                c.setPassword(encoder.encode("password"));
                return customerRepository.save(c);
            });

            // Seed musics if repository is empty
            if (musicRepository.count() == 0) {
                List<Music> musics = List.of(
                        createMusic("Laugh Track", "Funny laugh", new BigDecimal("1.99"), "Comedy", "Mixkit", "Album A", "Comedy", 2020, "static/musics/mixkit-crowd-laugh-424.wav", "https://picsum.photos/seed/1/400/200"),
                        createMusic("Sad Trombone", "Game over sound", new BigDecimal("2.49"), "SFX", "Mixkit", "Album B", "Effects", 2021, "static/musics/mixkit-sad-game-over-trombone-471.wav", "https://picsum.photos/seed/2/400/200"),
                        createMusic("Interface Remove", "UI sound", new BigDecimal("0.99"), "SFX", "Mixkit", "Album C", "Effects", 2022, "static/musics/mixkit-software-interface-remove-2576.wav", "https://picsum.photos/seed/3/400/200"),
                        createMusic("Cartoon Fart", "Classic gag sound", new BigDecimal("1.49"), "Comedy", "Mixkit", "Album D", "Comedy", 2019, "static/musics/mixkit-cartoon-fart-sound-2891.wav", "https://picsum.photos/seed/4/400/200")
                );
                musicRepository.saveAll(musics);
            }

            // Ensure customer has a cart with one item for demo
            Cart cart = cartRepository.findByCustomer(customer).orElseGet(() -> cartRepository.save(new Cart(customer)));
            if (cartItemRepository.findByCart(cart).isEmpty()) {
                musicRepository.findAll().stream().findFirst().ifPresent(m -> {
                    CartItem item = new CartItem(m, 1);
                    item.setCart(cart);
                    cartItemRepository.save(item);
                });
            }
        };
    }

    private static Music createMusic(String name, String desc, BigDecimal price, String category, String artist,
                                     String album, String genre, int year, String audioPath, String imageUrl) {
        Music m = new Music();
        m.setName(name);
        m.setDescription(desc);
        m.setPrice(price);
        m.setStockQuantity(999);
        m.setCategory(category);
        m.setArtist(artist);
        m.setAlbum(album);
        m.setGenre(genre);
        m.setReleaseYear(year);
        m.setImageUrl(imageUrl);
        m.setAudioFilePath(audioPath);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }
}
