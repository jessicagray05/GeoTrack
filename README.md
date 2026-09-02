# GeoTrack

### Project & Site Investigation Management System

GeoTrack is a full-stack web application I built to manage projects and site investigations in one place.

The application allows users to create and manage projects, track site investigations, monitor project deadlines and access functionality based on their role.

I built GeoTrack using Java 21, Spring Boot and PostgreSQL, with a HTML/CSS/JavaScript frontend.

## Features

- 📊 Project management dashboard
- 📁 Create, edit, view and delete projects
- 🔎 Search and filter projects
- 🏗️ Create and manage site investigations
- 🔗 Link site investigations to projects
- 📅 Project start and completion dates
- 🚦 Deadline tracking
- 🔐 Authentication and role-based access control
- 👑 Admin and Staff permissions
- 🛡️ Input validation and error handling
- 🌐 REST API
- 🗄️ PostgreSQL database

## Security

GeoTrack uses Spring Security to control access to different parts of the application.

Administrators have access to restricted management functionality, while Staff users have more limited permissions.

Security tests verify that protected functionality cannot be accessed by users without the required permissions.

## Technology

**Backend:** Java 21, Spring Boot, Spring Data JPA, Hibernate, Spring Security

**Database:** PostgreSQL

**Frontend:** HTML, CSS, JavaScript

**Testing:** JUnit, Mockito, MockMvc

**Tools:** Maven, Git, GitHub, Visual Studio Code

## Testing

Testing was included throughout development to make sure the main functionality and security rules work correctly.

### Current test results

**19 tests passed  
0 failures  
0 errors**

Tests cover:

- Project functionality
- Site investigation validation
- Security and role permissions
- Missing project handling
- Input validation

Run the tests with:

```bash
./mvnw clean test
Architecture

GeoTrack uses a layered Spring Boot architecture:

Frontend
   ↓
Controllers
   ↓
Services
   ↓
Repositories
   ↓
PostgreSQL

This keeps the application's frontend, business logic and database access separated.

Running the Project

Clone the repository:

git clone https://github.com/jessicagray05/GeoTrack.git
cd GeoTrack

Create a PostgreSQL database called:

geotrack

Set your database password as an environment variable:

export DB_PASSWORD="your_database_password"

Then run:

./mvnw spring-boot:run

The application will run at:

http://localhost:8080
Future Improvements

Some features I would like to add in the future include:

Document uploads
More detailed reporting
User management
Audit logging
Docker support
Cloud deployment
CI/CD
About

GeoTrack is a software engineering portfolio project I built to gain experience developing a complete application rather than just individual programming exercises.

It gave me experience working across the frontend, backend, database, security and testing side of a project.

Built by Jessica Gray
