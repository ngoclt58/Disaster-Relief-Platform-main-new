package com.relief.repository;

import com.relief.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phone = :phone")
    Optional<User> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.disabled = false")
    java.util.List<User> findByRoleAndNotDisabled(@Param("role") String role);
    
    // Admin-specific queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.disabled = false")
    long countActiveUsers();
    
    @Query("SELECT u.role as role, COUNT(u) as count FROM User u GROUP BY u.role")
    Map<String, Long> countByRole();
    
    // Analytics queries
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.role = :role")
    Page<User> findBySearchTermAndRole(@Param("search") String search, @Param("role") String role, Pageable pageable);
    
    Page<User> findByRole(String role, Pageable pageable);
    
    // Smart automation queries
    @Query("""
           SELECT u FROM User u
           WHERE u.geomPoint IS NOT NULL
             AND function('ST_DWithin', u.geomPoint, :point, :radius) = true
           """)
    List<User> findUsersInRadius(@Param("point") org.locationtech.jts.geom.Point point, @Param("radius") double radius);
}
