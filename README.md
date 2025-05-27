# E-Commerce Application

A comprehensive e-commerce application built with Spring Boot, featuring product management, user authentication, shopping cart functionality, order processing, analytics, and a recommendation system.

## Technology Stack

- **Backend**: Spring Boot 3.3.2, Java 21
- **Security**: Spring Security
- **Database**: PostgreSQL
- **Frontend**: Thymeleaf, Tailwind CSS
- **Additional Features**: WebSockets for real-time communication, Email support

## Features

### Core Features
- User authentication and authorization
- Product management
- Shopping cart functionality
- Order processing
- User profile management
- Admin dashboard
- Analytics and reporting

### Recommendation System

The application includes a personalized product recommendation system that suggests products to users based on their browsing and purchase history. The recommendation system uses collaborative filtering techniques to provide relevant product suggestions.

#### How the Recommendation System Works

1. **Data Collection**: The system collects data on user behavior, including:
   - Products viewed
   - Products purchased
   - Categories of interest

2. **Recommendation Generation**: Based on this data, the system generates personalized recommendations using:
   - Collaborative filtering based on purchase history
   - Category-based recommendations
   - Popular product recommendations for new users

3. **Implementation Details**:
   - Recommendations are stored in a dedicated database table
   - Recommendations are updated daily via a scheduled job
   - The system falls back to popular products when personalized recommendations aren't available

4. **User Interface**:
   - Recommended products are displayed on the home page
   - A dedicated recommendations page shows all personalized recommendations
   - Product detail pages show related product recommendations

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL
- Maven

### Installation
1. Clone the repository
2. Configure the database connection in `application.properties`
3. Run `mvn clean install` to build the project
4. Run `mvn spring-boot:run` to start the application
5. Access the application at `http://localhost:8080`

## License
This project is licensed under the MIT License - see the LICENSE file for details.
