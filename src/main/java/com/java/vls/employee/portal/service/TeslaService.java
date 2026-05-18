package com.java.vls.employee.portal.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.java.vls.employee.portal.configuration.TeslaProperties;
import com.java.vls.employee.portal.dto.request.TeslaTokenResponse;
import com.java.vls.employee.portal.dto.request.Vehicle;
import com.java.vls.employee.portal.dto.request.VehicleApiResponse;
import com.java.vls.employee.portal.entity.TeslaTokens;
import com.java.vls.employee.portal.entity.TeslaVehicle;
import com.java.vls.employee.portal.repository.TeslaTokenRepository;
import com.java.vls.employee.portal.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class TeslaService {

    private final RestTemplate restTemplate;
    private final TeslaProperties teslaProperties;
    private final VehicleRepository vehicleRepository;
    private final TeslaTokenRepository tokenRepository;

    public TeslaService(
            RestTemplate restTemplate,
            TeslaProperties teslaProperties,
            VehicleRepository vehicleRepository,
            TeslaTokenRepository tokenRepository) {
        this.restTemplate = restTemplate;
        this.teslaProperties = teslaProperties;
        this.vehicleRepository = vehicleRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Fetches the first vehicle from Tesla Fleet API and stores a local copy for later command calls.
     *
     * @return first vehicle returned by Tesla, or {@code null} when the account has no vehicles
     */
    public Vehicle getVehicles() {
        String url = buildFleetUrl("/api/1/vehicles");
        log.info("Fetching Tesla vehicles: url={}", url);

        ResponseEntity<VehicleApiResponse> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers()),
                    VehicleApiResponse.class
            );
        } catch (RestClientException ex) {
            log.error("Tesla vehicle fetch failed: url={}, reason={}", url, ex.getMessage(), ex);
            throw ex;
        }

        List<Vehicle> vehicles = Optional.ofNullable(response.getBody())
                .map(VehicleApiResponse::getResponse)
                .orElse(Collections.emptyList());
        if (vehicles.isEmpty()) {
            log.warn("Tesla vehicle fetch returned no vehicles: status={}", response.getStatusCodeValue());
            return null;
        }

        Vehicle vehicle = vehicles.get(0);
        log.info("Fetched Tesla vehicle: displayName={}, vinSuffix={}, status={}",
                vehicle.getDisplayName(), suffix(vehicle.getVin()), response.getStatusCodeValue());
        saveOrUpdateVehicle(vehicle);
        return vehicle;
    }

    /**
     * Executes a Tesla command against the configured vehicle.
     *
     * <p>The wake command goes directly to Fleet API. Other commands go through the configured local
     * Tesla proxy because vehicle command signing is handled there.</p>
     *
     * @param command Tesla command name, for example {@code flash_lights} or {@code door_lock}
     * @param body optional request body for commands that need parameters
     * @return raw Tesla/proxy response
     */
    public ResponseEntity<String> executeCommand(String command, Map<String, Object> body) {
        String vehicleIdOrVin = resolveVehicleIdOrVin();
        String url = buildCommandUrl(command, vehicleIdOrVin);
        Map<String, Object> requestBody = body == null ? Collections.emptyMap() : body;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers());

        log.info("Executing Tesla command: command={}, targetSuffix={}, url={}, bodyKeys={}",
                command, suffix(vehicleIdOrVin), url, requestBody.keySet());
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Tesla command completed: command={}, targetSuffix={}, status={}",
                    command, suffix(vehicleIdOrVin), response.getStatusCodeValue());
            return response;
        } catch (RestClientException ex) {
            log.error("Tesla command failed: command={}, targetSuffix={}, url={}, reason={}",
                    command, suffix(vehicleIdOrVin), url, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Builds JSON headers and attaches the current valid Tesla bearer token.
     */
    private HttpHeaders headers() {
        TeslaTokens token = getValidAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token.getAccessToken() != null && !token.getAccessToken().isBlank()) {
            headers.setBearerAuth(token.getAccessToken());
        }
        return headers;
    }

    /**
     * Returns the stored Tesla token, refreshing it when it is close to expiry.
     */
    public TeslaTokens getValidAccessToken() {
        TeslaTokens token = tokenRepository.findById(teslaProperties.getDefaultTokenId())
                .orElseThrow(() -> {
                    log.error("Tesla token not found: configuredTokenId={}", teslaProperties.getDefaultTokenId());
                    return new IllegalStateException("Tesla token not found for configured token id");
                });

        // Refresh slightly before expiry so command calls do not race the token timeout.
        if (token.getExpiresAt() != null
                && token.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(teslaProperties.getTokenRefreshSkewMinutes()))) {
            log.debug("Using cached Tesla token: tokenId={}, expiresAt={}", token.getId(), token.getExpiresAt());
            return token;
        }

        log.info("Tesla token refresh required: tokenId={}, expiresAt={}, refreshSkewMinutes={}",
                token.getId(), token.getExpiresAt(), teslaProperties.getTokenRefreshSkewMinutes());
        return refreshAccessToken(token);
    }

    /**
     * Refreshes an expired Tesla access token using the stored refresh token and persists the result.
     */
    public TeslaTokens refreshAccessToken(TeslaTokens token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "refresh_token");
        requestBody.put("client_id", token.getClient().getClientId());
        requestBody.put("refresh_token", token.getRefreshToken());

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(requestBody, headers);

        log.info("Refreshing Tesla access token: tokenId={}, authTokenUrl={}",
                token.getId(), teslaProperties.getAuthTokenUrl());
        ResponseEntity<TeslaTokenResponse> response;
        try {
            response = restTemplate.exchange(
                    teslaProperties.getAuthTokenUrl(),
                    HttpMethod.POST,
                    request,
                    TeslaTokenResponse.class
            );
        } catch (RestClientException ex) {
            log.error("Tesla token refresh failed: tokenId={}, authTokenUrl={}, reason={}",
                    token.getId(), teslaProperties.getAuthTokenUrl(), ex.getMessage(), ex);
            throw ex;
        }

        TeslaTokenResponse body = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> {
                    log.error("Tesla token refresh returned empty response: tokenId={}, status={}",
                            token.getId(), response.getStatusCodeValue());
                    return new IllegalStateException("Tesla token refresh returned an empty response");
                });

        token.setAccessToken(body.getAccessToken());
        token.setRefreshToken(body.getRefreshToken());
        token.setTokenType(body.getTokenType());
        token.setScope(body.getScope());

        token.setExpiresAt(
                LocalDateTime.now()
                        .plusSeconds(body.getExpiresIn())
        );
        TeslaTokens savedToken = tokenRepository.save(token);
        log.info("Tesla token refresh completed: tokenId={}, status={}, expiresAt={}",
                savedToken.getId(), response.getStatusCodeValue(), savedToken.getExpiresAt());
        return savedToken;
    }

    /**
     * Upserts Tesla vehicle details by VIN so repeat Fleet API calls keep the local table current.
     */
    private void saveOrUpdateVehicle(Vehicle vehicle) {
        Optional<TeslaVehicle> existing = vehicleRepository.findByVin(vehicle.getVin());
        TeslaVehicle entity = existing.orElseGet(TeslaVehicle::new);

        entity.setVehicleId(vehicle.getVehicleId());
        entity.setVin(vehicle.getVin());
        entity.setDisplayName(vehicle.getDisplayName());
        entity.setColor(vehicle.getColor());
        entity.setAccessType(vehicle.getAccessType());

        TeslaVehicle savedVehicle = vehicleRepository.save(entity);
        log.info("Saved Tesla vehicle locally: id={}, displayName={}, vinSuffix={}",
                savedVehicle.getId(), savedVehicle.getDisplayName(), suffix(savedVehicle.getVin()));
    }

    /**
     * Chooses the vehicle target for commands from config first, then falls back to the token's client vehicle.
     */
    private String resolveVehicleIdOrVin() {
        if (teslaProperties.getVehicleIdOrVin() != null && !teslaProperties.getVehicleIdOrVin().isBlank()) {
            log.debug("Using configured Tesla vehicle target: targetSuffix={}", suffix(teslaProperties.getVehicleIdOrVin()));
            return teslaProperties.getVehicleIdOrVin();
        }

        TeslaTokens token = getValidAccessToken();
        if (token.getClient() == null || token.getClient().getVehicles() == null) {
            log.error("No Tesla vehicle available for active client: tokenId={}", token.getId());
            throw new IllegalStateException("No Tesla vehicle configured for the active client");
        }
        log.debug("Using Tesla vehicle from active client: tokenId={}, targetSuffix={}",
                token.getId(), suffix(token.getClient().getVehicles().getVin()));
        return token.getClient().getVehicles().getVin();
    }

    /**
     * Routes wake-up to Fleet API and all other signed commands to the configured Tesla proxy.
     */
    private String buildCommandUrl(String command, String vehicleIdOrVin) {
        if ("wake_up".equals(command)) {
            return buildFleetUrl("/api/1/vehicles/" + vehicleIdOrVin + "/wake_up");
        }
        return buildProxyUrl("/api/1/vehicles/" + vehicleIdOrVin + "/command/" + command);
    }

    /**
     * Builds a Fleet API URL from configuration so the base URL can be changed without code edits.
     */
    private String buildFleetUrl(String path) {
        return UriComponentsBuilder.fromHttpUrl(teslaProperties.getFleetApiBase())
                .path(path)
                .toUriString();
    }

    /**
     * Builds a local proxy URL from configuration so Docker/Cloudflare environments can override it.
     */
    private String buildProxyUrl(String path) {
        return UriComponentsBuilder.fromHttpUrl(teslaProperties.getProxyBase())
                .path(path)
                .toUriString();
    }

    private String suffix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int start = Math.max(0, value.length() - 4);
        return value.substring(start);
    }
}
