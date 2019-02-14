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

package org.openecomp.mso.bpmn.infrastructure.L2PBB.deletion;

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
public class SDNC_Delete implements JavaDelegate {

    private final static Logger log = Logger.getLogger(SDNC_Delete.class.getName());
    
    
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
    	 * read service deletion user parameter 
    	 */
    	String Host1 = execution.getVariable("Host1").toString();
    	String Host1_Port=execution.getVariable("Host1_Port").toString();
    	String Host1_Port_URL=URLEncoder.encode(Host1_Port,"UTF-8");
    	String Host2 = execution.getVariable("Host2").toString();
    	String Host2_Port=execution.getVariable("Host2_Port").toString();
    	String Host2_Port_URL=URLEncoder.encode(Host2_Port,"UTF-8");
    	String policy_name="Policy_In_"+execution.getVariable("serviceInstanceName").toString();
    	String Egress_Outer_VID=execution.getVariable("Egress_Outer_VID").toString();
    	String Ingress_Outer_VID=execution.getVariable("Ingress_Outer_VID").toString();
    	String ISID=Ingress_Outer_VID+Egress_Outer_VID;
    	String Host1_Response = "";
    	String Host2_Response = "";
    	String Host1_Message = "";
    	String Host2_Message = "";    	
    	String SDNC_Success ="Fail";
    	
    	//host1 configuration deletion jsonpayload
        String jsonPayload_Host1 = "{\r\n" + 
        		"\"input\": {\r\n" + 
        		"\"module-name\": \"GENERIC-RESOURCE-API\",\r\n" + 
        		"\"rpc-name\": \"config-asr\",\r\n" + 
        		"\"mode\": \"sync\",\r\n" + 
        		"\"sli-parameter\": [\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-topology-identifier.pnf-type\",\r\n" + 
        		"\"string-value\": \"PNF_ASR\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-action\",\r\n" + 
        		"\"string-value\": \"DELETE\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[0].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"ASR_NAME\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[0].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Host1+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"policy_name\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+policy_name+"\"\r\n" + 
        		"},\r\n" + 
        		"\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"Ingress_Port\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Host1_Port_URL+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"ISID\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+ISID+"\"\r\n" + 
        		"}\r\n" + 
        		"]\r\n" + 
        		"}\r\n" + 
        		"}\r\n" + 
        		"";
        
    	//host2 configuration deletion jsonpayload
        String jsonPayload_Host2 = "{\r\n" + 
        		"\"input\": {\r\n" + 
        		"\"module-name\": \"GENERIC-RESOURCE-API\",\r\n" + 
        		"\"rpc-name\": \"config-asr\",\r\n" + 
        		"\"mode\": \"sync\",\r\n" + 
        		"\"sli-parameter\": [\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-topology-identifier.pnf-type\",\r\n" + 
        		"\"string-value\": \"PNF_ASR\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-action\",\r\n" + 
        		"\"string-value\": \"DELETE\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[0].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"ASR_NAME\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[0].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Host2+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"policy_name\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+policy_name+"\"\r\n" + 
        		"},\r\n" + 
        		"\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"Ingress_Port\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+Host2_Port_URL+"\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        		"\"string-value\": \"ISID\"\r\n" + 
        		"},\r\n" + 
        		"{\r\n" + 
        		"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        		"\"string-value\": \""+ISID+"\"\r\n" + 
        		"}\r\n" + 
        		"]\r\n" + 
        		"}\r\n" + 
        		"}\r\n" + 
        		"";
        
        //SDNC uri
        String uri_SDNC = SDNC_URL+"/restconf/operations/SLI-API:execute-graph";
     	      
        List<String> host1_SDNC_result =new ArrayList<>();
        List<String> host2_SDNC_result =new ArrayList<>();
        
        host1_SDNC_result = restcollection.SDNCPOST(uri_SDNC,jsonPayload_Host1); 
        host2_SDNC_result = restcollection.SDNCPOST(uri_SDNC,jsonPayload_Host2);
        
       Host1_Response = host1_SDNC_result.get(0);
       Host1_Message =  host1_SDNC_result.get(1);
       Host2_Response =  host2_SDNC_result.get(0);
       Host2_Message = host2_SDNC_result.get(1);
       
      //device configuration success check   
      if(Host1_Response.equals("200") && Host2_Response.equals("200") == true)
      {
    	SDNC_Success="Success";
      }
      else {
       	String DeviceConfiguration_response = "{\"Device Configuration Response\":{\""+Host1+"\":\""+Host1_Message+"\"},{\""+Host2+"\":\""+Host2_Message+"\"}}";		
       	execution.setVariable("WorkflowException", DeviceConfiguration_response);
      }
      
      //device configuration result 	
   	  execution.setVariable("SDNC_Success",SDNC_Success);
   	  execution.setVariable("Host1_Response",Host1_Response);
   	  execution.setVariable("Host1_SDNC_Message",Host1_Message);
      execution.setVariable("Host2_Response",Host2_Response);
      execution.setVariable("Host2_SDNC_Message",Host2_Message);    	

    }
}
