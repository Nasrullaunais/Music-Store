package com.music.musicstore.repositories;

import com.music.musicstore.models.Customer;
import com.music.musicstore.models.Music;
import com.music.musicstore.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> findByStatus(String status, Pageable pageable);

    List<Order> findByMusic(Music music);

    List<Order> findByCustomerAndStatus(Customer customer, String status);
    List<Order> findByCustomerAndOrderDateBetween(Customer customer, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByCustomerAndMusic(Customer customer, Music music);

    List<Order> findByCustomerAndOrderDateBetweenAndMusic(Customer customer, LocalDateTime startDate, LocalDateTime endDate, Music music);
}