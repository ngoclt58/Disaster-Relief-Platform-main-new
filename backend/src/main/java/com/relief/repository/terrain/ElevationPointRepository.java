package com.relief.repository.terrain;

import com.relief.domain.terrain.ElevationPoint;
import com.relief.domain.terrain.ElevationSource;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for elevation point data
 */
@Repository
public interface ElevationPointRepository extends JpaRepository<ElevationPoint, Long> {
    
    /**
     * Find elevation points within a bounding box
     */
    @Query(value = """
        SELECT * FROM elevation_points 
        WHERE ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(:centerLon, :centerLat), 4326))
        """, nativeQuery = true)
    List<ElevationPoint> findWithinBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat,
        @Param("centerLon") double centerLon,
        @Param("centerLat") double centerLat
    );
    
    /**
     * Find the nearest elevation point to a given location
     */
    @Query(value = """
        SELECT * FROM elevation_points 
        ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
        LIMIT 1
        """, nativeQuery = true)
    Optional<ElevationPoint> findNearestToPoint(@Param("lon") double lon, @Param("lat") double lat);
    
    /**
     * Find elevation points within a radius of a given point
     */
    @Query(value = """
        SELECT * FROM elevation_points 
        WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :radiusMeters)
        ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
        """, nativeQuery = true)
    List<ElevationPoint> findWithinRadius(
        @Param("lon") double lon,
        @Param("lat") double lat,
        @Param("radiusMeters") double radiusMeters
    );
    
    /**
     * Find elevation points by source
     */
    List<ElevationPoint> findBySource(ElevationSource source);
    
    /**
     * Get elevation statistics for a bounding box
     */
    @Query(value = """
        SELECT 
            MIN(elevation) as min_elevation,
            MAX(elevation) as max_elevation,
            AVG(elevation) as avg_elevation,
            STDDEV(elevation) as elevation_stddev,
            COUNT(*) as point_count
        FROM elevation_points 
        WHERE ST_Within(location, ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326))
        """, nativeQuery = true)
    ElevationStatistics getStatisticsForBounds(
        @Param("minLon") double minLon,
        @Param("minLat") double minLat,
        @Param("maxLon") double maxLon,
        @Param("maxLat") double maxLat
    );
    
    /**
     * Interface for elevation statistics projection
     */
    interface ElevationStatistics {
        Double getMinElevation();
        Double getMaxElevation();
        Double getAvgElevation();
        Double getElevationStddev();
        Long getPointCount();
    }
}



