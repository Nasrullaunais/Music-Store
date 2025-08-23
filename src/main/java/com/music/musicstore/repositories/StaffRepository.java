package com.music.musicstore.repositories;

import com.music.musicstore.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByName(String name);
}
