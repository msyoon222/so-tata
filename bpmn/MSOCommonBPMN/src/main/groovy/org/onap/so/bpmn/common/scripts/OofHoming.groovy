/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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
package org.onap.so.bpmn.common.scripts

import org.onap.so.bpmn.core.UrnPropertiesReader
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import org.onap.so.bpmn.core.domain.InventoryType
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.Subscriber
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.catalog.beans.AuthenticationType
import org.onap.so.db.catalog.beans.CloudIdentity
import org.onap.so.db.catalog.beans.CloudSite
import org.onap.so.db.catalog.beans.HomingInstance
import org.onap.so.db.catalog.beans.ServerType
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

import org.json.JSONArray
import org.json.JSONObject
import org.springframework.web.util.UriUtils

import static org.onap.so.bpmn.common.scripts.GenericUtils.*

/**
 * This class contains the scripts used
 * by the OOF Homing Subflow building block. The
 * subflow attempts to home the provided
 * resources by calling OOF.
 */
class OofHoming extends AbstractServiceTaskProcessor {

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    OofUtils oofUtils = new OofUtils(this)

    /**
     * This method validates the incoming variables.
     * The method then prepares the OOF request
     * and posts it to OOF's rest api.
     *
     * @param execution
     */
    public void callOof(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix", "HOME_")
        utils.log("DEBUG", "*** Started Homing Call OOF ***", isDebugEnabled)
        try {
            execution.setVariable("rollbackData", null)
            execution.setVariable("rolledBack", false)

            String requestId = execution.getVariable("msoRequestId")
            utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)
            String serviceInstanceName = execution.getVariable("serviceInstanceName")
            utils.log("DEBUG", "Incoming Service Instance Name is: " + serviceInstanceName, isDebugEnabled)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            utils.log("DEBUG", "Incoming Service Decomposition is: " + serviceDecomposition, isDebugEnabled)
            String subscriberInfo = execution.getVariable("subscriberInfo")
            utils.log("DEBUG", "Incoming Subscriber Information is: " + subscriberInfo, isDebugEnabled)
            Map customerLocation = execution.getVariable("customerLocation")
            utils.log("DEBUG", "Incoming Customer Location is: " + customerLocation.toString(), isDebugEnabled)
            String cloudOwner = execution.getVariable("cloudOwner")
            utils.log("DEBUG", "Incoming cloudOwner is: " + cloudOwner, isDebugEnabled)
            String cloudRegionId = execution.getVariable("cloudRegionId")
            utils.log("DEBUG", "Incoming cloudRegionId is: " + cloudRegionId, isDebugEnabled)

            if (isBlank(requestId) ||
                    isBlank(serviceInstanceId) ||
                    isBlank(serviceInstanceName) ||
                    isBlank(serviceDecomposition.toString()) ||
                    isBlank(customerLocation.toString())) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 4000,
                        "A required input variable is missing or null")
            } else {
                Subscriber subscriber = null
                if (isBlank(subscriberInfo)) {
                    subscriber = new Subscriber("", "", "")
                } else {
                String subId = jsonUtil.getJsonValue(subscriberInfo, "globalSubscriberId")
                String subName = jsonUtil.getJsonValue(subscriberInfo, "subscriberName")
                String subCommonSiteId = ""
                if (jsonUtil.jsonElementExist(subscriberInfo, "subscriberCommonSiteId")) {
                    subCommonSiteId = jsonUtil.getJsonValue(subscriberInfo, "subscriberCommonSiteId")
                }
                    subscriber = new Subscriber(subId, subName, subCommonSiteId)
                }

                //Authentication
                def authHeader = ""
                String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
                String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

                String basicAuthValue = utils.encrypt(basicAuth, msokey)
                if (basicAuthValue != null) {
                    utils.log("DEBUG", "Obtained BasicAuth username and password for OOF Adapter: " + basicAuthValue,
                            isDebugEnabled)
                    try {
                        authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                        execution.setVariable("BasicAuthHeaderValue", authHeader)
                    } catch (Exception ex) {
                        utils.log("DEBUG", "Unable to encode username and password string: " + ex, isDebugEnabled)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to " +
                                "encode username and password string")
                    }
                } else {
                    utils.log("DEBUG", "Unable to obtain BasicAuth - BasicAuth value null", isDebugEnabled)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                            "value null")
                }

                //Prepare Callback
                String timeout = execution.getVariable("timeout")
                if (isBlank(timeout)) {					
                    timeout = UrnPropertiesReader.getVariable("mso.oof.timeout", execution);
                    if (isBlank(timeout)) {
                        timeout = "PT30M"
                    }
                }
                utils.log("DEBUG", "Async Callback Timeout will be: " + timeout, isDebugEnabled)

                execution.setVariable("timeout", timeout)
                execution.setVariable("correlator", requestId)
                execution.setVariable("messageType", "oofResponse")

                //Build Request & Call OOF
                String oofRequest = oofUtils.buildRequest(execution, requestId, serviceDecomposition,
                        subscriber, customerLocation)
                execution.setVariable("oofRequest", oofRequest)
                utils.log("DEBUG", "OOF Request is: " + oofRequest, isDebugEnabled)

                String url = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
                utils.log("DEBUG", "Posting to OOF Url: " + url, isDebugEnabled)

                logDebug("URL to be used is: " + url, isDebugEnabled)

                RESTConfig config = new RESTConfig(url)
                RESTClient client = new RESTClient(config).addAuthorizationHeader(authHeader).
                        addHeader("Content-Type", "application/json")
                APIResponse response = client.httpPost(oofRequest)

                int responseCode = response.getStatusCode()
                logDebug("OOF sync response code is: " + responseCode, isDebugEnabled)
                String syncResponse = response.getResponseBodyAsString()
                execution.setVariable("syncResponse", syncResponse)
                logDebug("OOF sync response is: " + syncResponse, isDebugEnabled)

				if(responseCode != 202){
					exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
				}

                utils.log("DEBUG", "*** Completed Homing Call OOF ***", isDebugEnabled)
            }
        } catch (BpmnError b) {
            throw b
        } catch (Exception e) {
			msoLogger.error(e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                    "Internal Error - Occured in Homing callOof: " + e.getMessage())
        }
    }

    /**
     * This method processes the callback response
     * and the contained homing solution. It sets
     * homing solution assignment and license
     * information to the corresponding resources
     *
     * @param execution
     */
    public void processHomingSolution(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("DEBUG", "*** Started Homing Process Homing Solution ***", isDebugEnabled)
        try {
            String response = execution.getVariable("asyncCallbackResponse")
            utils.log("DEBUG", "OOF Async Callback Response is: " + response, isDebugEnabled)
            utils.logAudit("OOF Async Callback Response is: " + response)

            oofUtils.validateCallbackResponse(execution, response)
            String placements = jsonUtil.getJsonValue(response, "solutions.placementSolutions")
            utils.log("DEBUG", "****** Solution Placements: " + placements + " *****", isDebugEnabled)

            ServiceDecomposition decomposition = execution.getVariable("serviceDecomposition")
            utils.log("DEBUG", "Service Decomposition: " + decomposition, isDebugEnabled)

            List<Resource> resourceList = decomposition.getServiceResources()
            JSONArray arr = new JSONArray(placements)
            for (int i = 0; i < arr.length(); i++) {
                JSONArray arrSol = arr.getJSONArray(i)
                for (int j = 0; j < arrSol.length(); j++) {
                    JSONObject placement = arrSol.getJSONObject(j)
                    utils.log("DEBUG", "****** Placement Solution is: " + placement + " *****", "true")
                    String jsonServiceResourceId = jsonUtil.getJsonValue( placement.toString(), "serviceResourceId")
                    utils.log("DEBUG", "****** homing serviceResourceId is: " + jsonServiceResourceId + " *****", "true")
                    for (Resource resource : resourceList) {
                        String serviceResourceId = resource.getResourceId()
                        utils.log("DEBUG", "****** decomp serviceResourceId is: " + serviceResourceId + " *****", "true")
                        if (serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)) {
                            JSONObject solution = placement.getJSONObject("solution")
                            String solutionType = solution.getString("identifierType")
                            String inventoryType = ""
                            if (solutionType.equalsIgnoreCase("serviceInstanceId")) {
                                inventoryType = "service"
                            } else {
                                inventoryType = "cloud"
                            }
                            utils.log("DEBUG", "****** homing inventoryType is: " + inventoryType + " *****", "true")
                            resource.getHomingSolution().setInventoryType(InventoryType.valueOf(inventoryType))

                            JSONArray assignmentArr = placement.getJSONArray("assignmentInfo")
                            utils.log("DEBUG", "****** assignmentInfo is: " + assignmentArr.toString() + " *****", "true")

                            Map<String, String> assignmentMap = jsonUtil.entryArrayToMap(execution,
                                    assignmentArr.toString(), "key", "value")
                            String oofDirectives = null
                            assignmentMap.each { key, value ->
                                utils.log("DEBUG", "****** element: " + key + " *****", "true")
                                if (key == "oof_directives") {
                                    oofDirectives = value
                                    utils.log("DEBUG", "****** homing oofDirectives: " + oofDirectives + " *****", "true")
                                }
                            }
                            String cloudOwner = assignmentMap.get("cloudOwner")
                            utils.log("DEBUG", "****** homing cloudOwner: " + cloudOwner + " *****", "true")
                            String cloudRegionId = assignmentMap.get("locationId")
                            utils.log("DEBUG", "****** homing cloudRegionId: " + cloudRegionId + " *****", "true")
                            resource.getHomingSolution().setCloudOwner(cloudOwner)
                            resource.getHomingSolution().setCloudRegionId(cloudRegionId)

                            CloudSite cloudSite = new CloudSite()
                            cloudSite.setId(cloudRegionId)
                            cloudSite.setRegionId(cloudRegionId)
                            String orchestrator = execution.getVariable("orchestrator")
                            if ((orchestrator != null) && (orchestrator != "")) {
                                cloudSite.setOrchestrator(orchestrator)
                                utils.log("DEBUG", "****** orchestrator: " + orchestrator + " *****", "true")
                            } else {
                                cloudSite.setOrchestrator("multicloud")
                            }

                            CloudIdentity cloudIdentity = new CloudIdentity()
                            cloudIdentity.setId(cloudRegionId)
                            cloudIdentity.setIdentityServerType(ServerType."KEYSTONE")
                            cloudIdentity.setAdminTenant("service")
                            cloudIdentity.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD)
                            String msoMulticloudUserName = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.username", execution,
                                    "apih")
                            String msoMulticloudPassword = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.password", execution,
                                    "abc123")
                            cloudIdentity.setMsoId(msoMulticloudUserName)
                            cloudIdentity.setMsoPass(msoMulticloudPassword)
                            // Get MSB Url
                            String msbHost = oofUtils.getMsbHost(execution)
                            String multicloudApiEndpoint = UrnPropertiesReader
                                    .getVariable("mso.multicloud.api.endpoint", execution,
                                    "/api/multicloud-titaniumcloud/v1")
                            cloudIdentity.setIdentityUrl(msbHost + multicloudApiEndpoint
                                    + "/" + cloudOwner + "/" +
                                    cloudRegionId + "/infra_workload")
                            utils.log("DEBUG", "****** Cloud IdentityUrl: " + msbHost + multicloudApiEndpoint
                                    + "/" + cloudOwner + "/" +
                                    cloudRegionId + "/infra_workload"
                                    + " *****", "true")
                            utils.log("DEBUG", "****** CloudIdentity: " + cloudIdentity.toString()
                                    + " *****", "true")
                            cloudSite.setIdentityService(cloudIdentity)
                            utils.log("DEBUG", "****** CloudSite: " + cloudSite.toString()
                                    + " *****", "true")

                            // Set cloudsite in catalog DB here
                            oofUtils.createCloudSite(cloudSite, execution)

                            if (oofDirectives != null && oofDirectives != "") {
                                resource.getHomingSolution().setOofDirectives(oofDirectives)
                                execution.setVariable("oofDirectives", oofDirectives)
                                utils.log("DEBUG", "***** OofDirectives set to: " + oofDirectives +
                                        " *****", "true")
                            }

                            // Set Homing Instance
                            String serviceInstanceId = decomposition.getServiceInstance().getInstanceId()
                            HomingInstance homingInstance = new HomingInstance()
                            homingInstance.setServiceInstanceId(serviceInstanceId)
                            homingInstance.setCloudOwner(cloudOwner)
                            homingInstance.setCloudRegionId(cloudRegionId)
                            if (oofDirectives != null && oofDirectives != "") {
                                homingInstance.setOofDirectives(oofDirectives)}
                            else {
                                homingInstance.setOofDirectives("{}")
                            }
                            oofUtils.createHomingInstance(homingInstance, execution)

                            if (inventoryType.equalsIgnoreCase("service")) {
                                resource.getHomingSolution().setRehome(assignmentMap.get("isRehome").toBoolean())
                                VnfResource vnf = new VnfResource()
                                vnf.setVnfHostname(assignmentMap.get("vnfHostName"))
                                resource.getHomingSolution().setVnf(vnf)
                                resource.getHomingSolution().setServiceInstanceId(solution.getJSONArray("identifiers")[0].toString())
                            }
                        } else {
                            utils.log("DEBUG", "ProcessHomingSolution Exception: no matching serviceResourceIds returned in " +
                                    "homing solution", isDebugEnabled)
                            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - " +
                                    "Occurred in Homing ProcessHomingSolution: no matching serviceResourceIds returned")

                        }
                    }
                }
                if (JsonUtils.jsonElementExist(response, "solutions.licenseSolutions")) {
                    String licenseSolutions = jsonUtil.getJsonValue(response, "solutions.licenseSolutions")
                    JSONArray licenseArr = new JSONArray(licenseSolutions)
                    for (int l = 0; l < licenseArr.length(); l++) {
                        JSONObject license = licenseArr.getJSONObject(l)
                        String jsonServiceResourceId = license.getString("serviceResourceId")
                        for (Resource resource : resourceList) {
                            String serviceResourceId = resource.getResourceId()
                            if (serviceResourceId.equalsIgnoreCase(jsonServiceResourceId)) {
                                String jsonEntitlementPoolList = jsonUtil.getJsonValue(license.toString(), "entitlementPoolUUID")
                                List<String> entitlementPoolList = jsonUtil.StringArrayToList(execution, jsonEntitlementPoolList)
                                resource.getHomingSolution().getLicense().setEntitlementPoolList(entitlementPoolList)

                                String jsonLicenseKeyGroupList = jsonUtil.getJsonValue(license.toString(), "licenseKeyGroupUUID")
                                List<String> licenseKeyGroupList = jsonUtil.StringArrayToList(execution, jsonLicenseKeyGroupList)
                                resource.getHomingSolution().getLicense().setLicenseKeyGroupList(licenseKeyGroupList)
                            }
                        }
                    }
                }
            }
            execution.setVariable("serviceDecomposition", decomposition)
            execution.setVariable("homingSolution", placements) //TODO - can be removed as output variable

            utils.log("DEBUG", "*** Completed Homing Process Homing Solution ***", isDebugEnabled)
        } catch (BpmnError b) {
            utils.log("DEBUG", "ProcessHomingSolution Error: " + b, isDebugEnabled)
            throw b
        } catch (Exception e) {
            utils.log("DEBUG", "ProcessHomingSolution Exception: " + e, isDebugEnabled)
			msoLogger.error(e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in Homing ProcessHomingSolution")
        }
    }

    /**
     * This method logs the start of DHVCreateService
     * to make debugging easier.
     *
     * @param - execution
     */
    public String logStart(DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        String requestId = execution.getVariable("testReqId")
        if (isBlank(requestId)) {
            requestId = execution.getVariable("msoRequestId")
        }
        execution.setVariable("DHVCS_requestId", requestId)
        utils.log("DEBUG", "***** STARTED Homing Subflow for request: " + requestId + " *****", "true")
        utils.log("DEBUG", "****** Homing Subflow Global Debug Enabled: " + isDebugEnabled + " *****", "true")
        utils.logAudit("***** STARTED Homing Subflow for request: " + requestId + " *****")
    }

    /**
     * Auto-generated method stub
     */
    public void preProcessRequest(DelegateExecution execution) {}
        // Not Implemented Method

    /**
     * Constructs a workflow message callback URL for the specified message type and correlator.
     * This type of callback URL is used when a workflow wants an MSO adapter (like the SDNC
     * adapter) to call it back.  In other words, this is for callbacks internal to the MSO
     * complex.  Use <code>createWorkflowMessageAdapterCallbackURL</code> if the callback
     * will come from outside the MSO complex.
     * @param endpoint endpoint address to contruct URL from
     * @param messageType the message type (e.g. SDNCAResponse or VNFAResponse)
     * @param correlator the correlator value (e.g. a request ID)
     */
    public String createHomingCallbackURL(String endpoint, String messageType, String correlator) {
        try {
            if (endpoint == null || endpoint.isEmpty()) {
                ExceptionUtil exceptionUtil = new ExceptionUtil()
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
                        'mso:workflow:message:endpoint was not passed in')
            }

            utils.log("DEBUG", "passed in endpoint: " + endpoint + " *****", "true")

            while (endpoint.endsWith('/')) {
                endpoint = endpoint.substring(0, endpoint.length() - 1)
            }
            utils.log("DEBUG", "processed endpoint: " + endpoint + " *****", "true")

            return endpoint +
                    '/' + UriUtils.encodePathSegment(messageType, 'UTF-8') +
                    '/' + UriUtils.encodePathSegment(correlator, 'UTF-8')
        } catch (Exception ex) {
            utils.log("DEBUG", "createCallbackURL Exception: " + ex + " *****", "true")
        }
    }
}
