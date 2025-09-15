package com.music.musicstore.repositories;

import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by customer
    List<Ticket> findByCustomer(Customer customer);
    Page<Ticket> findByCustomer(Customer customer, Pageable pageable);

    // Find tickets by status
    List<Ticket> findByStatus(String status);
    Page<Ticket> findByStatus(String status, Pageable pageable);

    // Count tickets by status
    long countByStatus(String status);

    // Count tickets by multiple statuses
    long countByStatusIn(List<String> statuses);

    // Find tickets by customer and status
    List<Ticket> findByCustomerAndStatus(Customer customer, String status);
    List<Ticket> findByCustomerAndStatusIn(Customer customer, List<String> statuses);

    // Search functionality
    List<Ticket> findBySubjectContainingIgnoreCaseOrMessageContainingIgnoreCase(String subject, String message);
    Page<Ticket> findBySubjectContainingIgnoreCaseOrMessageContainingIgnoreCase(String subject, String message, Pageable pageable);

    // Find tickets by customer with search
    @Query("SELECT t FROM Ticket t WHERE t.customer = :customer AND (LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Ticket> findByCustomerAndSearchTerm(@Param("customer") Customer customer, @Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Ticket t WHERE t.customer = :customer AND (LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> findByCustomerAndSearchTerm(@Param("customer") Customer customer, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Find tickets by order
    List<Ticket> findByOrder_Id(Long orderId);

    // Find urgent tickets
    @Query("SELECT t FROM Ticket t WHERE t.status = 'URGENT' ORDER BY t.id DESC")
    List<Ticket> findUrgentTickets();

    // Find tickets needing attention (OPEN, URGENT, REPLIED)
    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'URGENT', 'REPLIED') ORDER BY CASE WHEN t.status = 'URGENT' THEN 1 WHEN t.status = 'REPLIED' THEN 2 ELSE 3 END, t.id DESC")
    List<Ticket> findTicketsNeedingAttention();

    @Query("SELECT t FROM Ticket t WHERE t.status IN ('OPEN', 'URGENT', 'REPLIED') ORDER BY CASE WHEN t.status = 'URGENT' THEN 1 WHEN t.status = 'REPLIED' THEN 2 ELSE 3 END, t.id DESC")
    Page<Ticket> findTicketsNeedingAttention(Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer = :customer")
    long countByCustomer(@Param("customer") Customer customer);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer = :customer AND t.status = :status")
    long countByCustomerAndStatus(@Param("customer") Customer customer, @Param("status") String status);

    // Find recent tickets
    @Query("SELECT t FROM Ticket t ORDER BY t.id DESC")
    Page<Ticket> findRecentTickets(Pageable pageable);

    // Custom queries for analytics
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> getStatusDistribution();

    @Query("SELECT DATE(t.id), COUNT(t) FROM Ticket t GROUP BY DATE(t.id) ORDER BY DATE(t.id) DESC")
    List<Object[]> getTicketCountByDate();
}
