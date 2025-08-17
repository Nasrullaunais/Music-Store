package com.music.musicstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.music.musicstore.models.Customer;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAllByRole(String role);
    List<Customer> findAllByUsernameContainingIgnoreCase(String username);
    List<Customer> findAllByEmailContainingIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByRole(String role);

    void deleteByUsername(String username);
    void deleteByEmail(String email);
}
