package com.music.musicstore.services;

import com.music.musicstore.models.Customer;
import com.music.musicstore.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // Import this

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with username: " + username));
    }


    public Customer registerCustomer(Customer customer) {
        if (customerRepository.findByUsername(customer.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists!");
        }
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }
        customerRepository.findById(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customer.getId()));
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    public Optional<Customer> authenticateUser(String username, String rawPassword) {
        return customerRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public Customer updateUser(Customer customer) {
        customerRepository.findById(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customer.getId()));

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }

        return customerRepository.save(customer);
    }


}
