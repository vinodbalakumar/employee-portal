package com.java.vinod.tesla.dashboard.services.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "tesla_client")
public class TeslaClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    @OneToOne(mappedBy = "client",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private TeslaTokens teslaToken;

    @OneToOne(mappedBy = "client",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private TeslaVehicle vehicles;

    public TeslaClient() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TeslaTokens getTeslaToken() {
        return teslaToken;
    }

    public void setTeslaToken(TeslaTokens teslaToken) {
        this.teslaToken = teslaToken;
    }

    public TeslaVehicle getVehicles() {
        return vehicles;
    }

    public void setVehicles(TeslaVehicle vehicles) {
        this.vehicles = vehicles;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
