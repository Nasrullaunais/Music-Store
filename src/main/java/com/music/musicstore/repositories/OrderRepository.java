package com.music.musicstore.repositories;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Order> findByCustomer(Customer customer, Pageable pageable);
}
