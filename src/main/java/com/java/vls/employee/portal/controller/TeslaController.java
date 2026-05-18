package com.java.vls.employee.portal.controller;

import com.java.vls.employee.portal.dto.request.Vehicle;
import com.java.vls.employee.portal.service.TeslaService;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tesla")
public class TeslaController {

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
        Vehicle json = teslaService.getVehicles();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    /**
     * Sends a wake-up request directly to Tesla Fleet API.
     */
    @PostMapping("/wake")
    public ResponseEntity<String> wakeVehicle() {
        return teslaService.executeCommand(WAKE_UP, null).getStatusCode() == HttpStatus.OK
                ? ResponseEntity.ok("Vehicle is waking up")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to wake vehicle");
    }

    /**
     * Flashes the configured vehicle lights through the Tesla proxy.
     */
    @PostMapping("/flash-lights")
    public ResponseEntity<String> flashLights() {
        return teslaService.executeCommand(FLASH_LIGHTS, null);
    }

    /**
     * Honks the configured vehicle horn through the Tesla proxy.
     */
    @PostMapping("/honk")
    public ResponseEntity<String> honkHorn() {
        return teslaService.executeCommand(HONK_HORN, null);
    }

    /**
     * Locks the configured vehicle doors.
     */
    @PostMapping("/lock")
    public ResponseEntity<String> lock() {
        return teslaService.executeCommand(DOOR_LOCK, null);
    }

    /**
     * Unlocks the configured vehicle doors.
     */
    @PostMapping("/unlock")
    public ResponseEntity<String> unlock() {
        return teslaService.executeCommand(DOOR_UNLOCK, null);
    }

    /**
     * Starts vehicle climate control.
     */
    @PostMapping("/climate/start")
    public ResponseEntity<String> startClimate() {
        return teslaService.executeCommand(CLIMATE_START, null);
    }

    /**
     * Stops vehicle climate control.
     */
    @PostMapping("/climate/stop")
    public ResponseEntity<String> stopClimate() {
        return teslaService.executeCommand(CLIMATE_STOP, null);
    }

    /**
     * Opens the rear trunk.
     */
    @PostMapping("/trunk/open")
    public ResponseEntity<String> openTrunk() {
        return teslaService.executeCommand(ACTUATE_TRUNK, Map.of("which_trunk", "rear"));
    }

    /**
     * Opens the front trunk.
     */
    @PostMapping("/frunk/open")
    public ResponseEntity<String> openFrunk() {
        return teslaService.executeCommand(ACTUATE_TRUNK, Map.of("which_trunk", "front"));
    }

    /**
     * Sets the driver-side cabin temperature.
     */
    @PostMapping("/set/driverTemperature")
    public ResponseEntity<String> setTemprature(@RequestParam Long driverTemp) {
        return teslaService.executeCommand(SET_TEMPS, Map.of("driver_temp", driverTemp));
    }

    /**
     * Sets the passenger-side cabin temperature.
     */
    @PostMapping("/set/passengerTemperature")
    public ResponseEntity<String> passengerTemperature(@RequestParam Long passengerTemperature) {
        return teslaService.executeCommand(SET_TEMPS, Map.of("passenger_temp", passengerTemperature));
    }

    /**
     * Starts vehicle charging.
     */
    @PostMapping("/start/charging")
    public ResponseEntity<String> startCharging() {
        return teslaService.executeCommand(CHARGE_START, null);
    }

    /**
     * Stops vehicle charging.
     */
    @PostMapping("/stop/charging")
    public ResponseEntity<String> stopCharging() {
        return teslaService.executeCommand(CHARGE_STOP, null);
    }

    /**
     * Sends a custom Tesla command by name for manual testing.
     */
    @PostMapping("/cmd")
    public ResponseEntity<String> cmd(@RequestParam(required = false)  String command) {
        return teslaService.executeCommand( command, null);
    }
}
