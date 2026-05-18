package com.java.vls.employee.portal.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class TeslaService {

    private static final int VEHICLE_DATA_WAKE_RETRY_COUNT = 3;
    private static final long VEHICLE_DATA_WAKE_RETRY_DELAY_MS = 10000L;

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
        String vehicleIdOrVin = resolveVehicleIdOrVin(!"wake_up".equals(command));
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
     * Reads whether the configured vehicle is locked or unlocked from Tesla vehicle data.
     */
    public Map<String, Object> getLockStatus() {
        JsonNode vehicleData;
        try {
            vehicleData = fetchVehicleData();
        } catch (HttpClientErrorException ex) {
            if (isVehicleUnavailable(ex)) {
                return unavailableStatus("lock", ex);
            }
            throw ex;
        }
        JsonNode vehicleState = vehicleData.path("vehicle_state");
        JsonNode lockedNode = vehicleState.path("locked");
        Boolean locked = lockedNode.isMissingNode() || lockedNode.isNull() ? null : lockedNode.asBoolean();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("available", true);
        status.put("locked", locked);
        status.put("lockState", locked == null ? "unknown" : locked ? "locked" : "unlocked");
        status.put("vehicleState", vehicleData.path("state").asText(""));
        status.put("vinSuffix", suffix(vehicleData.path("vin").asText("")));

        log.info("Tesla lock status read: lockState={}, vehicleState={}, vinSuffix={}",
                status.get("lockState"), status.get("vehicleState"), status.get("vinSuffix"));
        return status;
    }

    /**
     * Reads whether the configured vehicle is charging from Tesla vehicle data.
     */
    public Map<String, Object> getChargingStatus() {
        JsonNode vehicleData;
        try {
            vehicleData = fetchVehicleData();
        } catch (HttpClientErrorException ex) {
            if (isVehicleUnavailable(ex)) {
                return unavailableStatus("charging", ex);
            }
            throw ex;
        }
        JsonNode chargeState = vehicleData.path("charge_state");
        String chargingState = chargeState.path("charging_state").asText("unknown");

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("available", true);
        status.put("charging", "Charging".equalsIgnoreCase(chargingState));
        status.put("chargingState", chargingState);
        status.put("batteryLevel", nullableInt(chargeState.path("battery_level")));
        status.put("chargeLimitSoc", nullableInt(chargeState.path("charge_limit_soc")));
        status.put("timeToFullCharge", nullableDouble(chargeState.path("time_to_full_charge")));
        status.put("vehicleState", vehicleData.path("state").asText(""));
        status.put("vinSuffix", suffix(vehicleData.path("vin").asText("")));

        log.info("Tesla charging status read: chargingState={}, batteryLevel={}, vehicleState={}, vinSuffix={}",
                status.get("chargingState"), status.get("batteryLevel"), status.get("vehicleState"), status.get("vinSuffix"));
        return status;
    }

    /**
     * Reads a combined snapshot of lock and charging status with one Tesla vehicle data call.
     */
    public Map<String, Object> getVehicleStatus() {
        JsonNode vehicleData;
        try {
            vehicleData = fetchVehicleData();
        } catch (HttpClientErrorException ex) {
            if (isVehicleUnavailable(ex)) {
                return unavailableStatus("combined", ex);
            }
            throw ex;
        }
        JsonNode vehicleState = vehicleData.path("vehicle_state");
        JsonNode chargeState = vehicleData.path("charge_state");
        JsonNode lockedNode = vehicleState.path("locked");
        Boolean locked = lockedNode.isMissingNode() || lockedNode.isNull() ? null : lockedNode.asBoolean();
        String chargingState = chargeState.path("charging_state").asText("unknown");

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("available", true);
        status.put("locked", locked);
        status.put("lockState", locked == null ? "unknown" : locked ? "locked" : "unlocked");
        status.put("charging", "Charging".equalsIgnoreCase(chargingState));
        status.put("chargingState", chargingState);
        status.put("batteryLevel", nullableInt(chargeState.path("battery_level")));
        status.put("vehicleState", vehicleData.path("state").asText(""));
        status.put("displayName", vehicleData.path("display_name").asText(""));
        status.put("vinSuffix", suffix(vehicleData.path("vin").asText("")));

        log.info("Tesla combined status read: lockState={}, chargingState={}, batteryLevel={}, vehicleState={}, vinSuffix={}",
                status.get("lockState"), status.get("chargingState"), status.get("batteryLevel"),
                status.get("vehicleState"), status.get("vinSuffix"));
        return status;
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
        entity.setState(vehicle.getState());
        entity.setInService(vehicle.isInService());

        TeslaVehicle savedVehicle = vehicleRepository.save(entity);
        log.info("Saved Tesla vehicle locally: id={}, displayName={}, vinSuffix={}, state={}, inService={}",
                savedVehicle.getId(), savedVehicle.getDisplayName(), suffix(savedVehicle.getVin()),
                savedVehicle.getState(), savedVehicle.isInService());
    }

    /**
     * Chooses the vehicle target for commands from config first, then falls back to the token's client vehicle.
     */
    private String resolveVehicleIdOrVin() {
        return resolveVehicleIdOrVin(false);
    }

    private String resolveVehicleIdOrVin(boolean wakeBeforeCommand) {
        if (teslaProperties.getVehicleIdOrVin() != null && !teslaProperties.getVehicleIdOrVin().isBlank()) {
            checkVehicleWakeup(teslaProperties.getVehicleIdOrVin(), wakeBeforeCommand);
            log.debug("Using configured Tesla vehicle target: targetSuffix={}",
                    suffix(teslaProperties.getVehicleIdOrVin()));
            return teslaProperties.getVehicleIdOrVin();
        }

        TeslaTokens token = getValidAccessToken();
        if (token.getClient() == null || token.getClient().getVehicles() == null) {
            log.error("No Tesla vehicle available for active client: tokenId={}", token.getId());
            throw new IllegalStateException("No Tesla vehicle configured for the active client");
        }
        checkVehicleWakeup(token.getClient().getVehicles().getVin(), wakeBeforeCommand);
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

    private JsonNode fetchVehicleData() {
        String vehicleIdOrVin = resolveVehicleIdOrVin(false);
        String url = buildFleetUrl("/api/1/vehicles/" + vehicleIdOrVin + "/vehicle_data");
        log.info("Fetching Tesla vehicle data: targetSuffix={}, url={}", suffix(vehicleIdOrVin), url);

        try {
            return requestVehicleData(url, vehicleIdOrVin);
        } catch (HttpClientErrorException ex) {
            if (isVehicleUnavailable(ex)) {
                log.warn("Tesla vehicle data unavailable because vehicle is asleep/offline; waking and retrying: targetSuffix={}, retries={}, delayMs={}",
                        suffix(vehicleIdOrVin), VEHICLE_DATA_WAKE_RETRY_COUNT, VEHICLE_DATA_WAKE_RETRY_DELAY_MS);
                wakeVehicle(vehicleIdOrVin);
                HttpClientErrorException lastUnavailable = ex;
                for (int attempt = 1; attempt <= VEHICLE_DATA_WAKE_RETRY_COUNT; attempt++) {
                    waitForWakeup(vehicleIdOrVin, attempt);
                    try {
                        return requestVehicleData(url, vehicleIdOrVin);
                    } catch (HttpClientErrorException retryEx) {
                        if (!isVehicleUnavailable(retryEx)) {
                            throw retryEx;
                        }
                        lastUnavailable = retryEx;
                        log.warn("Tesla vehicle still asleep/offline after wake retry: targetSuffix={}, attempt={}/{}",
                                suffix(vehicleIdOrVin), attempt, VEHICLE_DATA_WAKE_RETRY_COUNT);
                    }
                }
                throw lastUnavailable;
            }
            log.error("Tesla vehicle data fetch failed: targetSuffix={}, url={}, status={}, reason={}",
                    suffix(vehicleIdOrVin), url, ex.getRawStatusCode(), ex.getMessage(), ex);
            throw ex;
        } catch (RestClientException ex) {
            log.error("Tesla vehicle data fetch failed: targetSuffix={}, url={}, reason={}",
                    suffix(vehicleIdOrVin), url, ex.getMessage(), ex);
            throw ex;
        }
    }

    private JsonNode requestVehicleData(String url, String vehicleIdOrVin) {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                JsonNode.class
        );
        JsonNode body = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new IllegalStateException("Tesla vehicle data returned an empty response"));
        JsonNode vehicleData = body.path("response");
        if (vehicleData.isMissingNode() || vehicleData.isNull()) {
            log.error("Tesla vehicle data response missing response node: status={}, targetSuffix={}",
                    response.getStatusCodeValue(), suffix(vehicleIdOrVin));
            throw new IllegalStateException("Tesla vehicle data response did not include response node");
        }
        log.info("Tesla vehicle data fetched: targetSuffix={}, status={}",
                suffix(vehicleIdOrVin), response.getStatusCodeValue());
        return vehicleData;
    }

    private void wakeVehicle(String vehicleIdOrVin) {
        String wakeUrl = buildFleetUrl("/api/1/vehicles/" + vehicleIdOrVin + "/wake_up");
        log.info("Sending Tesla wake-up before status read: targetSuffix={}, url={}", suffix(vehicleIdOrVin), wakeUrl);
        restTemplate.exchange(wakeUrl, HttpMethod.POST, new HttpEntity<>(headers()), String.class);
    }

    private void checkVehicleWakeup(String vehicleIdOrVin, boolean wakeBeforeCommand) {
        if (!wakeBeforeCommand || vehicleIdOrVin == null || vehicleIdOrVin.isBlank()) {
            return;
        }

        findVehicleByTarget(vehicleIdOrVin)
                .filter(vehicle -> isSleepingOrOffline(vehicle.getState()))
                .ifPresent(vehicle -> {
                    log.info("Vehicle state requires wake-up before command: targetSuffix={}, state={}, inService={}",
                            suffix(vehicle.getVin()), vehicle.getState(), vehicle.isInService());
                    wakeVehicle(vehicle.getVin());
                    waitForWakeup(vehicle.getVin(), 1);
                });
    }

    private Optional<TeslaVehicle> findVehicleByTarget(String vehicleIdOrVin) {
        Optional<TeslaVehicle> vehicleByVin = vehicleRepository.findByVin(vehicleIdOrVin);
        if (vehicleByVin.isPresent()) {
            return vehicleByVin;
        }

        try {
            return vehicleRepository.findByVehicleId(Long.valueOf(vehicleIdOrVin));
        } catch (NumberFormatException ex) {
            log.debug("Tesla vehicle target is not numeric, skipping vehicle_id lookup: targetSuffix={}",
                    suffix(vehicleIdOrVin));
            return Optional.empty();
        }
    }

    private boolean isSleepingOrOffline(String state) {
        return "asleep".equalsIgnoreCase(state) || "offline".equalsIgnoreCase(state);
    }

    private void waitForWakeup(String vehicleIdOrVin, int attempt) {
        try {
            log.info("Waiting before Tesla vehicle data wake retry: targetSuffix={}, attempt={}, delayMs={}",
                    suffix(vehicleIdOrVin), attempt, VEHICLE_DATA_WAKE_RETRY_DELAY_MS);
            Thread.sleep(VEHICLE_DATA_WAKE_RETRY_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for Tesla wake-up retry: targetSuffix={}", suffix(vehicleIdOrVin));
        }
    }

    private boolean isVehicleUnavailable(HttpClientErrorException ex) {
        return ex.getRawStatusCode() == 408
                && ex.getResponseBodyAsString().contains("offline or asleep");
    }

    private Map<String, Object> unavailableStatus(String statusType, HttpClientErrorException ex) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("available", false);
        status.put("vehicleState", "asleep_or_offline");
        status.put("message", "Vehicle is asleep or offline. Wake-up was sent; retry in a minute.");
        status.put("teslaStatus", ex.getRawStatusCode());

        if ("lock".equals(statusType) || "combined".equals(statusType)) {
            status.put("locked", null);
            status.put("lockState", "unknown");
        }
        if ("charging".equals(statusType) || "combined".equals(statusType)) {
            status.put("charging", null);
            status.put("chargingState", "unknown");
            status.put("batteryLevel", null);
        }

        log.warn("Tesla status unavailable after wake retries: statusType={}, teslaStatus={}, response={}",
                statusType, ex.getRawStatusCode(), ex.getResponseBodyAsString());
        return status;
    }

    private String suffix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int start = Math.max(0, value.length() - 4);
        return value.substring(start);
    }

    private Integer nullableInt(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private Double nullableDouble(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asDouble();
    }
}
