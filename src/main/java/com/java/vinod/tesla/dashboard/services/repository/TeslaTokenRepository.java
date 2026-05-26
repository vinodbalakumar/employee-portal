package com.java.vinod.tesla.dashboard.services.repository;

import com.java.vinod.tesla.dashboard.services.entity.TeslaTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeslaTokenRepository extends JpaRepository<TeslaTokens, Long> {

}
