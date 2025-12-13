# 🤖 ReliefBot - Kịch Bản Hỗ Trợ & Tương Tác

## 📋 **Tổng Quan**
ReliefBot là AI assistant thông minh cho Disaster Relief Platform, hỗ trợ người dùng trong các tình huống khẩn cấp và quản lý tài nguyên cứu trợ.

---

## 🎯 **Các Kịch Bản Chính**

### 1. 👋 **CHÀO HỎI & KHỞI ĐẦU**

**Trigger Keywords:** `hello`, `hi`, `hey`, `good morning`, `greetings`

**Kịch bản:**
```
User: "Hello"
Bot: "Hello [Tên User]! How can I help you today?"
Quick Replies: [Help] [Status] [Tasks] [Emergency]
```

**Ví dụ thực tế:**
- "Hi there" → "Hi there! I'm here to assist you with the disaster relief platform."
- "Good morning" → "Good morning! What can I do for you?"

---

### 2. 🆘 **TÌNH HUỐNG KHẨN CẤP**

**Trigger Keywords:** `emergency`, `urgent`, `critical`, `help now`, `danger`, `crisis`, `disaster`

**Kịch bản:**
```
User: "Emergency! Need help now!"
Bot: "🚨 EMERGENCY DETECTED! I'm escalating this to emergency responders immediately. Please stay safe and follow emergency protocols."
Actions: [Call Emergency Services] [Create Emergency Request] [View Emergency Procedures]
```

**Các tình huống cụ thể:**
- **Thiên tai:** "Flood emergency!" → Tự động escalate + hướng dẫn an toàn
- **Y tế:** "Medical emergency" → Kết nối với đội y tế + tạo request ưu tiên
- **Hỏa hoạn:** "Fire danger" → Thông báo lực lượng cứu hỏa + evacuation guide

---

### 3. 📝 **TẠO YÊU CẦU HỖ TRỢ**

**Trigger Keywords:** `create request`, `new request`, `submit request`, `need help`, `request help`

**Kịch bản:**
```
User: "I need to create a request for food supplies"
Bot: "I can help you create a new request. Please go to the Requests section and click 'Create New Request' to get started."
Actions: [Create Request] [View Request Types] [Emergency Request]
```

**Các loại request:**
- **Food:** "Need food supplies" → Hướng dẫn tạo food request + inventory check
- **Medical:** "Need medical supplies" → Medical request form + urgent priority
- **Shelter:** "Need temporary shelter" → Shelter request + location services
- **Transportation:** "Need evacuation transport" → Transport request + route planning

---

### 4. 📋 **QUẢN LÝ TASK & TRẠNG THÁI**

**Trigger Keywords:** `task status`, `my tasks`, `task progress`, `what tasks`, `task update`

**Kịch bản:**
```
User: "What are my current tasks?"
Bot: "Let me check your current tasks. You can view all your tasks in the Tasks section of the platform."
Actions: [View My Tasks] [Create New Task] [Update Task Status]
```

**Task scenarios:**
- **Volunteer:** "My volunteer tasks" → Hiển thị assigned tasks + progress
- **Coordinator:** "Team task status" → Team overview + assignment management
- **Driver:** "Delivery tasks" → Route optimization + delivery status

---

### 5. 📦 **TÌM KIẾM TÀI NGUYÊN**

**Trigger Keywords:** `find resources`, `where is`, `location`, `nearby`, `supplies`, `inventory`

**Kịch bản:**
```
User: "Where can I find medical supplies?"
Bot: "I can help you find resources. Check the Inventory section to see available supplies and their locations."
Actions: [View Inventory] [Search Resources] [Check Availability]
```

**Resource scenarios:**
- **Inventory:** "Check water supplies" → Stock levels + hub locations
- **Equipment:** "Need generators" → Equipment availability + reservation
- **Vehicles:** "Available trucks" → Fleet status + booking system

---

### 6. 🌤️ **THÔNG TIN THỜI TIẾT**

**Trigger Keywords:** `weather`, `forecast`, `rain`, `storm`, `temperature`

**Kịch bản:**
```
User: "What's the weather forecast?"
Bot: "For weather information, please check the Weather section in the platform or visit your local weather service."
Actions: [View Weather] [Weather Alerts] [Emergency Weather]
```

**Weather scenarios:**
- **Storm Warning:** "Incoming storm" → Weather alerts + safety protocols
- **Flood Risk:** "Heavy rain forecast" → Flood warnings + evacuation plans

---

### 7. ❓ **HỖ TRỢ & HƯỚNG DẪN**

**Trigger Keywords:** `help`, `assist`, `support`, `what can you do`, `commands`

**Kịch bản:**
```
User: "What can you help me with?"
Bot: "I can help you with:
• Creating and managing requests
• Checking task status  
• Finding resources
• Emergency procedures
• Platform navigation

What would you like to know?"
Actions: [Create Request] [View Tasks] [Find Resources] [Emergency Help]
```

---

### 8. 👋 **KẾT THÚC HỘI THOẠI**

**Trigger Keywords:** `bye`, `goodbye`, `thanks`, `thank you`, `exit`

**Kịch bản:**
```
User: "Thank you, goodbye!"
Bot: "Goodbye [Tên User]! Stay safe and don't hesitate to reach out if you need help."
```

---

## 🎭 **Kịch Bản Tương Tác Phức Tạp**

### **Scenario A: Người Dân Cần Hỗ Trợ Khẩn Cấp**
```
User: "Hi, I need help urgently"
Bot: "Hello! I'm here to help. What kind of urgent assistance do you need?"

User: "My house is flooded, need evacuation"
Bot: "🚨 EMERGENCY DETECTED! I'm escalating this to emergency responders immediately. 
For immediate safety:
1. Move to higher ground if possible
2. Stay away from electrical equipment
3. Emergency services are being notified

Would you like me to create an emergency evacuation request?"

User: "Yes please"
Bot: "I'll help you create an emergency request. Please go to the Dashboard and click 'Create Need' or use the emergency button. 
Make sure to include:
• Your exact location
• Number of people needing evacuation
• Any medical conditions
• Contact information

Emergency services will be prioritized for your request."
```

### **Scenario B: Volunteer Tìm Hiểu Nhiệm Vụ**
```
User: "Good morning, what tasks do I have today?"
Bot: "Good morning! Let me help you check your current tasks."

User: "I'm a volunteer driver"
Bot: "Great! For volunteer drivers, you can:
• View assigned delivery tasks in the Tasks section
• Check route optimization for efficient deliveries
• Update delivery status in real-time
• See inventory pickup locations

Would you like me to guide you to the Tasks page?"

User: "Yes, and how do I update task status?"
Bot: "To update task status:
1. Go to Tasks page
2. Find your assigned task
3. Click on the task to open details
4. Use the status dropdown to update (New → In Progress → Completed)
5. Add notes if needed

This helps coordinators track progress and optimize resource allocation."
```

### **Scenario C: Coordinator Quản Lý Tài Nguyên**
```
User: "I need to check inventory levels"
Bot: "I can help you find resources. Check the Inventory section to see available supplies and their locations."

User: "Specifically medical supplies at Central Hub"
Bot: "For specific inventory queries:
1. Go to Inventory Management
2. Filter by Hub: 'Central Relief Hub'  
3. Filter by Item category: 'Medical'
4. You'll see current stock levels, reserved quantities, and availability

You can also reserve items directly from the inventory page or create restock requests if supplies are low."

User: "How do I reserve supplies for a mission?"
Bot: "To reserve supplies:
1. In Inventory page, find the item you need
2. Click 'Reserve' button next to the item
3. Enter quantity needed
4. Specify the task/mission purpose
5. Confirm reservation

Reserved items will be held for your mission and deducted from available stock."
```

---

## 🔧 **Tính Năng Đặc Biệt**

### **1. Emergency Auto-Escalation**
- Tự động phát hiện từ khóa khẩn cấp
- Trigger emergency protocols
- Thông báo cho emergency responders
- Tạo high-priority requests

### **2. Context-Aware Responses**
- Nhớ thông tin user trong session
- Personalized responses với tên user
- Theo dõi conversation flow
- Smart follow-up questions

### **3. Multi-Language Support** (Future)
- Vietnamese + English responses
- Cultural context awareness
- Local emergency procedures

### **4. Integration với Platform**
- Direct links đến các trang chức năng
- Real-time data từ inventory/tasks
- Status updates và notifications
- Workflow automation

---

## 📊 **Metrics & Analytics**

ReliefBot track các metrics:
- **Response accuracy** - Intent recognition rate
- **User satisfaction** - Feedback scores  
- **Emergency response time** - Time to escalation
- **Task completion rate** - Successful guidance
- **Popular queries** - Most common requests

---

## 🚀 **Cách Sử Dụng Hiệu Quả**

### **Cho Người Dân:**
1. **Khẩn cấp:** Dùng từ "emergency", "urgent" để được ưu tiên
2. **Rõ ràng:** Mô tả cụ thể tình huống cần hỗ trợ
3. **Follow instructions:** Làm theo hướng dẫn của bot

### **Cho Volunteers:**
1. **Check tasks:** Hỏi "my tasks" để xem nhiệm vụ
2. **Update status:** Báo cáo tiến độ thường xuyên
3. **Ask for help:** Hỏi bot khi không rõ quy trình

### **Cho Coordinators:**
1. **Resource management:** Dùng bot để check inventory
2. **Team coordination:** Hỏi về task assignments
3. **Emergency response:** Rely on bot cho emergency protocols

---

**ReliefBot - Your AI Assistant for Disaster Relief Operations** 🤖🆘