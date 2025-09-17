package com.music.musicstore.services;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.ValidationException;
import com.music.musicstore.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("CustomerService initialized successfully");
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Customer not found with username: " + username));

            logger.info("Successfully loaded user: {}", username);
            return customer;
        } catch (Exception e) {
            logger.error("Error loading user by username: {}", username, e);
            throw e;
        }
    }

    public Customer findByUsername(String username) {
        logger.debug("Finding customer by username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }

        try {
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", username));

            logger.info("Successfully found customer: {}", username);
            return customer;
        } catch (Exception e) {
            logger.error("Error finding customer by username: {}", username, e);
            throw e;
        }
    }

    // Add missing count method for UnifiedUserService
    public long count() {
        logger.debug("Counting total customers");

        try {
            long count = customerRepository.count();
            logger.info("Total customer count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting customers", e);
            throw new RuntimeException("Failed to count customers", e);
        }
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        logger.debug("Creating new customer: {}", customer.getUsername());

        if (customer == null) {
            logger.error("Customer object is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            logger.error("Customer username is null or empty");
            throw new ValidationException("Customer username cannot be null or empty");
        }

        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            logger.error("Customer password is null or empty");
            throw new ValidationException("Customer password cannot be null or empty");
        }

        try {
            // Check if username already exists
            Optional<Customer> existingCustomer = customerRepository.findByUsername(customer.getUsername());
            if (existingCustomer.isPresent()) {
                logger.error("Username already exists: {}", customer.getUsername());
                throw new BusinessRuleException("Username already exists: " + customer.getUsername());
            }

            // Password is already encoded in UnifiedUserService, so don't encode it again
            Customer savedCustomer = customerRepository.save(customer);

            logger.info("Successfully created customer: {}", savedCustomer.getUsername());
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Error creating customer: {}", customer.getUsername(), e);
            throw e;
        }
    }

    public Customer save(Customer customer) {
        logger.debug("Saving customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer object is null");
            throw new ValidationException("Customer cannot be null");
        }

        try {
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Successfully saved customer: {}", savedCustomer.getUsername());
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Error saving customer: {}", customer.getUsername(), e);
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    public Customer findById(Long id) {
        logger.debug("Finding customer by ID: {}", id);

        if (id == null) {
            logger.error("Customer ID is null");
            throw new ValidationException("Customer ID cannot be null");
        }

        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

            logger.info("Successfully found customer by ID: {}", id);
            return customer;
        } catch (Exception e) {
            logger.error("Error finding customer by ID: {}", id, e);
            throw e;
        }
    }

    public Optional<Customer> findByIdOptional(Long id) {
        logger.debug("Finding customer by ID (optional): {}", id);

        if (id == null) {
            logger.error("Customer ID is null");
            throw new ValidationException("Customer ID cannot be null");
        }

        try {
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isPresent()) {
                logger.info("Successfully found customer by ID: {}", id);
            } else {
                logger.debug("Customer not found by ID: {}", id);
            }
            return customer;
        } catch (Exception e) {
            logger.error("Error finding customer by ID: {}", id, e);
            throw new RuntimeException("Failed to find customer by ID", e);
        }
    }

    public List<Customer> getAllCustomers() {
        logger.debug("Retrieving all customers");

        try {
            List<Customer> customers = customerRepository.findAll();
            logger.info("Successfully retrieved {} customers", customers.size());
            return customers;
        } catch (Exception e) {
            logger.error("Error retrieving all customers", e);
            throw new RuntimeException("Failed to retrieve customers", e);
        }
    }

    public void deleteCustomer(Long id) {
        logger.debug("Deleting customer with ID: {}", id);

        if (id == null) {
            logger.error("Customer ID is null");
            throw new ValidationException("Customer ID cannot be null");
        }

        try {
            // Check if customer exists before deletion
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isEmpty()) {
                logger.error("Customer not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException("Customer", id.toString());
            }

            customerRepository.deleteById(id);
            logger.info("Successfully deleted customer with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting customer with ID: {}", id, e);
            throw new RuntimeException("Failed to delete customer", e);
        }
    }

    public void updateCustomer(Customer customer) {
        logger.debug("Updating customer: {}", customer != null ? customer.getUsername() : "null");

        if (customer == null) {
            logger.error("Customer object is null");
            throw new ValidationException("Customer cannot be null");
        }

        if (customer.getId() == null) {
            logger.error("Customer ID is null for update");
            throw new ValidationException("Customer ID cannot be null for update");
        }

        try {
            Customer existingCustomer = customerRepository.findById(customer.getId())
                    .orElseThrow(() -> {
                        logger.error("Customer not found for update with ID: {}", customer.getId());
                        return new ResourceNotFoundException("Customer", customer.getId().toString());
                    });

            if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
                customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            }

            Customer updatedCustomer = customerRepository.save(customer);
            logger.info("Successfully updated customer: {} (ID: {})", updatedCustomer.getUsername(), updatedCustomer.getId());
        } catch (Exception e) {
            logger.error("Error updating customer: {}", customer.getUsername(), e);
            throw e;
        }
    }
}
