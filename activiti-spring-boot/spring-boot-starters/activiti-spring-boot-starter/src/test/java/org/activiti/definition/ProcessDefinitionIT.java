/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.definition;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.keycloak.KeycloakEnabledBaseTestIT;
import org.activiti.services.core.model.MetaBpmnModel;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessDefinitionMeta;
import org.activiti.services.core.model.ProcessModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class ProcessDefinitionIT extends KeycloakEnabledBaseTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String PROCESS_DEFINITIONS_URL = "/v1/process-definitions/";
    private static final String PROCESS_WITH_VARIABLES_2 = "ProcessWithVariables2";
    private static final String PROCESS_POOL_LANE = "process_pool1";

    @Test
    public void shouldRetrieveListOfProcessDefinition() throws Exception {
        //given
        //processes are automatically deployed from src/test/resources/processes

        //when
        ResponseEntity<PagedResources<ProcessDefinition>> entity = getProcessDefinitions();

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getContent()).extracting(ProcessDefinition::getName).contains(
                                                                                                  "ProcessWithVariables",
                                                                                                  "ProcessWithVariables2",
                                                                                                  "process_pool1",
                                                                                                  "SimpleProcess",
                                                                                                  "ProcessWithBoundarySignal");
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return restTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                     getRequestEntityWithHeaders(),
                                     responseType);

    }

    @Test
    public void shouldReturnProcessDefinitionById() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinition> responseType = new ParameterizedTypeReference<ProcessDefinition>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsEntity.getBody().getContent().iterator().next();

        //when
        ResponseEntity<ProcessDefinition> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId(),
                                                                         HttpMethod.GET,
                                                                         getRequestEntityWithHeaders(),
                                                                         responseType);

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(aProcessDefinition.getId());
    }

    @Test
    public void shouldReturnProcessDefinitionMetadata() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinitionMeta> responseType = new ParameterizedTypeReference<ProcessDefinitionMeta>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = null;

        Iterator<ProcessDefinition> it = processDefinitionsEntity.getBody().getContent().iterator();
        do {
            aProcessDefinition = it.next();
        } while (!aProcessDefinition.getName().equals(PROCESS_WITH_VARIABLES_2));

        //when
        ResponseEntity<ProcessDefinitionMeta> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId() + "/meta",
                                                                             HttpMethod.GET,
                                                                             getRequestEntityWithHeaders(),
                                                                             responseType);
        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getVariables()).hasSize(3);
        assertThat(entity.getBody().getUsers()).hasSize(4);
        assertThat(entity.getBody().getGroups()).hasSize(4);
        assertThat(entity.getBody().getUserTasks()).hasSize(2);
        assertThat(entity.getBody().getServiceTasks()).hasSize(2);
    }

    @Test
    public void shouldReturnProcessDefinitionMetadataForPoolLane() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinitionMeta> responseType = new ParameterizedTypeReference<ProcessDefinitionMeta>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = null;

        Iterator<ProcessDefinition> it = processDefinitionsEntity.getBody().getContent().iterator();
        do {
            aProcessDefinition = it.next();
        } while (!aProcessDefinition.getName().equals(PROCESS_POOL_LANE));

        //when
        ResponseEntity<ProcessDefinitionMeta> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId() + "/meta",
                                                                             HttpMethod.GET,
                                                                             getRequestEntityWithHeaders(),
                                                                             responseType);
        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getVariables()).hasSize(6);
        assertThat(entity.getBody().getUsers()).hasSize(4);
        assertThat(entity.getBody().getGroups()).hasSize(4);
        assertThat(entity.getBody().getUserTasks()).hasSize(3);
        assertThat(entity.getBody().getServiceTasks()).hasSize(3);
    }

    @Test
    public void shouldRetriveProcessModel() throws Exception {
        //given
        ParameterizedTypeReference<ProcessModel> responseType = new ParameterizedTypeReference<ProcessModel>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsEntity.getBody().getContent().iterator().next();

        //when
        ResponseEntity<ProcessModel> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId() + "/xml",
                                                                    HttpMethod.GET,
                                                                    getRequestEntityWithHeaders(),
                                                                    responseType);

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(aProcessDefinition.getId());
        assertThat(entity.getBody().getContent()).isEqualTo(getProcessXml(aProcessDefinition.getId().split(":")[0]));
    }

    private String getProcessXml(final String processDefinitionKey) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("processes/" + processDefinitionKey + ".bpmn20.xml")) {
            return new String(IoUtil.readInputStream(is, null), "UTF-8");
        }
    }

    @Test
    public void shouldRetriveBpmnModel() throws Exception {
        //given
        ParameterizedTypeReference<MetaBpmnModel> responseType = new ParameterizedTypeReference<MetaBpmnModel>() {
        };
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsEntity.getBody().getContent().iterator().next();

        //when
        ResponseEntity<MetaBpmnModel> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId() + "/json",
                                                                     HttpMethod.GET,
                                                                     getRequestEntityWithHeaders(),
                                                                     responseType);

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(aProcessDefinition.getId());

        BpmnModel targetModel = entity.getBody().getContent();
        final InputStream byteArrayInputStream = new ByteArrayInputStream(getProcessXml(aProcessDefinition.getId()
                                                                                                          .split(":")[0]).getBytes());
        BpmnModel sourceModel = bpmnXMLConverter.convertToBpmnModel(new InputStreamProvider() {

            @Override
            public InputStream getInputStream() {
                return byteArrayInputStream;
            }
        }, false, false);
        assertThat(targetModel.getMainProcess().getId().equals(sourceModel.getMainProcess().getId()));
        for (FlowElement element : targetModel.getMainProcess().getFlowElements()) {
            assertThat(sourceModel.getFlowElement(element.getId()) != null);
        }
    }
}