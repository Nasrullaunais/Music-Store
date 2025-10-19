package com.music.musicstore.repositories;

import com.music.musicstore.models.order.RefundRequest;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    // Find refund requests by customer
    List<RefundRequest> findByCustomer(Customer customer);

    // Find refund requests by status
    Page<RefundRequest> findByStatus(RefundRequest.RefundStatus status, Pageable pageable);

    // Find all refund requests with pagination
    Page<RefundRequest> findAllByOrderByRequestDateDesc(Pageable pageable);

    // Find refund request by order ID
    Optional<RefundRequest> findByOrderId(Long orderId);

    // Find pending refund requests
    List<RefundRequest> findByStatusOrderByRequestDateAsc(RefundRequest.RefundStatus status);

    // Count pending refund requests
    long countByStatus(RefundRequest.RefundStatus status);
}

