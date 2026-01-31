package com.deccan.petroconnect.repository;

import com.deccan.petroconnect.entity.PetrolStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetrolStationRepository extends JpaRepository<PetrolStation, Long> {
}