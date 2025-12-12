package com.relief.service.workflow;

/**
 * Workflow condition for conditional execution
 */
public class WorkflowCondition {
    private String variable;
    private String operator;
    private Object expectedValue;

    public WorkflowCondition() {}

    public WorkflowCondition(String variable, String operator, Object expectedValue) {
        this.variable = variable;
        this.operator = operator;
        this.expectedValue = expectedValue;
    }

    public String getVariable() { return variable; }
    public void setVariable(String variable) { this.variable = variable; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Object getExpectedValue() { return expectedValue; }
    public void setExpectedValue(Object expectedValue) { this.expectedValue = expectedValue; }
}


