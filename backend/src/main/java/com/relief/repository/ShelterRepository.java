package com.relief.repository;

import com.relief.entity.Shelter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, UUID> {

    // Find shelters by status
    List<Shelter> findByStatus(String status);
    Page<Shelter> findByStatus(String status, Pageable pageable);

    // Find available shelters (ACTIVE status and not full)
    @Query("SELECT s FROM Shelter s WHERE s.status = 'ACTIVE' AND s.currentOccupancy < s.capacity")
    List<Shelter> findAvailableShelters();

    @Query("SELECT s FROM Shelter s WHERE s.status = 'ACTIVE' AND s.currentOccupancy < s.capacity")
    Page<Shelter> findAvailableShelters(Pageable pageable);

    // Find emergency shelters
    List<Shelter> findByIsEmergencyShelterTrue();
    Page<Shelter> findByIsEmergencyShelterTrue(Pageable pageable);

    // Find shelters with minimum capacity
    @Query("SELECT s FROM Shelter s WHERE s.capacity >= :minCapacity")
    List<Shelter> findByMinimumCapacity(@Param("minCapacity") Integer minCapacity);

    // Find shelters by name (case insensitive)
    @Query("SELECT s FROM Shelter s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Shelter> findByNameContainingIgnoreCase(@Param("name") String name);

    // Find shelters within a bounding box (geographic search)
    @Query(value = "SELECT * FROM shelters s WHERE s.geom_point IS NOT NULL AND " +
            "ST_Within(s.geom_point, ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326))",
            nativeQuery = true)
    List<Shelter> findWithinBoundingBox(
            @Param("minLng") Double minLng,
            @Param("minLat") Double minLat,
            @Param("maxLng") Double maxLng,
            @Param("maxLat") Double maxLat
    );

    // Find nearby shelters using PostGIS distance function
    @Query(value = "SELECT *, ST_Distance(geom_point, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) as distance " +
            "FROM shelters s WHERE s.geom_point IS NOT NULL " +
            "ORDER BY ST_Distance(geom_point, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Shelter> findNearbyShelters(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("limit") Integer limit
    );

    // Find shelters with specific facilities
    @Query("SELECT s FROM Shelter s JOIN s.facilities f WHERE f IN :facilities")
    List<Shelter> findByFacilitiesIn(@Param("facilities") List<String> facilities);

    // Get shelter statistics
    @Query("SELECT COUNT(s) FROM Shelter s WHERE s.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT SUM(s.capacity) FROM Shelter s WHERE s.status = 'ACTIVE'")
    Long getTotalActiveCapacity();

    @Query("SELECT SUM(s.currentOccupancy) FROM Shelter s WHERE s.status = 'ACTIVE'")
    Long getTotalCurrentOccupancy();

    @Query("SELECT AVG(CAST(s.currentOccupancy AS DOUBLE) / s.capacity * 100) FROM Shelter s WHERE s.status = 'ACTIVE' AND s.capacity > 0")
    Double getAverageOccupancyRate();
}