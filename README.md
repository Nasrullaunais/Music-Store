# Music Store E-Commerce Application

A Spring Boot application for an e-commerce music store with admin panel functionality.

## Features

- **Admin Panel**: Secure admin interface for managing products, orders, and users
- **Product Management**: Add, edit, and delete music products with details like artist, genre, and album
- **Order Management**: View and process customer orders with status tracking
- **User Authentication**: Secure login for administrators
- **Responsive Design**: Mobile-friendly interface using Bootstrap 5

## Technology Stack

- **Backend**: Java 17+ with Spring Boot 3.5.4
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Database**: PostgreSQL
- **Security**: Spring Security
- **Build Tool**: Gradle

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.6 or higher

## Setup Instructions

### Database Setup

1. Create a PostgreSQL database for the application
2. Update the database configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Building and Running the Application

1. Clone the repository:
```bash
git clone https://github.com/yourusername/music-store.git
cd music-store
```

2. Build the application:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew bootRun
```

4. Access the application at `http://localhost:8080`

### Default Admin Account

On first startup, the application creates a default admin account:
- Username: `admin`
- Password: `admin123`

**Important**: Change the default admin password after first login for security.

## Project Structure

```
src/main/java/com/pg128/musicstore/
├── config/             # Configuration classes
├── controllers/        # MVC controllers
├── models/             # Entity classes
├── repositories/       # Data access interfaces
├── services/           # Business logic
└── MusicStoreApplication.java  # Main application class

src/main/resources/
├── static/             # Static resources (CSS, JS, images)
├── templates/          # Thymeleaf templates
└── application.properties  # Application configuration
```

## Admin Panel Features

### Dashboard
- Overview of store statistics
- Recent orders display

### Product Management
- List all products with filtering and pagination
- Add new products with music-specific details
- Edit existing products
- Delete products

### Order Management
- View all orders with filtering by status, customer, and date
- Update order status
- View order details
- Cancel orders

## Future Enhancements

- Customer-facing storefront
- Shopping cart functionality
- Payment gateway integration
- User registration and profiles
- Product reviews and ratings
- Advanced search and filtering
- Sales reports and analytics

## License

This project is licensed under the MIT License - see the LICENSE file for details.