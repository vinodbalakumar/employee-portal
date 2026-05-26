package com.java.vinod.tesla.dashboard.services.controller;

import com.java.vinod.tesla.dashboard.services.dto.request.Vehicle;
import com.java.vinod.tesla.dashboard.services.service.TeslaService;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class TeslaController {

    private static final Logger log = LoggerFactory.getLogger(TeslaController.class);
    private static final String WAKE_UP = "wake_up";
    private static final String FLASH_LIGHTS = "flash_lights";
    private static final String HONK_HORN = "honk_horn";
    private static final String DOOR_LOCK = "door_lock";
    private static final String DOOR_UNLOCK = "door_unlock";
    private static final String CLIMATE_START = "auto_conditioning_start";
    private static final String CLIMATE_STOP = "auto_conditioning_stop";
    private static final String ACTUATE_TRUNK = "actuate_trunk";
    private static final String SET_TEMPS = "set_temps";
    private static final String CHARGE_START = "charge_start";
    private static final String CHARGE_STOP = "charge_stop";

    private final TeslaService teslaService;

    public TeslaController(TeslaService teslaService) {
        this.teslaService = teslaService;
    }

    /**
     * Fetches vehicles from Tesla and saves the first vehicle locally.
     */
    @GetMapping("/vehicles")
    public ResponseEntity<Vehicle> getVehicles() {
        log.info("Tesla vehicles endpoint requested");
        Vehicle json = teslaService.getVehicles();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    /**
     * Reads whether the configured vehicle is locked or unlocked.
     */
    @GetMapping("/status/lock")
    public ResponseEntity<Map<String, Object>> getLockStatus() {
        log.info("Tesla lock status endpoint requested");
        return ResponseEntity.ok(teslaService.getLockStatus());
    }

    /**
     * Reads whether the configured vehicle is charging.
     */
    @GetMapping("/status/charging")
    public ResponseEntity<Map<String, Object>> getChargingStatus() {
        log.info("Tesla charging status endpoint requested");
        return ResponseEntity.ok(teslaService.getChargingStatus());
    }

    /**
     * Reads a combined snapshot of lock and charging state.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVehicleStatus() {
        log.info("Tesla combined status endpoint requested");
        return ResponseEntity.ok(teslaService.getVehicleStatus());
    }

    /**
     * Sends a wake-up request directly to Tesla Fleet API.
     */
    @PostMapping("/wake")
    public ResponseEntity<String> wakeVehicle() {
        log.info("Tesla wake endpoint requested");
        ResponseEntity<String> response = teslaService.executeCommand(WAKE_UP, null);
        return response.getStatusCode() == HttpStatus.OK
                ? ResponseEntity.ok("Vehicle is waking up")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to wake vehicle");
    }

    /**
     * Flashes the configured vehicle lights through the Tesla proxy.
     */
    @PostMapping("/flash-lights")
    public ResponseEntity<String> flashLights() {
        log.info("Tesla flash lights endpoint requested");
        return teslaService.executeCommand(FLASH_LIGHTS, null);
    }

    /**
     * Honks the configured vehicle horn through the Tesla proxy.
     */
    @PostMapping("/honk")
    public ResponseEntity<String> honkHorn() {
        log.info("Tesla honk endpoint requested");
        return teslaService.executeCommand(HONK_HORN, null);
    }

    /**
     * Locks the configured vehicle doors.
     */
    @PostMapping("/lock")
    public ResponseEntity<String> lock() {
        log.info("Tesla lock endpoint requested");
        return teslaService.executeCommand(DOOR_LOCK, null);
    }

    /**
     * Unlocks the configured vehicle doors.
     */
    @PostMapping("/unlock")
    public ResponseEntity<String> unlock() {
        log.info("Tesla unlock endpoint requested");
        return teslaService.executeCommand(DOOR_UNLOCK, null);
    }

    /**
     * Starts vehicle climate control.
     */
    @PostMapping("/climate/start")
    public ResponseEntity<String> startClimate() {
        log.info("Tesla climate start endpoint requested");
        return teslaService.executeCommand(CLIMATE_START, null);
    }

    /**
     * Stops vehicle climate control.
     */
    @PostMapping("/climate/stop")
    public ResponseEntity<String> stopClimate() {
        log.info("Tesla climate stop endpoint requested");
        return teslaService.executeCommand(CLIMATE_STOP, null);
    }

    /**
     * Opens the rear trunk.
     */
    @PostMapping("/trunk/open")
    public ResponseEntity<String> openTrunk() {
        log.info("Tesla rear trunk endpoint requested");
        return teslaService.executeCommand(ACTUATE_TRUNK, Map.of("which_trunk", "rear"));
    }

    /**
     * Opens the front trunk.
     */
    @PostMapping("/frunk/open")
    public ResponseEntity<String> openFrunk() {
        log.info("Tesla front trunk endpoint requested");
        return teslaService.executeCommand(ACTUATE_TRUNK, Map.of("which_trunk", "front"));
    }

    /**
     * Sets the driver-side cabin temperature.
     */
    @PostMapping("/set/driverTemperature")
    public ResponseEntity<String> setTemprature(@RequestParam Long driverTemp) {
        log.info("Tesla driver temperature endpoint requested: driverTemp={}", driverTemp);
        return teslaService.executeCommand(SET_TEMPS, Map.of("driver_temp", driverTemp));
    }

    /**
     * Sets the passenger-side cabin temperature.
     */
    @PostMapping("/set/passengerTemperature")
    public ResponseEntity<String> passengerTemperature(@RequestParam Long passengerTemperature) {
        log.info("Tesla passenger temperature endpoint requested: passengerTemperature={}", passengerTemperature);
        return teslaService.executeCommand(SET_TEMPS, Map.of("passenger_temp", passengerTemperature));
    }

    /**
     * Starts vehicle charging.
     */
    @PostMapping("/start/charging")
    public ResponseEntity<String> startCharging() {
        log.info("Tesla start charging endpoint requested");
        return teslaService.executeCommand(CHARGE_START, null);
    }

    /**
     * Stops vehicle charging.
     */
    @PostMapping("/stop/charging")
    public ResponseEntity<String> stopCharging() {
        log.info("Tesla stop charging endpoint requested");
        return teslaService.executeCommand(CHARGE_STOP, null);
    }

    /**
     * Sends a custom Tesla command by name for manual testing.
     */
    @PostMapping("/cmd")
    public ResponseEntity<String> cmd(@RequestParam(required = false)  String command) {
        log.info("Tesla custom command endpoint requested: command={}", command);
        return teslaService.executeCommand( command, null);
    }
}
