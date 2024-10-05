# employee-portal



# JWT Authentication Service 

This project demonstrates a simple JWT (JSON Web Token) authentication service built with Spring Boot. It provides endpoints for user login and token generation, as well as mechanisms for validating JWT tokens to secure access to protected resources.

## Table of Contents

- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Configuration](#configuration)
- [Endpoints](#endpoints)
- [Testing the JWT Authentication](#testing-the-jwt-authentication) stateless jwt token: means not managing session with server
- [License](#license)

## Technologies Used

- **Spring Boot**: Framework for building Java-based web applications.
- **Spring Security**: Provides authentication and authorization features.
- **jjwt**: Java library for creating and verifying JWT tokens.
- **H2 Database**: In-memory database for testing and development.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/jwt-authentication-service.git
   cd jwt-authentication-service

2. Ensure you have Java 11 or later and Maven installed.

3. Build the project:

bash

mvn clean install

4. Run the application:

bash

mvn spring-boot:run


## Configuration

The JwtUtil class is responsible for generating and validating JWT tokens. The secret key used for signing tokens is set in the SECRET_KEY variable. Ensure you change it to a secure key in a production environment.

The SecurityConfig class configures Spring Security to use JWT for authentication. You can modify the authentication rules in the configure(HttpSecurity http) method.

## Endpoints
1. Login
   Endpoint: /auth/login
   Method: POST
   Request Body:
   http
   Copy code
   username=user&password=pass
   Response:
   On successful login, returns a JWT token:
   json
   Copy code
   {
   "token": "your_jwt_token"
   }
   On failure, returns a 401 status with a message: "Invalid credentials".

2. Protected Resource
   Endpoint: /protected/resource
   Method: GET
   Headers:
   http
   Copy code
   Authorization: Bearer your_jwt_token
   Response:
   Returns the protected resource if the token is valid.
   Testing the JWT Authentication
   Login: Send a POST request to /auth/login with the username and password.

http

POST /auth/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=user&password=pass
A successful response will return a JWT token.

Access Protected Resource: Use the returned token to access protected endpoints.

When submitting a login form, you might send a POST request like this:

http
Copy code
POST /login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=user%20name&password=p%40ssword%26123
The server would then correctly interpret user name as the username and p@ssword&123 as the password.

Conclusion
Using URL encoding when passing usernames and passwords ensures that the data is transmitted safely and accurately, avoiding any misinterpretations by the server. It’s a critical step in ensuring reliable and secure communication between clients and servers.

http

GET /protected/resource HTTP/1.1
Authorization: Bearer your_jwt_token

## License
This project is licensed under the VinodBalakumar License - see the LICENSE file for details.


### Instructions

- Replace `https://github.com/vinodbalakumar/employee-portal.git` with the actual repository URL if you're hosting it on GitHub.
- You can adjust the endpoints, request/response details, and other sections based on your specific implementation or requirements.
- Include additional sections as necessary, such as deployment instructions or testing information, if applicable.

Feel free to modify this `README.md` template to suit your project's needs! Let me know if you have any other requests.
