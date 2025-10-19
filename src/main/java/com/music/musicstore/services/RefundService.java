package com.music.musicstore.services;

import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.order.RefundRequest;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.OrderRepository;
import com.music.musicstore.repositories.RefundRequestRepository;
import com.music.musicstore.exceptions.BusinessRuleException;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RefundService {

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Customer applies for refund within 24 hours of order
     */
    @Transactional
    public RefundRequest applyForRefund(Long orderId, Customer customer, String reason) {
        // Find the order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Verify the order belongs to the customer
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("You can only request refund for your own orders");
        }

        // Check if order is already cancelled or refunded
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessRuleException("This order is already cancelled");
        }

        // Check if refund request already exists
        if (refundRequestRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessRuleException("A refund request already exists for this order");
        }

        // Validate 24-hour window
        long hoursSinceOrder = ChronoUnit.HOURS.between(order.getOrderDate(), LocalDateTime.now());
        if (hoursSinceOrder > 24) {
            throw new BusinessRuleException("Refund requests can only be made within 24 hours of order placement. " +
                    "Your order was placed " + hoursSinceOrder + " hours ago.");
        }

        // Create refund request
        RefundRequest refundRequest = new RefundRequest(
            order,
            customer,
            reason,
            order.getTotalAmount()
        );

        return refundRequestRepository.save(refundRequest);
    }

    /**
     * Get all refund requests for a customer
     */
    public List<RefundRequest> getCustomerRefundRequests(Customer customer) {
        return refundRequestRepository.findByCustomer(customer);
    }

    /**
     * Get refund request by ID
     */
    public RefundRequest getRefundRequestById(Long id) {
        return refundRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Refund request not found with id: " + id));
    }

    /**
     * Get all refund requests (admin)
     */
    public Page<RefundRequest> getAllRefundRequests(Pageable pageable) {
        return refundRequestRepository.findAllByOrderByRequestDateDesc(pageable);
    }

    /**
     * Get pending refund requests (admin)
     */
    public List<RefundRequest> getPendingRefundRequests() {
        return refundRequestRepository.findByStatusOrderByRequestDateAsc(RefundRequest.RefundStatus.PENDING);
    }

    /**
     * Get refund requests by status (admin)
     */
    public Page<RefundRequest> getRefundRequestsByStatus(RefundRequest.RefundStatus status, Pageable pageable) {
        return refundRequestRepository.findByStatus(status, pageable);
    }

    /**
     * Admin approves refund request
     */
    @Transactional
    public RefundRequest approveRefund(Long refundRequestId, String adminUsername, String adminNotes) {
        RefundRequest refundRequest = getRefundRequestById(refundRequestId);

        // Check if already processed
        if (refundRequest.getStatus() != RefundRequest.RefundStatus.PENDING) {
            throw new BusinessRuleException("This refund request has already been " +
                refundRequest.getStatus().toString().toLowerCase());
        }

        // Update refund request status
        refundRequest.setStatus(RefundRequest.RefundStatus.APPROVED);
        refundRequest.setProcessedBy(adminUsername);
        refundRequest.setProcessedDate(LocalDateTime.now());
        refundRequest.setAdminNotes(adminNotes);

        // Update order status
        Order order = refundRequest.getOrder();
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Remove purchased music from customer
        // Customer should no longer own the music after refund is approved
        Customer customer = refundRequest.getCustomer();
        order.getOrderItems().forEach(orderItem -> {
            customer.getPurchasedMusic().remove(orderItem.getMusic());
        });

        return refundRequestRepository.save(refundRequest);
    }

    /**
     * Admin rejects refund request
     */
    @Transactional
    public RefundRequest rejectRefund(Long refundRequestId, String adminUsername, String adminNotes) {
        RefundRequest refundRequest = getRefundRequestById(refundRequestId);

        // Check if already processed
        if (refundRequest.getStatus() != RefundRequest.RefundStatus.PENDING) {
            throw new BusinessRuleException("This refund request has already been " +
                refundRequest.getStatus().toString().toLowerCase());
        }

        // Update refund request status
        refundRequest.setStatus(RefundRequest.RefundStatus.REJECTED);
        refundRequest.setProcessedBy(adminUsername);
        refundRequest.setProcessedDate(LocalDateTime.now());
        refundRequest.setAdminNotes(adminNotes);

        return refundRequestRepository.save(refundRequest);
    }

    /**
     * Get count of pending refund requests
     */
    public long getPendingRefundCount() {
        return refundRequestRepository.countByStatus(RefundRequest.RefundStatus.PENDING);
    }

    /**
     * Check if order is eligible for refund
     */
    public boolean isOrderEligibleForRefund(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // Check if within 24 hours
        long hoursSinceOrder = ChronoUnit.HOURS.between(order.getOrderDate(), LocalDateTime.now());
        if (hoursSinceOrder > 24) {
            return false;
        }

        // Check if order is not cancelled
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            return false;
        }

        // Check if refund request doesn't exist
        return refundRequestRepository.findByOrderId(orderId).isEmpty();
    }
}
