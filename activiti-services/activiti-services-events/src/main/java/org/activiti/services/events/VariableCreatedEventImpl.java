/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.events;

public class VariableCreatedEventImpl extends AbstractProcessEngineEvent implements VariableCreatedEvent {

    protected String variableName;
    protected String variableValue;
    protected String variableType;
    protected String taskId;

    public VariableCreatedEventImpl() {
    }

    public VariableCreatedEventImpl(String executionId,
                                    String processDefinitionId,
                                    String processInstanceId,
                                    String variableName,
                                    String variableValue,
                                    String variableType,
                                    String taskId) {
        super(executionId,
              processDefinitionId,
              processInstanceId);
        this.variableName = variableName;
        this.variableValue = variableValue;
        this.variableType = variableType;
        this.taskId = taskId;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    @Override
    public String getVariableValue() {
        return variableValue;
    }

    @Override
    public String getVariableType() {
        return variableType;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getEventType() {
        return "VariableCreatedEvent";
    }
}
