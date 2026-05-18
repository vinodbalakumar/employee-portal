# Employee Portal

Spring Boot REST application with JWT security, static page hosting, MySQL persistence, and Tesla Fleet API command endpoints.

## What This App Does

- Hosts static HTML from `src/main/resources/static` on `http://localhost:8080/`.
- Exposes a Tesla public key file for domain verification.
- Connects to a local MySQL database from either the IDE or Docker.
- Stores Tesla token, client, and vehicle data in MySQL.
- Calls Tesla Fleet API directly for vehicle lookup and wake-up.
- Calls the configured Tesla proxy for vehicle commands.

## Tech Stack

- Java 11
- Spring Boot 2.6.6
- Spring Security
- Spring Data JPA
- MySQL
- Maven
- Docker

## Run Locally From IDE Or Maven

Start MySQL on your machine first. The default database config is:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=test
DB_USERNAME=root
DB_PASSWORD=root
```

Build and run:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

Open:

```text
http://localhost:8080/
```

## Run With Docker

When the app runs in Docker, `localhost` points to the container, not your laptop. The Docker image uses `host.docker.internal` by default so the container can reach MySQL running on your host machine.

Build:

```bash
docker build -t employee-portal .
```

Run:

```bash
docker run -d --name employee-portal -p 8080:8080 employee-portal:latest
```

Run with explicit database settings:

```bash
docker run -d --name employee-portal -p 8080:8080 ^
  -e DB_HOST=host.docker.internal ^
  -e DB_PORT=3306 ^
  -e DB_NAME=test ^
  -e DB_USERNAME=root ^
  -e DB_PASSWORD=root ^
  -e TESLA_PROXY_BASE=https://host.docker.internal:4443 ^
  employee-portal:latest
```

On Linux, add the host gateway mapping:

```bash
docker run -d --name employee-portal ^
  --add-host=host.docker.internal:host-gateway ^
  -p 8080:8080 employee-portal:latest
```

## Configuration

Application settings are in `src/main/resources/application.properties`. Runtime values can be overridden with environment variables.

| Property | Environment Variable | Default |
| --- | --- | --- |
| `server.port` | `SERVER_PORT` if added manually | `8080` |
| `spring.datasource.url` | `DB_HOST`, `DB_PORT`, `DB_NAME` | `jdbc:mysql://localhost:3306/test` |
| `spring.datasource.username` | `DB_USERNAME` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | `root` |
| `tesla.fleet-api-base` | `TESLA_FLEET_API_BASE` | `https://fleet-api.prd.na.vn.cloud.tesla.com` |
| `tesla.proxy-base` | `TESLA_PROXY_BASE` | `https://127.0.0.1:4443` |
| `tesla.auth-token-url` | `TESLA_AUTH_TOKEN_URL` | `https://auth.tesla.com/oauth2/v3/token` |
| `tesla.vehicle-id-or-vin` | `TESLA_VEHICLE_ID_OR_VIN` | empty |
| `tesla.default-token-id` | `TESLA_DEFAULT_TOKEN_ID` | `1` |
| `tesla.token-refresh-skew-minutes` | `TESLA_TOKEN_REFRESH_SKEW_MINUTES` | `2` |

The Tesla URLs are not hardcoded in service code. `TeslaService` reads them through `TeslaProperties`.

## Important URLs

| URL | Description |
| --- | --- |
| `GET /` | Static home page from `static/index.html` |
| `GET /api/hello` | Simple application health-style response |
| `GET /.well-known/appspecific/com.tesla.3p.public-key.pem` | Tesla public key file |
| `GET /api/.well-known/appspecific/com.tesla.3p.public-key.pem` | API-prefixed Tesla public key file |
| `GET /api/tesla/vehicles` | Fetches vehicles from Tesla Fleet API |
| `POST /api/tesla/wake` | Wakes the configured vehicle |
| `POST /api/tesla/flash-lights` | Flashes vehicle lights |
| `POST /api/tesla/honk` | Honks horn |
| `POST /api/tesla/lock` | Locks doors |
| `POST /api/tesla/unlock` | Unlocks doors |
| `POST /api/tesla/climate/start` | Starts climate |
| `POST /api/tesla/climate/stop` | Stops climate |
| `POST /api/tesla/trunk/open` | Opens rear trunk |
| `POST /api/tesla/frunk/open` | Opens front trunk |
| `POST /api/tesla/start/charging` | Starts charging |
| `POST /api/tesla/stop/charging` | Stops charging |
| `POST /api/tesla/cmd?command=<tesla_command>` | Sends a custom command |

## Cloudflare Tunnel Notes

This project uses Cloudflare Tunnel for the public domain, not ngrok.

Create or route the DNS name to your Cloudflare tunnel:

```bash
cloudflared tunnel route dns test vinodbalakumar.com
```

Start the Cloudflare tunnel so the public domain points to the local Spring Boot app on port `8080`:

```bash
cloudflared tunnel run test
```

The public Cloudflare URL should route to:

```text
http://localhost:8080
```

Keep the Tesla HTTP proxy running separately on `127.0.0.1:4443`:

```powershell
.\tesla-http-proxy.exe `
  -key-file config\private-key.pem `
  -cert localhost-cert.pem `
  -tls-key localhost-key.pem `
  -host 127.0.0.1 `
  -port 4443 `
  -verbose
```

Cloudflare should expose the Spring Boot app on port `8080`. Do not point Cloudflare to the Tesla proxy TLS port `4443`; that proxy is only used internally by `TeslaService` when sending signed vehicle commands.

## Common Troubleshooting

If `http://localhost:8080` does not open:

```bash
docker ps --filter "name=employee-portal"
docker logs --tail 100 employee-portal
```

If Docker cannot connect to MySQL:

- Make sure MySQL is running on the host.
- Make sure the `test` database exists.
- Make sure the configured MySQL user can connect over TCP.
- In Docker, use `DB_HOST=host.docker.internal`, not `localhost`.

If Docker cannot connect to the Tesla HTTP proxy:

- In Docker, use `TESLA_PROXY_BASE=https://host.docker.internal:4443`, not `https://127.0.0.1:4443`.
- Confirm the proxy is running on the host with `Get-NetTCPConnection -LocalPort 4443`.
- From inside the container, `127.0.0.1` is the container itself, not your Windows host.

If the Tesla public key fails:

- Confirm the file exists at `src/main/resources/static/.well-known/appspecific/com.tesla.3p.public-key.pem`.
- Rebuild the Docker image after changing static files.
- Test `http://localhost:8080/.well-known/appspecific/com.tesla.3p.public-key.pem`.

If Cloudflare domain requests do not reach the app:

- Confirm the app is running on `http://localhost:8080`.
- Confirm `cloudflared tunnel run test` is still running.
- Confirm the DNS route exists for `vinodbalakumar.com`.
- Confirm Cloudflare routes to port `8080`, not proxy port `4443`.
- Check app logs with `docker logs --tail 100 employee-portal`.

## Build Verification

Use this before deploying:

```bash
mvn clean package -DskipTests
docker build -t employee-portal .
docker run -d --name employee-portal -p 8080:8080 employee-portal:latest
```

Smoke test:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/hello
curl http://localhost:8080/.well-known/appspecific/com.tesla.3p.public-key.pem
```
