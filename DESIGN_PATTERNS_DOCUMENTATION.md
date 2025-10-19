# Design Patterns Documentation - Music Store Application

## 🎯 Core Patterns Taught in Class

This project extensively implements **two key design patterns** that were taught in the course:

### 1. **Factory Pattern** (Creational)
- Creates objects without exposing instantiation logic
- **Where it's used**: Spring `@Bean` methods, SecurityFilterChain creation, PasswordEncoder factory
- **See detailed explanation**: [Section 7 - Factory Pattern](#7-factory-pattern)

### 2. **Strategy Pattern** (Behavioral)
- Defines interchangeable algorithms/behaviors
- **Where it's used**: File storage strategies, different user authentication strategies, deletion fallback strategies
- **See detailed explanation**: [Section 9 - Strategy Pattern](#9-strategy-pattern)

---

## 📚 Additional Patterns Implemented

Beyond the two core patterns taught in class, this project also demonstrates several other industry-standard design patterns that are commonly used in Spring Boot applications:

1. [MVC (Model-View-Controller) Pattern](#1-mvc-pattern)
2. [Repository Pattern](#2-repository-pattern)
3. [Service Layer Pattern](#3-service-layer-pattern)
4. [Dependency Injection Pattern](#4-dependency-injection-pattern)
5. [DTO (Data Transfer Object) Pattern](#5-dto-pattern)
6. [Singleton Pattern](#6-singleton-pattern)
7. [Factory Pattern](#7-factory-pattern)
8. [Chain of Responsibility Pattern](#8-chain-of-responsibility-pattern)
9. [Strategy Pattern](#9-strategy-pattern)
10. [Observer Pattern](#10-observer-pattern)
11. [Facade Pattern](#11-facade-pattern)
12. [Builder Pattern](#12-builder-pattern)
13. [Template Method Pattern](#13-template-method-pattern)
14. [Decorator Pattern](#14-decorator-pattern)
15. [Proxy Pattern](#15-proxy-pattern)

---

## 1. MVC (Model-View-Controller) Pattern

### Why It's Used
The MVC pattern separates the application into three interconnected components, promoting separation of concerns and making the codebase more maintainable and scalable.

### Why This Music Store Needs MVC Pattern
Our Music Store requires clear separation between data (music tracks, user info, orders), business logic (purchase processing, authentication), and presentation (REST API responses), allowing frontend developers and backend developers to work independently and making the system easier to test and maintain.

### Implementation
- **Model**: Entity classes representing database tables
- **Controller**: REST API Controllers handling HTTP requests
- **View**: JSON responses (REST API)

### Code Example

**Model (Entity)**:
```java
// filepath: src/main/java/com/music/musicstore/models/music/Music.java
@Entity
@Table(name = "music")
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Artist username is required")
    @Column(name = "artist_username", nullable = false)
    private String artistUsername;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Getters and setters...
}
```

**Controller**:
```java
// filepath: src/main/java/com/music/musicstore/api/AdminApiController.java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminApiController {
    @Autowired
    private UnifiedUserService unifiedUserService;

    @Autowired
    private MusicService musicService;

    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UnifiedRegisterRequest request,
                                      @AuthenticationPrincipal UserDetails currentUser,
                                      HttpServletRequest httpRequest) {
        UserDto userDto = unifiedUserService.createUser(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getRole(),
            request.getFirstName(),
            request.getLastName(),
            request.getArtistName(),
            request.getCover()
        );
        return ResponseEntity.ok(userDto);
    }
}
```

---

## 2. Repository Pattern

### Why It's Used
The Repository pattern abstracts data access logic, providing a clean API for data operations. It decouples business logic from data access concerns and makes testing easier.

### Why This Music Store Needs Repository Pattern
With numerous database tables (music tracks, orders, customers, artists, reviews, carts), the Repository Pattern provides a clean interface to query and manipulate data without cluttering business logic with SQL code, enabling easy testing and potential database technology changes.

### Implementation
Spring Data JPA repositories extend `JpaRepository` to provide CRUD operations and custom query methods.

### Code Example

```java
// filepath: src/main/java/com/music/musicstore/repositories/MusicRepository.java
@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    // Soft delete aware queries - exclude deleted music by default
    List<Music> findByCategoryAndIsDeletedFalse(String category);

    List<Music> findByArtistUsernameAndIsDeletedFalse(String artistUsername);

    Page<Music> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    Page<Music> findByArtistUsernameContainingIgnoreCaseAndIsDeletedFalse(String artistUsername, Pageable pageable);

    // Combined search - exclude deleted
    Page<Music> findByNameContainingIgnoreCaseOrArtistUsernameContainingIgnoreCaseAndIsDeletedFalse(
            String name, String artistUsername, Pageable pageable);

    Optional<Music> findByNameAndIsDeletedFalse(String name);

    @Query("SELECT m FROM Music m WHERE m.isFlagged = true AND m.isDeleted = false")
    List<Music> findFlaggedMusic();
}
```

**Benefits**:
- Clean separation between business logic and data access
- Automatic query generation from method names
- Support for custom JPQL queries
- Built-in pagination and sorting

---

## 3. Service Layer Pattern

### Why It's Used
The Service Layer pattern encapsulates business logic, coordinating between controllers and repositories. It ensures business rules are centralized and reusable.

### Why This Music Store Needs Service Layer Pattern
Our Music Store has complex business operations like processing purchases (checking cart, creating orders, sending receipts, clearing cart) and managing music deletion (checking dependencies, deleting reviews, removing files), which require coordinating multiple repositories and enforcing business rules in a centralized, reusable manner.

### Implementation
Service classes annotated with `@Service` contain business logic and orchestrate multiple repositories.

### Code Example

```java
// filepath: src/main/java/com/music/musicstore/services/MusicService.java
@Service
public class MusicService {
    private static final Logger logger = LoggerFactory.getLogger(MusicService.class);

    private final MusicRepository musicRepository;
    private final FileStorageService fileStorageService;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository,
                       FileStorageService fileStorageService,
                       OrderItemRepository orderItemRepository,
                       CartItemRepository cartItemRepository,
                       ReviewRepository reviewRepository) {
        this.musicRepository = musicRepository;
        this.fileStorageService = fileStorageService;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<Music> getAllMusic() {
        return musicRepository.findByIsDeletedFalse();
    }

    public Music getMusicById(Long id) {
        return musicRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Music not found with id: " + id));
    }

    // Complex business logic coordinating multiple operations
    public void deleteMusic(Long id) throws Exception {
        Music music = getMusicById(id);
        
        // Check for dependencies
        long orderCount = orderItemRepository.countByMusicId(id);
        long cartCount = cartItemRepository.countByMusicId(id);
        
        if (orderCount > 0 || cartCount > 0) {
            throw new BusinessRuleException("Cannot delete music that has been purchased or is in carts");
        }
        
        // Delete associated reviews
        reviewRepository.deleteByMusicId(id);
        
        // Delete music file
        if (music.getAudioFilePath() != null) {
            fileStorageService.deleteFile(music.getAudioFilePath());
        }
        
        musicRepository.deleteById(id);
        logger.info("Successfully deleted music with ID: {}", id);
    }
}
```

---

## 4. Dependency Injection Pattern

### Why It's Used
Dependency Injection (DI) promotes loose coupling by removing hard-coded dependencies. Spring Framework manages object creation and injection, making the application more testable and maintainable.

### Why This Music Store Needs Dependency Injection Pattern
Since services depend on multiple repositories and other services (e.g., OrderService needs CartService, EmailSender, OrderRepository), Dependency Injection automatically provides these dependencies, making testing easier by allowing mock objects and reducing boilerplate instantiation code.

### Implementation
Spring's `@Autowired` annotation or constructor injection is used throughout the application.

### Code Example

**Constructor Injection (Preferred)**:
```java
// filepath: src/main/java/com/music/musicstore/services/OrderService.java
@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final EmailSender emailSender;
    private final CustomerService customerService;

    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       CartService cartService, 
                       CartItemRepository cartItemRepository, 
                       EmailSender emailSender, 
                       CustomerService customerService,
                       CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.emailSender = emailSender;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    public void placeOrder(Customer customer) {
        Cart cart = cartService.getOrCreateCart(customer);
        if(cart.getItems().isEmpty()){
            throw new IllegalStateException("Cannot place order with empty cart");
        }
        Order order = new Order(cart);
        orderRepository.save(order);
        emailSender.sendReceipt(order.getTotalAmount(), itemList, customer.getEmail(), order.getId());
    }
}
```

**Field Injection**:
```java
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
    @Autowired
    private UnifiedUserService unifiedUserService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private AuditLogService auditLogService;
}
```

---

## 5. DTO (Data Transfer Object) Pattern

### Why It's Used
DTOs transfer data between layers without exposing internal domain models. They prevent over-fetching, protect sensitive data, and provide a clean API contract.

### Why This Music Store Needs DTO Pattern
When sending music data to the frontend, we need to include additional computed fields (average rating, total reviews) and hide sensitive internal fields (database IDs, soft delete flags), while also shaping the API response to match frontend requirements without altering our database entities.

### Implementation
Separate DTO classes for request/response objects with mapping logic.

### Code Example

**DTO Class**:
```java
// filepath: src/main/java/com/music/musicstore/dto/MusicDto.java
public class MusicDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String audioFilePath;
    private String originalFileName;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Music-specific fields
    private String artist;
    private String artistUsername;
    private String album;
    private String albumName;
    private String genre;
    private Integer releaseYear;

    // Rating fields
    private Double averageRating;
    private Integer totalReviews;

    // Status fields
    private Boolean isFlagged;
    private LocalDateTime flaggedAt;

    // Constructors, getters, and setters...
}
```

**Mapper Component**:
```java
// filepath: src/main/java/com/music/musicstore/dto/TicketMapper.java
@Component
public class TicketMapper {
    @Autowired
    private TicketMessageMapper ticketMessageMapper;

    public TicketDto toDto(Ticket ticket) {
        if (ticket == null) return null;
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setStatus(ticket.getStatus());

        if (ticket.getCreatedAt() != null) {
            ZonedDateTime zdt = ZonedDateTime.of(ticket.getCreatedAt(), ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("UTC"));
            dto.setCreatedAt(zdt.format(DateTimeFormatter.ISO_INSTANT));
        }

        Customer customer = ticket.getCustomer();
        if (customer != null) {
            UserSummaryDto c = new UserSummaryDto(customer.getId(), customer.getUsername(), "CUSTOMER");
            dto.setCustomer(c);
        }

        return dto;
    }
}
```

---

## 6. Singleton Pattern

### Why It's Used
Ensures a class has only one instance and provides a global point of access. Spring beans are singletons by default, reducing memory footprint and ensuring consistent state.

### Why This Music Store Needs Singleton Pattern
Critical components like JWT token utilities, email senders, and file storage services should have only one instance shared across the entire application to conserve memory, maintain consistent configuration (like JWT secrets), and avoid redundant object creation for thousands of requests.

### Benefits
- ✅ **Memory Efficiency**: Only one instance exists throughout the application lifecycle, reducing memory consumption especially for stateless services handling thousands of concurrent requests
- ✅ **Consistent Configuration**: Singleton ensures all parts of the application use the same configuration (e.g., same JWT secret, same email server settings), preventing configuration inconsistencies
- ✅ **Thread-Safe Shared Resources**: Spring-managed singletons are thread-safe by default, allowing multiple requests to safely share the same service instance without synchronization issues

### Implementation
Spring manages singleton beans through the IoC container. All `@Service`, `@Repository`, `@Controller`, and `@Component` annotated classes are singletons by default.

### Code Example

```java
// filepath: src/main/java/com/music/musicstore/configs/JwtUtil.java
@Component  // Singleton by default
public class JwtUtil {
    @Value("${jwt.secret:AsCbibFzH8thaWH+aqQPwyvuGZuv/EkUfQZHLkl81KU}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

**Email Service Singleton**:
```java
// filepath: src/main/java/com/music/musicstore/services/EmailSender.java
@Service  // Singleton bean
public class EmailSender {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReceiptEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nasrullaunais@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
```

---

## 7. Factory Pattern ✨ (TAUGHT IN CLASS)

### Why It's Used
The Factory pattern creates objects without exposing the creation logic to the client. Instead of using `new` keyword directly, we use a factory method that decides which object to create.

**Real-world analogy**: Like a restaurant kitchen - you order a pizza, but you don't know how it's made. The kitchen (factory) handles the creation.

### Why This Music Store Needs Factory Pattern
In our Music Store, we have **multiple user types** (Customer, Artist, Staff, Admin) and **complex security configurations** that need to be created consistently. The Factory Pattern ensures these objects are created correctly without duplicating complex initialization logic throughout the codebase, making it easy to add new user roles or change security settings in one place.

### Benefits
- ✅ Hides complex object creation logic
- ✅ Makes code more flexible and maintainable
- ✅ Easy to add new types without changing existing code

### Implementation in This Project

We use Factory Pattern in **configuration classes** where Spring creates and configures complex objects for us.

### 📌 Example 1: Password Encoder Factory (SIMPLEST)

This is the **easiest example** to understand:

```java
// filepath: src/main/java/com/music/musicstore/configs/PasswordConfig.java
@Configuration
public class PasswordConfig {
    
    // Factory method - creates a BCryptPasswordEncoder object
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Complex object creation hidden
    }
}
```

**How it's used:**
```java
@Service
public class CustomerService {
    @Autowired
    private PasswordEncoder passwordEncoder;  // Don't need to know HOW it's created
    
    public void registerCustomer(String password) {
        // Just use it! Factory handled the creation
        String encrypted = passwordEncoder.encode(password);
    }
}
```

**Why this is Factory Pattern:**
- ✅ The `passwordEncoder()` method is the **factory**
- ✅ It creates `BCryptPasswordEncoder` object
- ✅ Other classes don't need to know about `BCryptPasswordEncoder` - they just use `PasswordEncoder` interface
- ✅ If we want to change to a different encoder, we only change ONE place

---

### 📌 Example 2: Authentication Manager Factory

```java
// filepath: src/main/java/com/music/musicstore/configs/SecurityConfig.java
@Configuration
public class SecurityConfig {

    // Factory method - creates AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**How it's used:**
```java
@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;  // Factory created this!
    
    public String login(String username, String password) {
        // We use it without knowing how it was created
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        return generateToken(auth);
    }
}
```

---

### 📌 Example 3: User Creation Factory (BEST EXAMPLE)

This is the **best example** because it shows the classic Factory Pattern use case:

```java
// filepath: src/main/java/com/music/musicstore/services/UnifiedUserService.java
@Service
public class UnifiedUserService {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ArtistService artistService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private AdminService adminService;


    public UserDto createUser(String username, String password, String email, String role) {
        
        // Factory decides WHICH type of user to create
        switch (role.toUpperCase()) {
            case "CUSTOMER":
                return customerService.registerCustomer(username, password, email);
                
            case "ARTIST":
                return artistService.registerArtist(username, password, email);
                
            case "STAFF":
                return staffService.registerStaff(username, password, email);
                
            case "ADMIN":
                return adminService.registerAdmin(username, password, email);
                
            default:
                throw new ValidationException("Invalid role: " + role);
        }
    }
}
```

**How the Controller uses it:**
```java
@RestController
public class AdminApiController {
    @Autowired
    private UnifiedUserService userFactory;  // This is our factory!
    
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        // Controller doesn't need to know HOW to create each user type
        // Factory handles all the complexity!
        UserDto user = userFactory.createUser(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getRole()  // Just pass the role, factory does the rest
        );
        return ResponseEntity.ok(user);
    }
}
```

**Why this is the BEST example:**
- ✅ **Clear Factory Role**: `UnifiedUserService` is clearly a factory
- ✅ **Creates Different Types**: Customer, Artist, Staff, Admin - based on input
- ✅ **Hides Complexity**: Controller doesn't need to know how each user type is created
- ✅ **Easy to Extend**: Want to add "Moderator" role? Just add one more case!

---

### 🎯 Key Takeaway

**Factory Pattern in Simple Terms:**
```
Instead of:     Object obj = new ComplexObject(param1, param2, param3);
We use:         Object obj = factory.createObject(simpleParam);
```

The factory method handles all the complexity, and the client just gets what it needs!
```markdown
```
##9. Strategy Pattern ✨ (TAUGHT IN CLASS)

### Why It's Used
The Strategy pattern allows you to define multiple ways (strategies) to do the same task, and switch between them easily. Each strategy is interchangeable.

**Real-world analogy**: Like different payment methods - you can pay with cash, credit card, or mobile payment. The store accepts all, but the process is different.

### Why This Music Store Needs Strategy Pattern
Our Music Store application requires handling various operations in different ways depending on the context, such as storing files, deleting files, and authenticating users. The Strategy Pattern provides the flexibility to switch between these algorithms or methods at runtime, making the system more modular and easier to extend.

### Benefits
- ✅ Multiple algorithms for the same task
- ✅ Easy to add new strategies without changing existing code
- ✅ Cleaner code - each strategy is separate

### Implementation in This Project

We use Strategy Pattern when we have **different ways to accomplish the same goal**.

---

### 📌 Example 1: File Upload Strategies (BEST EXAMPLE)

This is the **clearest example** - we have TWO strategies for storing files:

```java
// filepath: src/main/java/com/music/musicstore/services/FileStorageService.java
@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    // STRATEGY 1: Simple storage (no subfolder)
    public String storeFile(MultipartFile file) throws IOException {
        // Just store in the main upload directory
        return storeFile(file, null);
    }

    // STRATEGY 2: Organized storage (with subfolder)
    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        Path uploadPath;
        
        if (subFolder == null || subFolder.isBlank()) {
            // Strategy 1: Store in main folder
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        } else {
            // Strategy 2: Store in subfolder (music/ or covers/)
            uploadPath = Paths.get(uploadDir).getParent()
                              .resolve(subFolder)
                              .toAbsolutePath()
                              .normalize();
        }

        Files.createDirectories(uploadPath);
        
        // Generate unique filename
        String uniqueFilename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Store the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFilename;
    }
}
```

**How it's used - CHOOSE the strategy:**
```java
@Service
public class MusicService {
    @Autowired
    private FileStorageService fileStorage;
    
    public void uploadMusic(MultipartFile audioFile, MultipartFile coverImage) {
        // Strategy 1: Store audio in "music" folder
        String audioPath = fileStorage.storeFile(audioFile, "music");
        // Strategy 2: Store cover in "covers" folder
        String coverPath = fileStorage.storeFile(coverImage, "covers");
    }
}
```

**Why this is Strategy Pattern:**
- ✅ **Same Goal**: Store a file
- ✅ **Different Strategies**: Main folder vs subfolder
- ✅ **Client Chooses**: Caller decides which strategy to use
- ✅ **Interchangeable**: Can switch strategies easily

---

### 📌 Example 2: File Deletion Strategies with Fallback

This shows **multiple fallback strategies** - try one, if it fails, try another:

```java
// filepath: src/main/java/com/music/musicstore/services/FileStorageService.java
public boolean deleteFile(String filename) {
    if (filename == null || filename.trim().isEmpty()) return false;
    
    try {
        // STRATEGY 1: Try main upload directory
        Path path1 = Paths.get(uploadDir).resolve(filename).normalize();
        if (Files.exists(path1)) {
            return Files.deleteIfExists(path1);
        }

        // STRATEGY 2: Try covers folder
        Path path2 = Paths.get(uploadDir).getParent()
                         .resolve("covers")
                         .resolve(filename)
                         .normalize();
        if (Files.exists(path2)) {
            return Files.deleteIfExists(path2);
        }

        // STRATEGY 3: Try music folder
        Path path3 = Paths.get("./uploads/music")
                         .toAbsolutePath()
                         .resolve(filename)
                         .normalize();
        if (Files.exists(path3)) {
            return Files.deleteIfExists(path3);
        }

        // STRATEGY 4: Try target/classes (production build)
        Path path4 = Paths.get("./target/classes/static/uploads/music")
                         .toAbsolutePath()
                         .resolve(filename)
                         .normalize();
        if (Files.exists(path4)) {
            return Files.deleteIfExists(path4);
        }

        return false;  // All strategies failed
        
    } catch (IOException e) {
        return false;
    }
}
```

**Why this is Strategy Pattern:**
- ✅ **Multiple Strategies**: Try 4 different locations
- ✅ **Same Goal**: Delete the file
- ✅ **Automatic Fallback**: If one fails, try next strategy
- ✅ **Flexible**: Easy to add more search locations

---

### 📌 Example 3: User Authentication Strategies (EXCELLENT EXAMPLE)

This shows choosing **different authentication strategies** based on user type:

```java
// filepath: src/main/java/com/music/musicstore/services/CombinedUserDetailsService.java
@Service
public class CombinedUserDetailsService implements UserDetailsService {
    @Autowired
    private AdminService adminService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ArtistService artistService;
    @Autowired
    private StaffService staffService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // STRATEGY 1: Try Admin authentication
        try {
            return adminService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e1) {
            
            // STRATEGY 2: Try Staff authentication
            try {
                return staffService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e2) {
                
                // STRATEGY 3: Try Artist authentication
                try {
                    return artistService.loadUserByUsername(username);
                } catch (UsernameNotFoundException e3) {
                    
                    // STRATEGY 4: Try Customer authentication
                    return customerService.loadUserByUsername(username);
                }
            }
        }
    }
}
```

**Simplified visualization:**
```
User tries to login
    ↓
Try Strategy 1: Is it an Admin? → No
    ↓
Try Strategy 2: Is it a Staff? → No
    ↓
Try Strategy 3: Is it an Artist? → No
    ↓
Try Strategy 4: Is it a Customer? → YES! ✓
```

**Why this is Strategy Pattern:**
- ✅ **Same Goal**: Authenticate user
- ✅ **Different Strategies**: Admin, Staff, Artist, Customer authentication
- ✅ **Runtime Decision**: Decides which strategy to use at runtime
- ✅ **Extensible**: Easy to add new user types (e.g., Moderator)

---

### 📌 Example 4: Email Content Strategies (SIMPLE)

Different strategies for building email content:

```java
// filepath: src/main/java/com/music/musicstore/services/EmailSender.java
@Service
public class EmailSender {
    private final JavaMailSender mailSender;

    // STRATEGY 1: Receipt email
    public void sendReceipt(BigDecimal amount, StringBuilder items, String email, String orderId) {
        String subject = "Your Purchase Receipt - Order #" + orderId;
        String body = "Thank you for your purchase!\n\n" +
                      "Order details:\n" + items.toString() +
                      "\nTotal: $" + amount;
        
        sendEmail(email, subject, body);
    }

    // STRATEGY 2: Status change email
    public void sendUserStatusChangeEmail(String email, String username, String newStatus) {
        String subject = "Account Status Update";
        String body = "Dear " + username + ",\n\n" +
                      "Your account status changed to: " + newStatus;
        
        sendEmail(email, subject, body);
    }

    // STRATEGY 3: Welcome email
    public void sendWelcomeEmail(String email, String username) {
        String subject = "Welcome to Music Store!";
        String body = "Hi " + username + ",\n\n" +
                      "Welcome to our music store!";
        
        sendEmail(email, subject, body);
    }

    // Common method used by all strategies
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
```

**How it's used - SELECT the strategy:**
```java
@Service
public class OrderService {
    @Autowired
    private EmailSender emailSender;
    
    public void placeOrder(Customer customer, Order order) {
        // Choose Strategy 1: Send receipt
        emailSender.sendReceipt(order.getTotal(), items, customer.getEmail(), order.getId());
    }
}

@Service
public class AdminService {
    @Autowired
    private EmailSender emailSender;
    
    public void banUser(Customer customer) {
        // Choose Strategy 2: Send status change
        emailSender.sendUserStatusChangeEmail(customer.getEmail(), customer.getUsername(), "BANNED");
    }
}

@Service
public class CustomerService {
    @Autowired
    private EmailSender emailSender;
    
    public void registerCustomer(Customer customer) {
        // Choose Strategy 3: Send welcome email
        emailSender.sendWelcomeEmail(customer.getEmail(), customer.getUsername());
    }
}
```

**Why this is Strategy Pattern:**
- ✅ **Same Goal**: Send email
- ✅ **Different Strategies**: Receipt, status change, welcome
- ✅ **Each strategy builds different content**
- ✅ **Client chooses** which strategy to use

---

### 🎯 Key Takeaway

**Strategy Pattern in Simple Terms:**
```
Problem: Need to do Task X, but there are multiple ways to do it

Solution: Create different strategies (methods/classes) for each way
         Client chooses which strategy to use

Example:
  Task: Store a file
  Strategy 1: Store in main folder
  Strategy 2: Store in subfolder
  → Client picks which one to use!
```

**Real Code Example:**
```java
// Without Strategy Pattern - Messy!
if (fileType.equals("music")) {
    // 20 lines of music storage code
} else if (fileType.equals("cover")) {
    // 20 lines of cover storage code
} else if (fileType.equals("document")) {
    // 20 lines of document storage code
}

// With Strategy Pattern - Clean!
fileStorage.storeFile(file, fileType);  // Strategy handles it!
```

The key is: **Same goal, different approaches, client chooses!**

---
