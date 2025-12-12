package com.relief.repository.heatmap;

import com.relief.domain.heatmap.HeatmapConfiguration;
import com.relief.domain.heatmap.HeatmapType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for heatmap configurations
 */
@Repository
public interface HeatmapConfigurationRepository extends JpaRepository<HeatmapConfiguration, Long> {
    
    /**
     * Find configurations by heatmap type
     */
    List<HeatmapConfiguration> findByHeatmapType(HeatmapType heatmapType);
    
    /**
     * Find active configurations
     */
    List<HeatmapConfiguration> findByIsActiveTrue();
    
    /**
     * Find active configurations by type
     */
    List<HeatmapConfiguration> findByHeatmapTypeAndIsActiveTrue(HeatmapType heatmapType);
    
    /**
     * Find configuration by name
     */
    Optional<HeatmapConfiguration> findByName(String name);
    
    /**
     * Find configurations by name containing
     */
    List<HeatmapConfiguration> findByNameContainingIgnoreCase(String name);
}



