package com.java.vls.employee.portal.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {

    private long id;
    @JsonProperty("vehicle_id")
    private long vehicleId;
    @JsonProperty("vin")
    private String vin;

    @JsonProperty("color")
    private String color;

    @JsonProperty("access_type")
    private String accessType;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("state")
    private String state;
    @JsonProperty("in_service")
    private boolean inService;

    public Vehicle() {}

    public Vehicle(long id, long vehicleId, String vin, String color, String accessType, String displayName) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.color = color;
        this.accessType = accessType;
        this.displayName = displayName;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isInService() { return inService; }
    public void setInService(boolean inService) { this.inService = inService; }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", vin='" + vin + '\'' +
                ", color='" + color + '\'' +
                ", accessType='" + accessType + '\'' +
                ", displayName='" + displayName + '\'' +
                ", state='" + state + '\'' +
                ", inService=" + inService +
                '}';
    }
}
