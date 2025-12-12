# Heatmap Analytics & Visualization

## Overview

The Heatmap Analytics feature provides powerful data visualization capabilities for disaster relief operations, enabling real-time analysis of disaster impact, resource distribution, and response effectiveness through interactive heatmap visualizations.

## Features

### 🔥 **Multi-Type Heatmap Visualization**
- **Disaster Impact**: Visual representation of disaster severity and affected areas
- **Resource Distribution**: Density mapping of relief resources and supplies
- **Response Effectiveness**: Performance metrics and response coverage analysis
- **Needs Density**: Concentration of emergency needs and requests
- **Task Concentration**: Workload distribution and task clustering
- **Volunteer Activity**: Volunteer engagement and activity hotspots
- **Infrastructure Damage**: Critical infrastructure damage assessment
- **Population Density**: Population distribution and evacuation planning
- **Emergency Services**: Service coverage and response capabilities
- **Supply Chains**: Logistics and supply chain activity mapping

### 📊 **Advanced Data Processing**
- **Real-time Aggregation**: Dynamic data point aggregation and processing
- **Intensity Calculation**: Sophisticated intensity scoring algorithms
- **Weighted Analysis**: Multi-factor weighting for accurate representation
- **Spatial Clustering**: Geographic clustering for pattern recognition
- **Temporal Analysis**: Time-based heatmap evolution and trends
- **Statistical Analysis**: Comprehensive statistics and metrics calculation

### 🗺️ **Interactive Visualization**
- **MapLibre Integration**: High-performance map rendering with heatmap overlays
- **Multi-layer Display**: Toggle between different heatmap types and layers
- **Click-to-Analyze**: Interactive point inspection and detailed information
- **Zoom-adaptive Rendering**: Dynamic heatmap resolution based on zoom level
- **Color-coded Intensity**: Intuitive color schemes for different data types
- **Responsive Design**: Mobile and desktop optimized visualization

### ⚙️ **Configurable Layer Generation**
- **Custom Configurations**: Flexible heatmap generation parameters
- **Color Scheme Customization**: Customizable color palettes and gradients
- **Intensity Range Control**: Configurable intensity scaling and normalization
- **Radius and Blur Settings**: Adjustable influence radius and blur effects
- **Aggregation Methods**: Multiple data aggregation algorithms (SUM, AVG, MAX, MIN, COUNT)
- **Time Window Control**: Configurable temporal data filtering

## Technical Architecture

### Backend Components

#### **Domain Models**
```java
// Heatmap data points
@Entity
public class HeatmapData {
    private Point location;              // Geographic coordinates
    private HeatmapType heatmapType;     // Type of heatmap data
    private Double intensity;            // Intensity value (0.0-1.0)
    private Double weight;               // Weight for aggregation
    private Double radius;               // Influence radius in meters
    private String category;             // Optional category grouping
    private String metadata;             // Additional JSON data
    private Long sourceId;               // Source record ID
    private String sourceType;           // Source record type
}

// Heatmap layer configuration
@Entity
public class HeatmapConfiguration {
    private String name;                 // Configuration name
    private HeatmapType heatmapType;     // Target heatmap type
    private String colorScheme;          // Color configuration JSON
    private Double intensityRangeMin;    // Minimum intensity value
    private Double intensityRangeMax;    // Maximum intensity value
    private Double radiusMultiplier;     // Radius scaling factor
    private Double opacity;              // Layer opacity (0.0-1.0)
    private Double blurRadius;           // Blur radius in pixels
    private String aggregationMethod;    // Data aggregation method
    private Integer timeWindowHours;     // Time window for data
    private Double spatialResolutionMeters; // Spatial resolution
}

// Generated heatmap layers
@Entity
public class HeatmapLayer {
    private String name;                 // Layer name
    private HeatmapType heatmapType;     // Layer type
    private Polygon bounds;              // Geographic bounds
    private String tileUrlTemplate;      // Map tile URL template
    private Integer minZoom;             // Minimum zoom level
    private Integer maxZoom;             // Maximum zoom level
    private Long dataPointsCount;        // Number of data points
    private Double intensityMin;         // Minimum intensity
    private Double intensityMax;         // Maximum intensity
    private Double intensityAvg;         // Average intensity
    private Boolean isPublic;            // Public accessibility
    private LocalDateTime expiresAt;     // Optional expiration
}
```

#### **Services**
- **HeatmapDataService**: Manages heatmap data points and aggregation
- **HeatmapVisualizationService**: Handles layer generation and visualization
- **HeatmapConfigurationService**: Manages heatmap configurations and settings

#### **REST API Endpoints**
```
POST /api/heatmap/data                    # Add heatmap data point
POST /api/heatmap/data/bulk              # Bulk add heatmap data
GET  /api/heatmap/data/bounds            # Get data within bounds
GET  /api/heatmap/data/type/{type}       # Get data by type
GET  /api/heatmap/data/statistics        # Get heatmap statistics

POST /api/heatmap/layers                 # Generate heatmap layer
GET  /api/heatmap/layers/{id}            # Get specific layer
GET  /api/heatmap/layers/type/{type}     # Get layers by type
GET  /api/heatmap/layers/public          # Get public layers
GET  /api/heatmap/layers/bounds          # Get layers within bounds
GET  /api/heatmap/tiles/{layerId}        # Generate heatmap tiles
```

### Frontend Components

#### **HeatmapVisualization Component**
- Interactive MapLibre GL JS integration
- Real-time heatmap rendering and updates
- Multi-layer display with toggle controls
- Click-to-analyze point inspection
- Responsive design for all devices

#### **HeatmapConfigurationPanel Component**
- Layer generation interface
- Configuration parameter management
- Heatmap type selection
- Preview and validation
- Generation progress tracking

#### **HeatmapDashboard Component**
- Comprehensive heatmap analytics interface
- Statistics and metrics display
- Layer management and visualization
- Real-time data monitoring
- Interactive controls and settings

### Database Schema

#### **Heatmap Data Table**
```sql
CREATE TABLE heatmap_data (
    id BIGSERIAL PRIMARY KEY,
    location GEOMETRY(Point, 4326) NOT NULL,
    heatmap_type VARCHAR(50) NOT NULL,
    intensity DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    radius DOUBLE PRECISION NOT NULL,
    category VARCHAR(100),
    metadata JSONB,
    source_id BIGINT,
    source_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### **Heatmap Configurations Table**
```sql
CREATE TABLE heatmap_configurations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    heatmap_type VARCHAR(50) NOT NULL,
    color_scheme TEXT NOT NULL,
    intensity_range_min DOUBLE PRECISION NOT NULL,
    intensity_range_max DOUBLE PRECISION NOT NULL,
    radius_multiplier DOUBLE PRECISION NOT NULL,
    opacity DOUBLE PRECISION NOT NULL,
    blur_radius DOUBLE PRECISION NOT NULL,
    aggregation_method VARCHAR(20) NOT NULL,
    time_window_hours INTEGER,
    spatial_resolution_meters DOUBLE PRECISION,
    is_active BOOLEAN NOT NULL DEFAULT true
);
```

#### **Heatmap Layers Table**
```sql
CREATE TABLE heatmap_layers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    heatmap_type VARCHAR(50) NOT NULL,
    bounds GEOMETRY(Polygon, 4326) NOT NULL,
    tile_url_template VARCHAR(500) NOT NULL,
    min_zoom INTEGER NOT NULL,
    max_zoom INTEGER NOT NULL,
    data_points_count BIGINT NOT NULL,
    intensity_min DOUBLE PRECISION NOT NULL,
    intensity_max DOUBLE PRECISION NOT NULL,
    intensity_avg DOUBLE PRECISION NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP
);
```

## Usage Examples

### **1. Adding Heatmap Data**
```typescript
// Add single heatmap data point
const heatmapData = await HeatmapService.addHeatmapData({
  longitude: -74.0,
  latitude: 40.7,
  heatmapType: 'DISASTER_IMPACT',
  intensity: 0.8,
  weight: 1.5,
  radius: 1000,
  category: 'FLOODING',
  metadata: '{"severity": "high", "affected_people": 500}',
  sourceId: 123,
  sourceType: 'NEED'
});

// Bulk add heatmap data
const bulkData = await HeatmapService.bulkAddHeatmapData([
  {
    longitude: -74.0, latitude: 40.7,
    heatmapType: 'RESOURCE_DISTRIBUTION',
    intensity: 0.6, weight: 1.2, radius: 800,
    category: 'MEDICAL', sourceId: 1, sourceType: 'RESOURCE'
  },
  {
    longitude: -74.1, latitude: 40.7,
    heatmapType: 'RESOURCE_DISTRIBUTION',
    intensity: 0.7, weight: 1.3, radius: 900,
    category: 'FOOD', sourceId: 2, sourceType: 'RESOURCE'
  }
]);
```

### **2. Generating Heatmap Layers**
```typescript
// Generate heatmap layer
const layer = await HeatmapService.generateHeatmapLayer({
  name: 'Disaster Impact Analysis',
  description: 'Real-time disaster impact visualization',
  heatmapType: 'DISASTER_IMPACT',
  isPublic: true,
  generationParameters: '{"radius": 1000, "intensity": 0.8, "opacity": 0.6}'
});

// Get heatmap layers by type
const layers = await HeatmapService.getHeatmapLayersByType('DISASTER_IMPACT');

// Get public heatmap layers
const publicLayers = await HeatmapService.getPublicHeatmapLayers();
```

### **3. Querying Heatmap Data**
```typescript
// Get heatmap data within bounds
const data = await HeatmapService.getHeatmapDataInBounds(
  -74.1, 40.6, -73.9, 40.8
);

// Get heatmap data by type
const disasterData = await HeatmapService.getHeatmapDataByType('DISASTER_IMPACT');

// Get heatmap statistics
const stats = await HeatmapService.getHeatmapStatistics(
  'DISASTER_IMPACT', '2024-01-01T00:00:00Z', '2024-01-31T23:59:59Z'
);
```

### **4. MapLibre Integration**
```typescript
// Generate heatmap source for MapLibre
const heatmapSource = HeatmapService.generateHeatmapSource(heatmapData);

// Generate heatmap layer
const heatmapLayer = HeatmapService.generateHeatmapLayer('DISASTER_IMPACT');

// Generate circle layer for individual points
const circleLayer = HeatmapService.generateCircleLayer('DISASTER_IMPACT');

// Add to map
map.addSource('heatmap-source', heatmapSource);
map.addLayer(heatmapLayer);
map.addLayer(circleLayer);
```

## Configuration

### **Backend Configuration**
```yaml
# application.yml
heatmap:
  data:
    max-points-per-request: 10000
    default-radius: 1000
    default-weight: 1.0
    intensity-range:
      min: 0.0
      max: 1.0
  visualization:
    default-opacity: 0.6
    default-blur-radius: 15.0
    color-schemes:
      disaster-impact: ["#00ff00", "#ffff00", "#ff8800", "#ff0000"]
      resource-distribution: ["#0000ff", "#0088ff", "#00ffff", "#88ff00"]
      response-effectiveness: ["#ff0000", "#ff8800", "#ffff00", "#00ff00"]
    aggregation-methods: ["SUM", "AVG", "MAX", "MIN", "COUNT"]
  layers:
    max-layers-per-user: 50
    default-expiration-hours: 24
    tile-cache-ttl: 3600
```

### **Frontend Configuration**
```typescript
// Heatmap visualization options
const heatmapOptions = {
  showHeatmap: true,
  showPoints: true,
  opacity: 0.6,
  blurRadius: 15,
  intensityRange: [0, 1],
  colorScheme: 'default',
  autoRefresh: true,
  refreshInterval: 300000, // 5 minutes
  maxDataPoints: 10000
};
```

## Performance Considerations

### **Data Processing Optimization**
- **Spatial Indexing**: GIST indexes on geometry columns for fast spatial queries
- **Data Aggregation**: Efficient aggregation algorithms for large datasets
- **Caching Strategies**: Redis caching for frequently accessed heatmap data
- **Batch Processing**: Bulk operations for large data imports

### **Visualization Performance**
- **Tile-based Rendering**: Efficient map tile generation and caching
- **Level-of-Detail**: Dynamic detail based on zoom level
- **Data Clustering**: Spatial clustering for performance optimization
- **Memory Management**: Efficient memory usage for large datasets

### **API Performance**
- **Response Compression**: Gzip compression for large data responses
- **Pagination**: Limit results for large queries
- **Rate Limiting**: Prevent abuse of expensive operations
- **Caching Headers**: Proper HTTP caching for static resources

## Security Considerations

### **Data Access Control**
- **Role-based Access**: Different access levels for different user roles
- **Geographic Restrictions**: Limit access to specific regions
- **API Authentication**: Secure API access with JWT tokens
- **Data Encryption**: Encrypt sensitive heatmap metadata

### **Layer Security**
- **Public/Private Layers**: Control layer visibility and access
- **Expiration Management**: Automatic cleanup of expired layers
- **Access Logging**: Track all layer access and downloads
- **Metadata Sanitization**: Remove sensitive information from metadata

## Monitoring & Analytics

### **Key Metrics**
- **Data Processing Performance**: Processing times and success rates
- **Visualization Performance**: Rendering times and user interactions
- **Layer Generation**: Layer creation success rates and performance
- **User Engagement**: Heatmap usage patterns and feature adoption

### **Alerting**
- **Processing Failures**: Alert on heatmap generation errors
- **Performance Issues**: Alert on slow API responses
- **Storage Capacity**: Alert on disk space usage
- **Layer Expiration**: Alert on layer expiration and cleanup

## Integration Examples

### **External Data Sources**
```typescript
// Integration with needs data
const needsData = await NeedsService.getNeedsInBounds(minLon, minLat, maxLon, maxLat);
const heatmapData = needsData.map(need => ({
  longitude: need.longitude,
  latitude: need.latitude,
  heatmapType: 'NEEDS_DENSITY',
  intensity: calculateNeedIntensity(need),
  weight: calculateNeedWeight(need),
  radius: calculateNeedRadius(need),
  category: need.category,
  sourceId: need.id,
  sourceType: 'NEED'
}));

// Integration with task data
const tasksData = await TasksService.getTasksInBounds(minLon, minLat, maxLon, maxLat);
const taskHeatmapData = tasksData.map(task => ({
  longitude: task.longitude,
  latitude: task.latitude,
  heatmapType: 'TASK_CONCENTRATION',
  intensity: calculateTaskIntensity(task),
  weight: calculateTaskWeight(task),
  radius: calculateTaskRadius(task),
  category: task.category,
  sourceId: task.id,
  sourceType: 'TASK'
}));
```

### **Real-time Updates**
```typescript
// WebSocket integration for real-time updates
const socket = new WebSocket('ws://localhost:8080/ws/heatmap');

socket.onmessage = (event) => {
  const update = JSON.parse(event.data);
  if (update.type === 'HEATMAP_DATA_UPDATE') {
    // Update heatmap data
    updateHeatmapData(update.data);
  } else if (update.type === 'HEATMAP_LAYER_UPDATE') {
    // Update heatmap layer
    updateHeatmapLayer(update.data);
  }
};
```

## Future Enhancements

### **Planned Features**
- **Machine Learning Integration**: AI-powered heatmap analysis and prediction
- **3D Visualization**: Three-dimensional heatmap rendering
- **Mobile Optimization**: Enhanced mobile heatmap features
- **Offline Capabilities**: Cached heatmap data for offline use
- **Advanced Analytics**: Statistical analysis and trend detection

### **Integration Opportunities**
- **IoT Sensors**: Real-time sensor data integration
- **Social Media**: Social media sentiment heatmaps
- **Weather Data**: Weather pattern correlation analysis
- **Traffic Data**: Traffic flow and congestion mapping
- **Crowdsourcing**: Citizen-reported data integration

## Troubleshooting

### **Common Issues**

#### **Heatmap Rendering Problems**
- Check MapLibre GL JS version compatibility
- Verify heatmap data format and coordinates
- Check browser WebGL support
- Review console for rendering errors

#### **Performance Issues**
- Monitor data point count and aggregation settings
- Check spatial indexing and query performance
- Review memory usage and garbage collection
- Optimize heatmap layer configurations

#### **Data Accuracy Issues**
- Verify coordinate system and projection
- Check intensity calculation algorithms
- Review weight and radius settings
- Validate data source accuracy

### **Debug Tools**
- **Heatmap Data Inspector**: Browser tool for data validation
- **Performance Profiler**: Rendering performance analysis
- **API Testing**: Postman collection for API testing
- **Database Queries**: SQL queries for data validation

## Support

For technical support or feature requests related to Heatmap Analytics:

1. **Documentation**: Check this guide and API documentation
2. **Logs**: Review application and processing logs
3. **Database**: Check heatmap data and layer tables
4. **Community**: Post questions in the development team chat
5. **Issues**: Create GitHub issues for bugs or feature requests

---

*This feature significantly enhances the disaster relief platform's data visualization capabilities, providing powerful heatmap analytics for comprehensive analysis of disaster impact, resource distribution, and response effectiveness.*



