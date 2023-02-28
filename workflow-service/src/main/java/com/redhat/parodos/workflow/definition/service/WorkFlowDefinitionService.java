/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.definition.service;

import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.dto.WorkFlowCheckerDTO;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * workflow definition service
 *
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */
public interface WorkFlowDefinitionService {

	WorkFlowDefinitionResponseDTO save(String workFlowName, String workFlowDescription, WorkFlowType workFlowType,
			Map<String, WorkFlowTask> hmWorkFlowTasks);

	List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitions();

	WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(UUID id);

	List<WorkFlowDefinitionResponseDTO> getWorkFlowDefinitionsByName(String name);

	void saveWorkFlowChecker(String workFlowTaskName, String workFlowCheckerName,
			WorkFlowCheckerDTO workFlowCheckerDTO);

}