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

package org.openecomp.mso.bpmn.infrastructure.L2PBB.activation;

import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.onap.so.bpmn.core.json.JsonUtils;
import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import org.apache.log4j.*;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import org.json.*;
import org.openecomp.mso.bpmn.infrastructure.L2PBB.utils.RESTCollection;

public class ServiceInstanceCreate implements JavaDelegate {    
   

    private final static Logger logger = Logger.getLogger(AAICreate.class.getName());
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {        	
    	//rest util import
    	RESTCollection restcollection = new RESTCollection();
    	//ONAP Component URL
    	String SDNC_URL = "http://10.0.115.140:30202";
    	String AAI_URL = "https://10.0.115.140:30233";
    	//String SDNC_URL = "http://10.0.115.128:8282";
    	//String AAI_URL = "https://10.0.115.127:8443";
    	
    	/*
    	 * Read service configuration user parameters
    	 */
    	String Host1 = execution.getVariable("Host1").toString();
    	String Host1_Port=execution.getVariable("Host1_Port").toString();
    	String Host2 = execution.getVariable("Host2").toString();
    	String Host2_Port=execution.getVariable("Host2_Port").toString();    	
    	String CIR=execution.getVariable("CIR").toString();
    	String CBS=execution.getVariable("CBS").toString();
    	String policy_name=execution.getVariable("policy_name").toString();
    	String Egress_Outer_VID=execution.getVariable("Egress_Outer_VID").toString();
    	String Ingress_Outer_VID=execution.getVariable("Ingress_Outer_VID").toString();
    	String ISID=execution.getVariable("ISID").toString();
    	String service_instance_name = execution.getVariable("serviceInstanceName").toString();
        String serviceInstanceId = execution.getVariable("serviceInstanceId").toString();
 

        //To be obtained in future from the 
        String BVID = "CBD1506";
        
        String service_instance_url = AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances/service-instance/"+serviceInstanceId;        	
        
    	
        String service_instance_payload = 
"        {\r\n" + 						  
"            \"service-instance-id\": \""+serviceInstanceId+"\",\r\n" + 
"            \"service-instance-name\": \""+service_instance_name+"\",\r\n" + 
"            \"environment-context\": \"General_Revenue-Bearing\",\r\n" + 
"            \"workload-context\": \"Production\",\r\n" + 
"            \"model-invariant-id\": \"ff35cc1c-db52-44de-bf63-db3b137297cc\",\r\n" + 
"            \"model-version-id\": \"021fd4fb-533f-4e1d-a514-73e171c811a2\",\r\n" + 
"      \r\n" + 
"            \"orchestration-status\": \"Active\",\r\n" + 
"            \"relationship-list\": {\r\n" + 
"                \"relationship\": [\r\n" + 
"                    {\r\n" + 
"                        \"related-to\": \"project\",\r\n" + 
"                        \"relationship-label\": \"org.onap.relationships.inventory.Uses\",\r\n" + 
"                        \"related-link\": \"/aai/v14/business/projects/project/Project-Demonstration\",\r\n" + 
"                        \"relationship-data\": [\r\n" + 
"                            {\r\n" + 
"                                \"relationship-key\": \"project.project-name\",\r\n" + 
"                                \"relationship-value\": \"Project-Demonstration\"\r\n" + 
"                            }\r\n" + 
"                        ]\r\n" + 
"                    },\r\n" + 
"                    {\r\n" + 
"                        \"related-to\": \"owning-entity\",\r\n" + 
"                        \"relationship-label\": \"org.onap.relationships.inventory.BelongsTo\",\r\n" + 
"                        \"related-link\": \"/aai/v14/business/owning-entities/owning-entity/7ea2e6ea-d126-4245-8cf7-426b8bde9866\",\r\n" + 
"                        \"relationship-data\": [\r\n" + 
"                            {\r\n" + 
"                                \"relationship-key\": \"owning-entity.owning-entity-id\",\r\n" + 
"                                \"relationship-value\": \"7ea2e6ea-d126-4245-8cf7-426b8bde9866\"\r\n" + 
"                            }\r\n" + 
"                        ]\r\n" + 
"                    }\r\n" + 
"                ]\r\n" + 
"            }\r\n" + 
"        }";
        
        //CREATE Logical-Link Instance
        try{
              
            //RESTCALL AAIPUT     
            String connection_response_LLI = restcollection.AAIRESTPUT(service_instance_url,service_instance_payload);       

        } catch (MalformedURLException e) {
            e.printStackTrace();
            execution.setVariable("WorkflowException","Service Instance Creation Failed (MalformedURL)\n URL: "+service_instance_url+"\n Payload: "+service_instance_payload);
        } catch (IOException e) {
            e.printStackTrace();
            execution.setVariable("WorkflowException","Service Instance Creation Failed (IOException)\n URL: "+service_instance_url+"\n Payload: "+service_instance_payload);
        }                
          
    }
}
