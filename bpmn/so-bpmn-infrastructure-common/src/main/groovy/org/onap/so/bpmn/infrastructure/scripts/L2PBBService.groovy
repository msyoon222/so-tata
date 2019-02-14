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

public class L2PBBService extends AbstractServiceTaskProcessor {
	String Prefix="L2PBBService"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	/*
	 * Preprocess request from North API
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		utils.log("DEBUG", " *** preProcessRequest() of CreateGenericALaCarteServiceInstance *** ", isDebugEnabled)

		try {

			String siRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(siRequest)

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			utils.log("DEBUG", "Input Request:" + siRequest + " reqId:" + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				serviceInstanceId = UUID.randomUUID().toString()
				utils.log("DEBUG", "Generated new Service Instance ID:" + serviceInstanceId, isDebugEnabled)
			} else {
				utils.log("DEBUG", "Using provided Service Instance ID:" + serviceInstanceId, isDebugEnabled)
			}

			serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceInstanceId)

			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
			
			//requestInfo
			execution.setVariable("source", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source"))
			execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName"))
			execution.setVariable("disableRollback", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.suppressRollback"))
			String productFamilyId = null;
			try {
				productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
			} catch (JSONException e) {
				productFamilyId = null;
			}
			if (isBlank(productFamilyId))
			{
				msg = "Input productFamilyId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}

			//modelInfo
			String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
			if (isBlank(serviceModelInfo)) {
				msg = "Input serviceModelInfo is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("serviceModelInfo", serviceModelInfo)
			}

			utils.log("DEBUG", "modelInfo" + serviceModelInfo,  isDebugEnabled)
		
			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			}
			
			/*
			 * Extracting User Parameters from incoming Request and converting into a Map
			 */
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(siRequest)
			//InputParams
			def userParams = reqMap.requestDetails?.requestParameters?.userParams
			Map<String, String> inputMap = [:]
			if (userParams != null) {
				userParams.each {
					userParam -> inputMap.put(userParam.name.toString(), userParam.value.toString())
				}
			}
			
			utils.log("DEBUG", "User Input Parameters map: " + userParams.toString(), isDebugEnabled)
			execution.setVariable("serviceInputParams", inputMap)
						
			String Host1
			String Host1_Port
			String Host2
			String Host2_Port
			String CIR
			String CBS
			String policy_name
			String Egress_Outer_VID
			String Ingress_Outer_VID
			String ISID
			String ACTION
			/*
			 * Read User parameters for L2PBB service
			 */
			try {		ACTION = inputMap.get('ACTION').toString()		}catch(Exception e) {		ACTION=""			}
			  execution.setVariable("ACTION",ACTION)
			  /*
			   * Service Creation parameters
			   */
			  if(ACTION=="CREATE") {
				try {		Host1 =  inputMap.get('Host1').toString()	}catch(Exception e) {	Host1 = ""		}
				try {		Host1_Port = inputMap.get('Host1_Port').toString()	}catch(Exception e) {	Host1_Port=""	}
				try {		Host2 = inputMap.get('Host2').toString()	}catch(Exception e) {	Host2=""	}
				try {		Host2_Port = inputMap.get('Host2_Port').toString()	}catch(Exception e) {	Host2_Port=""	}
				try {		CIR = inputMap.get('CIR').toString()	}catch(Exception e) {	CIR=""	}
				try {		CBS = inputMap.get('CBS').toString()	}catch(Exception e) {	CBS=""	}
				try {		policy_name = inputMap.get('policy_name').toString()	}catch(Exception e) {	policy_name=""	}
				try {		Egress_Outer_VID = inputMap.get('Egress_Outer_VID').toString()	}catch(Exception e) {	Egress_Outer_VID=""		}
				try {		Ingress_Outer_VID = inputMap.get('Ingress_Outer_VID').toString()	}catch(Exception e) {		Ingress_Outer_VID=""	}
				try {		ISID = inputMap.get('ISID').toString()	}catch(Exception e) {		ISID=""		}

				
				execution.setVariable("Host1",Host1)
				execution.setVariable("Host1_Port",Host1_Port)
				execution.setVariable("Host2",Host2)
				execution.setVariable("Host2_Port",Host2_Port)
								
				execution.setVariable("CIR",CIR)
				execution.setVariable("CBS",CBS)
				execution.setVariable("policy_name",policy_name)
				execution.setVariable("Egress_Outer_VID",Egress_Outer_VID)
				execution.setVariable("Ingress_Outer_VID",Ingress_Outer_VID)
				execution.setVariable("ISID",ISID)
			  }
			
			/*
			* Service Deletion parameters
			*/
			  else if(ACTION=="DELETE")
				  {
				
				  }
			/*
			 * Service Update parameters
			 */
			  else if(ACTION=="UPDATE") {
				try {		CIR = inputMap.get('CIR').toString()	}catch(Exception e) {	CIR=""	}
				try {		CBS = inputMap.get('CBS').toString()	}catch(Exception e) {	CBS=""	}
				try {		policy_name = inputMap.get('policy_name').toString()	}catch(Exception e) {	policy_name=""	}
				
				execution.setVariable("CIR",CIR)
				execution.setVariable("CBS",CBS)
				execution.setVariable("policy_name",policy_name)
				
			}
			//TODO
			//execution.setVariable("failExists", true)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest of CreateGenericALaCarteServiceInstance *****",  isDebugEnabled)
	}
	
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

	
	public void prepareCompletionRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** prepareCompletion *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String source = execution.getVariable("source")
			String ACTION = execution.getVariable("ACTION")
			
			if(ACTION =='CREATE')
			{
			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>CREATE</action>
							<source>VID</source>
			   			</request-info>
						<status-message>Service Instance was created successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
			   			<mso-bpel-name>Create L2PBB Service Completed</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""
			}
			else if(ACTION == 'UPDATE') {
				String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>UPDATE</action>
							<source>VID</source>
			   			</request-info>
						<status-message>Service Instance was updated successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
			   			<mso-bpel-name>Update L2PBB Service Completed</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""
			}
			else if(ACTION == 'DELETE') {
				String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>DELETE</action>
							<source>VID</source>
			   			</request-info>
						<aetgt:status-message>E2E Service Instance was deleted successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>Delete L2PBB Service Completed</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""
	
			}
			
			
			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

			
		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG", "*** Exit prepareCompletionRequest ***", isDebugEnabled)
	}
	
	/*
	 * Error Handling Block
	 */
	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncError *** ", isDebugEnabled)
		
		try {
				String errorMessage =execution.getVariable("WorkflowException")
				
				String buildworkflowException =
					"""Service Reqeust Error:${errorMessage}"""

			utils.logAudit(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage(), isDebugEnabled)
		}

	}
		

}
