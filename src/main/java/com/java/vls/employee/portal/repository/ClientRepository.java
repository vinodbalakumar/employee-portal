package com.java.vls.employee.portal.repository;

import com.java.vls.employee.portal.entity.Client;
import com.java.vls.employee.portal.entity.TeslaTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

}
