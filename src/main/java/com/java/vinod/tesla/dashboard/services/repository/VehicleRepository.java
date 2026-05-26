package com.java.vinod.tesla.dashboard.services.repository;

import com.java.vinod.tesla.dashboard.services.entity.TeslaVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<TeslaVehicle, Long> {

    // Find by VIN
    Optional<TeslaVehicle> findByVin(String vin);

    // Find by display name
    List<TeslaVehicle> findByDisplayName(String displayName);

    // Find by state (online / offline)
    List<TeslaVehicle> findByState(String state);

    // Find by vehicle_id
    Optional<TeslaVehicle> findByVehicleId(Long vehicleId);

    // Find all vehicles currently in service
    List<TeslaVehicle> findByInServiceTrue();

    // Find by access type
    List<TeslaVehicle> findByAccessType(String accessType);

    // Check if VIN already exists
    boolean existsByVin(String vin);

    // Custom query — search by state and access type
    @Query("SELECT v FROM TeslaVehicle v WHERE v.state = :state AND v.accessType = :accessType")
    List<TeslaVehicle> findByStateAndAccessType(@Param("state") String state,
                                           @Param("accessType") String accessType);

    // Native query — find latest updated vehicles
    @Query(value = "SELECT * FROM tesla_vehicle ORDER BY updated_at DESC LIMIT :limit",
           nativeQuery = true)
    List<TeslaVehicle> findLatestUpdated(@Param("limit") int limit);
}
