package com.music.musicstore.repositories;

import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByMusic(Music music);

    @Modifying
    @Transactional
    @Query("UPDATE OrderItem oi SET oi.music = NULL WHERE oi.music = :music")
    void nullifyMusicReference(@Param("music") Music music);
}
