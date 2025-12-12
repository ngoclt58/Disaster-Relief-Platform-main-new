-- Create workflow templates table
CREATE TABLE IF NOT EXISTS workflow_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    template_data JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create workflow executions table
CREATE TABLE IF NOT EXISTS workflow_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id VARCHAR(100) NOT NULL UNIQUE,
    request_id UUID REFERENCES needs_requests(id),
    workflow_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL, -- COMPLETED, FAILED, ERROR, IN_PROGRESS
    error_message TEXT,
    execution_data JSONB,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create indexes
CREATE INDEX idx_workflow_templates_name ON workflow_templates(name);
CREATE INDEX idx_workflow_templates_active ON workflow_templates(is_active);
CREATE INDEX idx_workflow_executions_execution_id ON workflow_executions(execution_id);
CREATE INDEX idx_workflow_executions_request_id ON workflow_executions(request_id);
CREATE INDEX idx_workflow_executions_status ON workflow_executions(status);
CREATE INDEX idx_workflow_executions_workflow_type ON workflow_executions(workflow_type);
CREATE INDEX idx_workflow_executions_start_time ON workflow_executions(start_time);

-- Insert default workflow templates
INSERT INTO workflow_templates (name, description, template_data) VALUES
(
    'MEDICAL_EMERGENCY',
    'High-priority medical emergency response workflow',
    '{
        "steps": [
            {"name": "IMMEDIATE_ALERT", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "DISPATCHER", "message": "Medical emergency reported - immediate response required"}},
            {"name": "CREATE_MEDICAL_RESPONSE", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "MEDICAL_RESPONSE", "assigneeRole": "HELPER"}},
            {"name": "ASSIGN_USER", "type": "ASSIGN_USER", "required": true, "parameters": {"assigneeRole": "HELPER"}},
            {"name": "ESCALATION_ALERT", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "ADMIN", "message": "Medical emergency escalated to admin"}},
            {"name": "WAIT_300", "type": "WAIT_FOR_CONDITION", "required": false, "parameters": {"waitSeconds": 300}},
            {"name": "CONDITIONAL", "type": "CONDITIONAL_BRANCH", "required": false, "condition": {"variable": "taskStatus", "operator": "not_equals", "expectedValue": "completed"}, "trueStep": {"name": "FOLLOW_UP", "type": "SEND_NOTIFICATION", "required": false, "parameters": {"recipientRole": "DISPATCHER", "message": "Medical emergency still pending - follow up required"}}}
        ]
    }'::jsonb
),
(
    'FOOD_REQUEST',
    'Standard food assistance workflow',
    '{
        "steps": [
            {"name": "CREATE_FOOD_DELIVERY", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "FOOD_DELIVERY", "assigneeRole": "HELPER"}},
            {"name": "ASSIGN_USER", "type": "ASSIGN_USER", "required": true, "parameters": {"assigneeRole": "HELPER"}},
            {"name": "TASK_ASSIGNED", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "HELPER", "message": "Food delivery task assigned"}},
            {"name": "WAIT_3600", "type": "WAIT_FOR_CONDITION", "required": false, "parameters": {"waitSeconds": 3600}},
            {"name": "CONDITIONAL", "type": "CONDITIONAL_BRANCH", "required": false, "condition": {"variable": "taskStatus", "operator": "not_equals", "expectedValue": "picked_up"}, "trueStep": {"name": "REMINDER", "type": "SEND_NOTIFICATION", "required": false, "parameters": {"recipientRole": "HELPER", "message": "Food delivery task reminder"}}}
        ]
    }'::jsonb
),
(
    'EVACUATION',
    'Emergency evacuation coordination workflow',
    '{
        "steps": [
            {"name": "EVACUATION_ALERT", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "DISPATCHER", "message": "Evacuation required - coordinate response"}},
            {"name": "CREATE_EVACUATION_COORDINATION", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "EVACUATION_COORDINATION", "assigneeRole": "DISPATCHER"}},
            {"name": "PARALLEL_EXECUTION", "type": "PARALLEL_EXECUTION", "required": true, "parallelSteps": [
                {"name": "RESIDENT_ALERT", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "RESIDENT", "message": "Evacuation in progress - follow instructions"}},
                {"name": "CREATE_TRANSPORTATION", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "TRANSPORTATION", "assigneeRole": "HELPER"}},
                {"name": "CREATE_SHELTER_COORDINATION", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "SHELTER_COORDINATION", "assigneeRole": "HELPER"}}
            ]},
            {"name": "WAIT_1800", "type": "WAIT_FOR_CONDITION", "required": false, "parameters": {"waitSeconds": 1800}},
            {"name": "CONDITIONAL", "type": "CONDITIONAL_BRANCH", "required": false, "condition": {"variable": "evacuationStatus", "operator": "not_equals", "expectedValue": "completed"}, "trueStep": {"name": "ESCALATION", "type": "SEND_NOTIFICATION", "required": false, "parameters": {"recipientRole": "ADMIN", "message": "Evacuation not completed - escalate to admin"}}}
        ]
    }'::jsonb
),
(
    'HIGH_PRIORITY',
    'High priority request workflow with escalation',
    '{
        "steps": [
            {"name": "CREATE_URGENT_RESPONSE", "type": "CREATE_TASK", "required": true, "parameters": {"taskType": "URGENT_RESPONSE", "assigneeRole": "HELPER"}},
            {"name": "ASSIGN_USER", "type": "ASSIGN_USER", "required": true, "parameters": {"assigneeRole": "HELPER"}},
            {"name": "URGENT_TASK", "type": "SEND_NOTIFICATION", "required": true, "parameters": {"recipientRole": "HELPER", "message": "High priority task assigned - respond quickly"}},
            {"name": "WAIT_1800", "type": "WAIT_FOR_CONDITION", "required": false, "parameters": {"waitSeconds": 1800}},
            {"name": "CONDITIONAL", "type": "CONDITIONAL_BRANCH", "required": false, "condition": {"variable": "taskStatus", "operator": "not_equals", "expectedValue": "in_progress"}, "trueStep": {"name": "ESCALATION", "type": "SEND_NOTIFICATION", "required": false, "parameters": {"recipientRole": "DISPATCHER", "message": "High priority task not started - escalate"}}},
            {"name": "WAIT_3600", "type": "WAIT_FOR_CONDITION", "required": false, "parameters": {"waitSeconds": 3600}},
            {"name": "CONDITIONAL_FINAL", "type": "CONDITIONAL_BRANCH", "required": false, "condition": {"variable": "taskStatus", "operator": "not_equals", "expectedValue": "completed"}, "trueStep": {"name": "ADMIN_ESCALATION", "type": "SEND_NOTIFICATION", "required": false, "parameters": {"recipientRole": "ADMIN", "message": "High priority task not completed - admin intervention required"}}}
        ]
    }'::jsonb
)
ON CONFLICT (name) DO NOTHING;

COMMENT ON TABLE workflow_templates IS 'Stores workflow templates for automated request processing';
COMMENT ON TABLE workflow_executions IS 'Stores workflow execution history and status';
COMMENT ON COLUMN workflow_templates.template_data IS 'JSON structure containing workflow steps and configuration';
COMMENT ON COLUMN workflow_executions.execution_data IS 'JSON structure containing execution results and step outputs';

