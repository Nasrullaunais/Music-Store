package com.music.musicstore.services;

import com.music.musicstore.dto.UserDto;
import com.music.musicstore.models.users.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UnifiedUserService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto createUser(String username, String password, String email, String role,
                             String firstName, String lastName, String artistName, String cover) {
        String encodedPassword = passwordEncoder.encode(password);

        switch (role.toUpperCase()) {
            case "CUSTOMER":
                Customer customer = new Customer();
                customer.setUsername(username);
                customer.setPassword(encodedPassword);
                customer.setEmail(email);
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                Customer savedCustomer = customerService.createCustomer(customer);
                return convertCustomerToDto(savedCustomer);

            case "ARTIST":
                Artist artist = new Artist();
                artist.setUserName(username);
                artist.setPassword(encodedPassword);
                artist.setEmail(email);
                if (artistName != null) artist.setArtistName(artistName);
                if (cover != null) artist.setCover(cover);
                Artist savedArtist = artistService.save(artist);
                return convertArtistToDto(savedArtist);

            case "STAFF":
                Staff staff = new Staff();
                staff.setUsername(username);
                staff.setPassword(encodedPassword);
                staff.setEmail(email);
                staff.setFirstName(firstName);
                staff.setLastName(lastName);
                Staff savedStaff = staffService.save(staff);
                return convertStaffToDto(savedStaff);

            case "ADMIN":
                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setPassword(encodedPassword);
                admin.setEmail(email);
                Admin savedAdmin = adminService.save(admin);
                return convertAdminToDto(savedAdmin);

            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public UserDto getUserInfo(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return switch (role) {
            case "ROLE_CUSTOMER" -> {
                Customer customer = customerService.findByUsername(username);
                yield convertCustomerToDto(customer);
            }
            case "ROLE_ARTIST" -> {
                Artist artist = artistService.findByUserName(username);
                yield convertArtistToDto(artist);
            }
            case "ROLE_STAFF" -> {
                Staff staff = staffService.findByUsername(username);
                yield convertStaffToDto(staff);
            }
            case "ROLE_ADMIN" -> {
                Admin admin = adminService.findByUsername(username);
                yield convertAdminToDto(admin);
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }

    // Additional methods for admin functionality
    public long getTotalUsersCount() {
        return customerService.count() + artistService.count() + staffService.count() + adminService.count();
    }

    private UserDto convertCustomerToDto(Customer customer) {
        return new UserDto(
            customer.getId(),
            customer.getUsername(),
            customer.getEmail(),
            "CUSTOMER",
            customer.getFirstName(),
            customer.getLastName()
        );
    }

    private UserDto convertArtistToDto(Artist artist) {
        UserDto dto = new UserDto(
            artist.getId(),
            artist.getUserName(),
            artist.getEmail(),
            "ARTIST"
        );
        dto.setArtistName(artist.getArtistName());
        dto.setCover(artist.getCover());
        return dto;
    }

    private UserDto convertStaffToDto(Staff staff) {
        return new UserDto(
            staff.getId(),
            staff.getUsername(),
            staff.getEmail(),
            "STAFF",
            staff.getFirstName(),
            staff.getLastName()
        );
    }

    private UserDto convertAdminToDto(Admin admin) {
        return new UserDto(
            admin.getId(),
            admin.getUsername(),
            admin.getEmail(),
            "ADMIN"
        );
    }
}
