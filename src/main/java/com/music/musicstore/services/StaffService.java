package com.music.musicstore.services;

import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffService {
    private StaffRepository staffRepository;

    @Autowired
    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public UserDetails loadUserByUsername(String username) {
        return staffRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Staff not found with username: " + username));
    }
}
