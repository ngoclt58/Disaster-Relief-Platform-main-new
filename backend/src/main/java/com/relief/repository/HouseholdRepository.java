package com.relief.repository;

import com.relief.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, UUID> {
    Optional<Household> findByResidentUser_Id(UUID residentUserId);
}





