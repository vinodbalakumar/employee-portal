package com.java.vls.employee.portal.entity;



import javax.persistence.*;

@Entity
@Table(name = "tesla_vehicle")
public class TeslaVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vin", unique = true, nullable = false, length = 17)
    private String vin;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "state")
    private String state;

    @Column(name = "color")
    private String color;

    @Column(name = "access_type")
    private String accessType;

    @Column(name = "in_service")
    private boolean inService;

    @Column(name = "calendar_enabled")
    private boolean calendarEnabled;

    @Column(name = "api_version")
    private Integer apiVersion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private TeslaClient client;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }

    // Constructors
    public TeslaVehicle() {}

    public TeslaVehicle(Long vehicleId, String vin, String displayName, String state) {
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.displayName = displayName;
        this.state = state;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public boolean isInService() { return inService; }
    public void setInService(boolean inService) { this.inService = inService; }

    public boolean isCalendarEnabled() { return calendarEnabled; }
    public void setCalendarEnabled(boolean calendarEnabled) { this.calendarEnabled = calendarEnabled; }

    public Integer getApiVersion() { return apiVersion; }
    public void setApiVersion(Integer apiVersion) { this.apiVersion = apiVersion; }

    public TeslaClient getClient() { return client; }
    public void setClient(TeslaClient client) { this.client = client; }

    public java.util.Date getCreatedAt() { return createdAt; }
    public java.util.Date getUpdatedAt() { return updatedAt; }
}
