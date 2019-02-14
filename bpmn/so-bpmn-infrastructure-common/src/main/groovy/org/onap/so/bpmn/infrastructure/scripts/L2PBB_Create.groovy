/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.rest.APIResponse


import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils
import org.json.JSONException;
/**
 * This groovy class supports the <class>PnfAssociationandConfiguration.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */

public class L2PBB_Create{
	String Prefix="L2PBB_Create"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void sendUnavailabilityResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

		String Host1_check = execution.getVariable("Host1_check")
		String Host2_check = execution.getVariable("Host2_check")
		String Host1_Inventory_Status = execution.getVariable("Host1_Inventory_Status")
		String Host2_Inventory_Status = execution.getVariable("Host2_Inventory_Status")
		
		try {
			// RESTResponse for API Handler (APIH) Reply Task
			String Availability_response = """{"Device Check":{"Host1":"${Host1_check}","Host1 Status":"${Host1_Inventory_Status}" ,"Host2":"${Host2_check}", "Host2 Status":"${Host2_Inventory_Status}" }}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + Availability_response, isDebugEnabled)
			sendWorkflowResponse(execution, 400, Availability_response)
			//execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}
	
	
	
	public void DeviceConfigurationFailResponse (DelegateExecution execution) {
		
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendDeviceConfigurationFailResponse *** ", isDebugEnabled)

		String Host1_SDNC_Message = execution.getVariable("Host1_SDNC_Message")
		String Host2_SDNC_Message = execution.getVariable("Host2_SDNC_Message")
		
	 
		try {
			// RESTResponse for API Handler (APIH) Reply Task
			String Availability_response = """{"Device Configuration Fail":{"Host1":"${Host1_SDNC_Message}",,"Host2":"${Host2_SDNC_Message}"}}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + Availability_response, isDebugEnabled)
			sendWorkflowResponse(execution, 400, Availability_response)
			//execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}
	
	
	// *******************************
	public void prepareDecomposeService(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** Inside prepareDecomposeService of CreateGenericALaCarteServiceInstance ***** ", isDebugEnabled)
		try {
			String siRequest = execution.getVariable("bpmnRequest")
			String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
			execution.setVariable("serviceModelInfo", serviceModelInfo)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		utils.log("DEBUG", " ***** Completed prepareDecomposeService of CreateGenericALaCarteServiceInstance ***** ", isDebugEnabled)
	 }
	
	public void prepareCreateServiceInstance(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareCreateServiceInstance of CreateGenericALaCarteServiceInstance ***** ", isDebugEnabled)

			/*
			 * Extracting User Parameters from incoming Request and converting into a Map
			 */
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			def siRequest = execution.getVariable("bpmnRequest")
			Map reqMap = jsonSlurper.parseText(siRequest)
			//InputParams
			def userParams = reqMap.requestDetails?.requestParameters?.userParams
			Map<String, String> inputMap = [:]
			
			if (userParams != null) {
				userParams.each {
					userParam -> inputMap.put(userParam.name, userParam.value)
				}
			}

			utils.log("DEBUG", "User Input Parameters map: " + userParams.toString(), isDebugEnabled)
			execution.setVariable("serviceInputParams", inputMap)
			
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		   
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			serviceDecomposition.getServiceInstance().setInstanceId(serviceInstanceId)
			
			String serviceInstanceName = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName")
			serviceDecomposition.getServiceInstance().setInstanceName(serviceInstanceName)
			execution.setVariable("serviceInstanceName", serviceInstanceName)
			execution.setVariable("serviceDecomposition", serviceDecomposition)
			execution.setVariable("serviceDecompositionString", serviceDecomposition.toJsonString())
			utils.log("DEBUG", "serviceDecomposition.serviceInstanceName: " + serviceDecomposition.getServiceInstance().getInstanceName(), isDebugEnabled)
						 
			utils.log("DEBUG", " ***** Completed prepareCreateServiceInstance of CreateGenericALaCarteServiceInstance ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. Unexpected Error from method prepareCreateServiceInstance() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	 
	 public void sendSyncResponse (DelegateExecution execution) {
		 def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		 utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)
 
		 try {
			 String requestId = execution.getVariable("msoRequestId")
			 String serviceInstanceId = execution.getVariable("serviceInstanceId")
			 // RESTResponse for API Handler (APIH) Reply Task
			 String createServiceRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + createServiceRestRequest, isDebugEnabled)
			sendWorkflowResponse(execution, 202, createServiceRestRequest)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}


}
