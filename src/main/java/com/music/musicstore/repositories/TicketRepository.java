package com.music.musicstore.repositories;

import com.music.musicstore.models.support.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
