package com.music.musicstore.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CombinedUserDetailsService implements UserDetailsService {
    private final AdminService adminService;
    private final CustomerService customerService;

    public CombinedUserDetailsService(AdminService adminService, CustomerService customerService) {
        this.adminService = adminService;
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return adminService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            return customerService.loadUserByUsername(username);
        }
    }
}
