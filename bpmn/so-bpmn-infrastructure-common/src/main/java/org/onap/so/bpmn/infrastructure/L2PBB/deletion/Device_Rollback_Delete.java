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

public class Device_Rollback_Delete implements JavaDelegate {

    private final static Logger log = Logger.getLogger(Device_Rollback_Delete.class.getName());
    
    
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
    	 * read service deletion rollback user parameter 
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
    	String CIR =execution.getVariable("CIR").toString();
    	String CBS =   execution.getVariable("CBS").toString();
    	String Host1_DBMAC="";
    	String Host2_DBMAC="";
    	String ISID=Ingress_Outer_VID+Egress_Outer_VID;
    	String Host1_Response_Message = execution.getVariable("Host1_SDNC_Message").toString();
    	String Host2_Response_Message = execution.getVariable("Host2_SDNC_Message").toString();
    	String Host1_Response_Code =execution.getVariable("Host1_Response").toString();
    	String Host2_Response_Code =    execution.getVariable("Host2_Response").toString();			
    	String Host1_ROLLBACK_Message = "";
    	String Host2_ROLLBACK_Message = "";
    	String Host1_ROLLBACK_Response ="";
    	String Host2_ROLLBACK_Response = "";
    				
    	String jsonPayload_Host1="";
    	String jsonPayload_Host2="";

    	/*
    	 * BMAC Address Qeury host1
    	 */
  	     try{  
  	    	  String Linterface1_URI= AAI_URL+"/aai/v11/network/pnfs/pnf/"+Host1;  	   			
  		  	  String jsonPayload_Linterface1 = "";
  		  		
  		  	  String l_interface_response = restcollection.AAIRESTGET(Linterface1_URI,jsonPayload_Linterface1);
  		   
  		  	  JSONParser parser = new JSONParser();   		
  	 		  JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response);  	 		  
  	 		  Host2_DBMAC =l_interface_obj.get("pnf-name2").toString();

  	   } catch (MalformedURLException e) {
  	   	e.printStackTrace();
  	   } catch (IOException e) {
  	   	e.printStackTrace();
  	   }    

     	/*
     	 * BMAC Address Qeury host2
     	 */
  	     try{  
  	    	 	String Linterface1_URI= AAI_URL+"/aai/v11/network/pnfs/pnf/"+Host2;	  	   
  		  		String jsonPayload_Linterface1 = "";
  			
  		  	  String l_interface_response =  restcollection.AAIRESTGET(Linterface1_URI,jsonPayload_Linterface1);
  		     
  		  	  JSONParser parser = new JSONParser();   		
  	 		  JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response);  	 		  
  	 		  Host1_DBMAC =l_interface_obj.get("pnf-name2").toString();

  	   } catch (MalformedURLException e) {
  	   	e.printStackTrace();
  	   } catch (IOException e) {
  	   	e.printStackTrace();
  	   }  
  	     
  	   /*
  	    * Device rollback jsonpayload depending on SDNC response message
  	    */
  	   
    	//when policymap configuration success, interface fail
    	if(Host1_Response_Message.equals("Edge-Bridge Delete Fail")==true && Host1_Response_Code.equals("200")== false) {
    		
       		jsonPayload_Host1="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   
    		
    	}
  	     else if(Host1_Response_Message.equals("Interface Delete Fail")==true && Host1_Response_Code.equals("200")== false) {
    		
       		jsonPayload_Host1="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   
    		
    	}
    	//when policymap, interface configuration success, bridge fail
    	else if(Host1_Response_Message.equals("Policy-map Delete Fail")==true  && Host1_Response_Code.equals("200")== false) {
       		jsonPayload_Host1="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1q\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Ingress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Tagging\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"push\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1ad\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   
    		
    	}
    	
    	//when policymap, interface, bridge configuration success, service instance fail
    	else if(Host1_Response_Code.equals("200")== true)
    	{
    		jsonPayload_Host1="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"cir\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+CIR+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"cbs\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+CBS+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"policy_name\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+policy_name+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1q\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Ingress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Tagging\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"push\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1ad\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[12].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[12].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[13].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[13].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host1_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   		
    	}
    	else {
    		jsonPayload_Host1="";
    	}
    	
    	//when policymap configuration success, interface fail
    	if(Host2_Response_Message.equals("Edge-Bridge Delete Fail")==true && Host2_Response_Code.equals("200")== false) {
    		
       		jsonPayload_Host2="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   
    		
    	}
    	else if(Host2_Response_Message.equals("Interface Delete Fail")==true && Host2_Response_Code.equals("200")== false) {
    		
       		jsonPayload_Host2="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   
    		
    	}
    	//when policymap, interface configuration success, bridge fail
    	else if(Host2_Response_Message.equals("Policy-map Delete Fail")==true  && Host2_Response_Code.equals("200")== false) {
       		jsonPayload_Host2="{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1q\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Ingress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Tagging\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"push\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1ad\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";       		
    	}
    	
    	//when policymap, interface, bridge configuration success, service instance fail
    	else if(Host2_Response_Code.equals("200")== true)
    			{
    		jsonPayload_Host2= "{\r\n" + 
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
        			"\"string-value\": \"ROLLBACK_DELETE\"\r\n"+ 
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
        			"\"string-value\": \"cir\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[1].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+CIR+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"cbs\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[2].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+CBS+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"policy_name\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[3].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+policy_name+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[4].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1q\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[5].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Ingress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Tagging\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[6].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"push\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_Ethertype\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[7].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"dot1ad\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Egress_Outer_VID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[8].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Egress_Outer_VID+"\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"Ingress_Port\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[9].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_Port+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"ISID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[10].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+ISID+"\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"BVID\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[11].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"1506\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[12].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"description\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[12].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \"TATA ONAP Testing\"\r\n" + 
        			"},\r\n" + 
        			"\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[13].pnf-parameter-name\",\r\n" + 
        			"\"string-value\": \"DBMAC\"\r\n" + 
        			"},\r\n" + 
        			"{\r\n" + 
        			"\"parameter-name\": \"preload-data.pnf-topology-information.pnf-parameters[13].pnf-parameter-value\",\r\n" + 
        			"\"string-value\": \""+Host2_DBMAC+"\"\r\n" + 
        			"}\r\n" + 
        			"]\r\n" + 
        			"}\r\n" + 
        			"}\r\n" + 
        			"";   		
    	} 	
    	else {
    		jsonPayload_Host2="";
    	}
    	String uri_SDNC = SDNC_URL+"/restconf/operations/SLI-API:execute-graph";       
    	
        List<String> host1_rollback_result =new ArrayList<>();
        List<String> host2_rollback_result =new ArrayList<>();
        
        host1_rollback_result = restcollection.SDNCPOST(uri_SDNC,jsonPayload_Host1); 
        host2_rollback_result = restcollection.SDNCPOST(uri_SDNC,jsonPayload_Host2);   

    }
}
