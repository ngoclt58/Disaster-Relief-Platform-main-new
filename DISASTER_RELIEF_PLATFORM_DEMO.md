# 🚨 Disaster Relief Platform - Client Demo Presentation

## 📋 **1. Platform Overview**

### **What is the Disaster Relief Platform?**
The **Disaster Relief Platform** is a comprehensive, full-stack web 
application designed to coordinate and manage disaster relief operations 
efficiently.it serves as a centralized hub for emergency response teams, 
volunteers, Track and manage critical supplies, equipment, and shelter availability

The Disaster Relief Platform represents a comprehensive solution for 
modern emergency management, combining cutting-edge technology with 
practical disaster response needs.

### **Technology Stack**
- **Backend**: Java (17) Spring Boot (3.2.0) with PostgreSQL + PostGIS for
				geospatial data
- **Frontend**: React.js (18.2.0) with TypeScript (4.9.5)
- **Real-time**: Server-Sent Events (SSE) for live updates
- **AI Integration**: Intelligent chatbot for automated assistance
- **Security**: JWT, Spring Security: 6.x
- ** Database (PostgreSQL + PostGIS)
				PostgreSQL: 15.x hoặc 16.x
				PostGIS: 3.3+ (spatial extension)
				JDBC Driver: postgresql:42.6.0
				Flyway: 9.x (database migration)
				Connection Pool: HikariCP (default)
				
🌐 Frontend (React + TypeScript)
    ↕️ HTTP/REST API
🖥️ Backend (Spring Boot + Java)
    ↕️ JPA/Hibernate
🗄️ Database (PostgreSQL + PostGIS)
    ↕️ Real-time Updates
📡 SSE (Server-Sent Events)
```
---

## 🎯 **2. Core Features & Technical Implementation**

### **2.1 Create Need Request System**

#### **Purpose:**
Allows citizens and relief workers to submit requests for assistance during 
emergencies, including medical aid, food supplies, shelter, and evacuation services.


#### **Technical Implementation:**

**Backend Code Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🎮 controller/ResidentController.java          # POST /residents/needs
├── 🔧 service/ResidentService.java                # Business logic for need creation
├── 🗃️ repository/NeedsRequestRepository.java      # Database operations
├── 📊 entity/NeedsRequest.java                    # JPA entity model
├── 📝 dto/CreateNeedRequest.java                  # Request DTO
└── 🔄 realtime/RealtimeBroadcaster.java          # SSE notifications
```

**Frontend Code Structure:**
```
📁 frontend/src/
├── 📄 pages/DashboardPage.tsx                     # Main dashboard with "Create Need" button
├── 🧩 components/NeedsForm.tsx                    # Request creation form
├── 🔗 services/api.ts                             # API service calls
└── 🎨 components/Layout.tsx                       # Navigation integration
```

**Database Schema:**
```sql
📁 backend/src/main/resources/db/migration/
└── 📄 V1__initial_schema.sql                      # needs_requests table definition
```

---
### **2.2 Interactive Map Visualization**

#### **Purpose:**
Provides real-time geospatial visualization of disaster areas, 
resource locations, active requests, and shelter positions for strategic 
planning and coordination.

#### **Key Capabilities:**
- Real-time request plotting with severity indicators
- Shelter location mapping with capacity status
- Resource hub visualization
- Interactive filtering and search
- Route optimization for emergency vehicles
- Heatmap overlays for incident density

#### **Technical Implementation:**

**Frontend Code Structure:**
```
📁 frontend/src/
├── 📄 pages/MapPage.tsx                           # Main map interface
├── 🔗 services/api.ts                             # Geospatial data APIs
└── 🎨 components/Layout.tsx                       # Map navigation
```

**Backend Geospatial Support:**
```
📁 backend/src/main/java/com/relief/
├── 📊 entity/NeedsRequest.java                    # PostGIS Point geometry
├── 📊 entity/Shelter.java                         # Geographic coordinates
└── 🗃️ repository/*Repository.java                 # Spatial queries
```

**Database Geospatial Schema:**
```sql
📁 backend/src/main/resources/db/migration/
├── 📄 V1__initial_schema.sql                      # PostGIS geometry columns
└── 📄 V2__postgis_indexes.sql                     # Spatial indexes for performance
```

---

### **2.3 Shelter Management System**

#### **Purpose:**
Comprehensive management of emergency shelters including capacity tracking, 
occupancy monitoring, and resource allocation for displaced populations.

#### **Technical Implementation:**

**Backend Code Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🎮 controller/ShelterController.java           # REST API endpoints
├── 🔧 service/ShelterService.java                 # Business logic
├── 🗃️ repository/ShelterRepository.java           # Database operations
├── 📊 entity/Shelter.java                         # JPA entity model
├── 📝 dto/ShelterRequest.java                     # Request DTO
└── 📝 dto/ShelterResponse.java                    # Response DTO
```

**Frontend Code Structure:**
```
📁 frontend/src/
├── 📄 pages/ShelterManagement.tsx                 # Shelter CRUD operations
├── 🔗 services/api.ts                             # Shelter API methods
└── 🎨 components/Layout.tsx                       # Navigation menu
```

**Database Schema:**
```sql
📁 backend/src/main/resources/db/migration/
├── 📄 V24__create_shelters_table.sql              # Initial shelter schema
├── 📄 V25__add_more_shelters.sql                  # Sample data
└── 📄 V26__translate_shelters_to_english.sql      # Localization
```

---

### **2.4 Inventory Management System**

#### **Purpose:**
Comprehensive tracking and management of relief supplies, equipment, 
and resources across multiple distribution hubs with real-time stock 
monitoring.

#### **Key Capabilities:**
- Multi-hub inventory tracking
- Real-time stock level monitoring
- Automated low-stock alerts
- Resource reservation system
- Stock movement history
- Analytics and reporting

#### **Technical Implementation:**

**Backend Code Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🎮 controller/InventoryController.java         # REST API endpoints
├── 🔧 service/InventoryService.java               # Business logic
├── 🗃️ repository/InventoryStockRepository.java    # Stock operations
├── 🗃️ repository/InventoryHubRepository.java      # Hub management
├── 📊 entity/InventoryStock.java                  # Stock entity
├── 📊 entity/InventoryHub.java                    # Hub entity
└── 📊 entity/ItemCatalog.java                     # Item catalog
```

**Frontend Code Structure:**
```
📁 frontend/src/
├── 📄 pages/InventoryPage.tsx                     # Main inventory interface
├── 🧩 components/InventoryManager.tsx             # Inventory management UI
├── 🔗 services/api.ts                             # Inventory API methods
└── 🎨 components/Layout.tsx                       # Navigation integration
```

**Database Schema:**
```sql
📁 backend/src/main/resources/db/migration/
├── 📄 V3__inventory_and_tasks.sql                 # Inventory tables
└── 📄 V21__create_stock_movements_table.sql       # Movement tracking
```

---

### **2.5 Authentication & Security System**

#### **Purpose:**
Robust security framework ensuring secure access control, 
user authentication, and role-based permissions for different user types 
in emergency scenarios.

#### **Key Capabilities:**
- JWT-based authentication
- Role-based access control (Admin, Coordinator, Volunteer, Resident)
- Secure password management
- Session management
- API endpoint protection
- CORS configuration for cross-origin requests

#### **Technical Implementation:**

**Backend Security Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🔐 config/SecurityConfig.java                  # Spring Security configuration
├── 🔐 security/JwtAuthenticationFilter.java       # JWT token processing
├── 🔐 security/JwtTokenProvider.java              # Token generation/validation
├── 🎮 controller/AuthController.java              # Login/logout endpoints
├── 🔧 service/AuthService.java                    # Authentication logic
├── 📊 entity/User.java                            # User entity with roles
└── 🗃️ repository/UserRepository.java              # User data operations
```

**Frontend Authentication:**
```
📁 frontend/src/
├── 📄 pages/LoginPage.tsx                         # Login interface
├── 🧩 components/RouteGuard.tsx                   # Protected route wrapper
├── 🏪 store/authStore.ts                          # Authentication state management
├── 🔗 services/api.ts                             # JWT token handling
└── 🎨 components/Layout.tsx                       # User session display
```

**Database Schema:**
```sql
📁 backend/src/main/resources/db/migration/
└── 📄 V1__initial_schema.sql                      # Users and roles tables
```

---

### **2.6 AI-Powered ChatBot (ReliefBot)**

#### **Purpose:**
Intelligent conversational AI assistant that provides 24/7 support, 
emergency guidance, and platform navigation assistance to users during 
crisis situations.

#### **Key Capabilities:**
- Natural language processing with intent recognition
- Emergency situation auto-detection and escalation
- Multi-language support (English/Vietnamese)
- Context-aware conversations
- Integration with platform features
- Real-time assistance and guidance

#### **Technical Implementation:**

**Backend AI Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🎮 controller/ChatBotController.java           # Chat API endpoints
├── 🔧 service/communication/ChatBotService.java   # AI logic & intent processing
├── 📊 entity/ChatSession.java                     # Session management
└── 🔄 realtime/RealtimeBroadcaster.java          # Real-time chat updates
```

**Frontend Chat Interface:**
```
📁 frontend/src/
├── 🧩 components/communication/ChatBot.tsx        # Chat UI component
├── 🔗 services/chatBotService.ts                  # Chat API integration
└── 📄 pages/CommunicationDashboard.tsx           # Chat integration
```

**AI Intent Configuration:**
- **8 Core Intents**: Greeting, Emergency, Help, Task Status, Create Request, Find Resources, Weather, Goodbye
- **Pattern Matching**: Regex-based keyword recognition
- **Confidence Scoring**: 0.1 to 0.95 confidence levels
- **Emergency Detection**: Auto-escalation for critical situations

---

### **2.7 Stock Alert System**

#### **Purpose:**
Proactive monitoring and alerting system that automatically notifies 
administrators and coordinators when critical supplies fall below safe 
thresholds.

#### **Key Capabilities:**
- Real-time stock level monitoring
- Configurable alert thresholds
- Multi-channel notifications (Email, SMS, In-app)
- Priority-based alert classification
- Automated reorder suggestions
- Historical trend analysis

#### **Technical Implementation:**

**Backend Alert Structure:**
```
📁 backend/src/main/java/com/relief/
├── 🔧 service/AnalyticsService.java               # Stock analytics & alerts
├── 🔧 service/InventoryService.java               # Stock monitoring logic
├── 🧩 components/StockAlerts.tsx                  # Alert display component
├── 🗃️ repository/InventoryStockRepository.java    # Stock queries
└── 🔄 realtime/RealtimeBroadcaster.java          # Real-time notifications
```

**Frontend Alert Display:**
```
📁 frontend/src/
├── 🧩 components/StockAlerts.tsx                  # Alert UI component
├── 📄 pages/DashboardPage.tsx                     # Dashboard integration
└── 🔗 services/api.ts                             # Alert API methods
```

**Alert Logic:**
- **Low Stock Threshold**: < 10 units
- **Critical Stock**: < 1 unit (out of stock)
- **Auto-calculation**: Total stock vs. reserved quantities
- **Real-time Updates**: Instant notifications via SSE

---

## 🏗️ **3. Architecture Overview**

### **System Architecture:**

### **Key Design Patterns:**
- **MVC Architecture**: Clear separation of concerns
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Clean API contracts
- **Service Layer**: Business logic encapsulation
- **Real-time Updates**: Event-driven architecture

### **Security Features:**
- **JWT Authentication**: Stateless token-based auth
- **Role-based Access**: Granular permission control
- **CORS Protection**: Cross-origin request security
- **Input Validation**: Comprehensive data validation
- **SQL Injection Prevention**: Parameterized queries

---
