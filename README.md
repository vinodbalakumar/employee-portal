# Tesla Dashboard Services

Spring Boot backend for Tesla dashboard APIs. It validates Sharity JWT access tokens and calls Tesla Fleet API / Tesla proxy for vehicle data and commands.

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- MySQL 8
- Flyway migrations
- Jakarta Validation
- Lombok
- RestTemplate with Apache HttpClient 5
- Jackson 3 `tools.jackson.databind.JsonNode`

## Runtime Role

This service does not create JWTs. It only validates JWTs using the public PEM key.

The authorization server signs tokens:

```text
jwt-private.pem
```

This service validates tokens:

```text
jwt-public.pem
```

## Local Run

Default local values:

```text
Database: tesla
MySQL host: localhost
MySQL port: 3306
Username: root
Password: root
JWT public key: C:/Users/HP/projects/certs/jwt-public.pem
Audience: auth-clients
```

Run:

```powershell
mvn spring-boot:run
```

Local URL:

```text
http://localhost:8081/tesla-dashboard-services/api
```

## Docker Deployment

Deployment files are centralized in:

```text
C:\Users\HP\projects\deployments
```

Deploy this service only:

```powershell
cd C:\Users\HP\projects\deployments
docker compose up -d --build tesla-dashboard-services
```

Deploy all services:

```powershell
cd C:\Users\HP\projects\deployments
docker compose up -d --build
```

The container runs on internal port `8080`. Nginx exposes it publicly.

Public route:

```text
https://vinodbalakumar.com/tesla-dashboard-services/api
http://localhost:8080/tesla-dashboard-services/api
```

## JWT Public Key

Docker Compose mounts the PC cert folder:

```yaml
volumes:
  - C:/Users/HP/projects/certs:/keys:ro
```

This service receives:

```text
JWT_PUBLIC_KEY_PATH=/keys/jwt-public.pem
JWT_AUDIENCES=auth-clients
```

Every protected request must include:

```http
Authorization: Bearer <access-token>
```

## API URLs

All URLs below include the servlet context path.

| Method | URL | Purpose |
| --- | --- | --- |
| `GET` | `/tesla-dashboard-services/api/hello` | Public test endpoint |
| `GET` | `/tesla-dashboard-services/api/vehicles` | Fetch Tesla vehicle |
| `GET` | `/tesla-dashboard-services/api/status` | Combined lock and charging status |
| `GET` | `/tesla-dashboard-services/api/status/lock` | Lock status |
| `GET` | `/tesla-dashboard-services/api/status/charging` | Charging status |
| `POST` | `/tesla-dashboard-services/api/wake` | Wake vehicle |
| `POST` | `/tesla-dashboard-services/api/flash-lights` | Flash lights |
| `POST` | `/tesla-dashboard-services/api/honk` | Honk horn |
| `POST` | `/tesla-dashboard-services/api/lock` | Lock doors |
| `POST` | `/tesla-dashboard-services/api/unlock` | Unlock doors |
| `POST` | `/tesla-dashboard-services/api/climate/start` | Start climate |
| `POST` | `/tesla-dashboard-services/api/climate/stop` | Stop climate |
| `POST` | `/tesla-dashboard-services/api/trunk/open` | Open trunk |
| `POST` | `/tesla-dashboard-services/api/frunk/open` | Open frunk |
| `POST` | `/tesla-dashboard-services/api/start/charging` | Start charging |
| `POST` | `/tesla-dashboard-services/api/stop/charging` | Stop charging |
| `POST` | `/tesla-dashboard-services/api/cmd?command=<name>` | Run custom command |

Example:

```http
GET /tesla-dashboard-services/api/status
Authorization: Bearer <access-token>
```

## Tesla Configuration

Runtime variables:

```text
TESLA_FLEET_API_BASE=https://fleet-api.prd.na.vn.cloud.tesla.com
TESLA_PROXY_BASE=https://host.docker.internal:4443
TESLA_AUTH_TOKEN_URL=https://auth.tesla.com/oauth2/v3/token
TESLA_VEHICLE_ID_OR_VIN=
TESLA_DEFAULT_TOKEN_ID=1
TESLA_TOKEN_REFRESH_SKEW_MINUTES=2
```

The Tesla proxy runs on the PC. Docker containers reach it with:

```text
https://host.docker.internal:4443
```

## Database

MySQL runs on the PC, not in Docker:

```text
DB_HOST=host.docker.internal
DB_PORT=3306
DB_NAME=tesla
DB_USERNAME=root
DB_PASSWORD=root
```

Flyway migrations are in:

```text
src/main/resources/db/migration
```

## Useful Commands

```powershell
mvn test
mvn -DskipTests compile
docker compose logs -f tesla-dashboard-services
```
