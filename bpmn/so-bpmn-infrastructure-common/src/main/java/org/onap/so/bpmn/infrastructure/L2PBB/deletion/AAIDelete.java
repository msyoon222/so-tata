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
import org.openecomp.mso.bpmn.infrastructure.L2PBB.utils.RESTCollection;

public class AAIDelete implements JavaDelegate {    
   

    private final static Logger logger = Logger.getLogger(AAIDelete.class.getName());
    
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
    	String Host2 = execution.getVariable("Host2").toString();
    	String Host2_Port=execution.getVariable("Host2_Port").toString();
        String serviceInstanceId =  execution.getVariable("serviceInstanceId").toString();
        String logical_link_path = execution.getVariable("logical_link_path").toString();
        String l_interface1_path = execution.getVariable("l_interface1_path").toString();
        String l_interface2_path = execution.getVariable("l_interface2_path").toString();
        String l_interface_1_resource_verrsion = execution.getVariable("l_interface_1_resource_verrsion").toString();
        String l_interface_2_resource_verrsion = execution.getVariable("l_interface_2_resource_verrsion").toString();
    	String service_instance_name = execution.getVariable("serviceInstanceName").toString();
    	String serviceinstance_resource_version = execution.getVariable("serviceinstance_resource_version").toString();
        //
        String msoRequestId=execution.getVariable("msoRequestId").toString();
        execution.setVariable("msoRequestId",msoRequestId);
        execution.setVariable("serviceInstanceId",serviceInstanceId);
             
        
        //Delete Service Instance
        try{ 
        	String uri_serviceinstance= AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances/service-instance/"+serviceInstanceId+"?resource-version="+serviceinstance_resource_version;
  	  		String jsonPayload_service_instance = "";

  	  		String l_interface_response = restcollection.AAIRESTDELETE(uri_serviceinstance,jsonPayload_service_instance);  		  	 	  		
    		  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
      	execution.setVariable("WorkflowException", "Service Instance Deletion Fail");
      } catch (IOException e) {
      	e.printStackTrace();
      	execution.setVariable("WorkflowException", "Service Instance Deletion Fail");
      }  
      
        
        //Delete Logical-Interface 1
        try{ 
        	String uri_Linterface_host1= AAI_URL+""+l_interface1_path+"?resource-version="+l_interface_1_resource_verrsion;      		
  	  		String jsonPayload_Linterface_host1 = "";

  	  		String l_interface_response = restcollection.AAIRESTDELETE(uri_Linterface_host1,jsonPayload_Linterface_host1);  		  	 	  		
    		  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
      	execution.setVariable("WorkflowException", "Logical_interface 1 Deletion Fail");
      } catch (IOException e) {
      	e.printStackTrace();
     	execution.setVariable("WorkflowException", "Logical_interface 1 Deletion Fail");
      }   
        
        //Delete Logical-Interface 2
        try{  
        	String uri_Linterface_host2= AAI_URL+""+l_interface2_path+"?resource-version="+l_interface_2_resource_verrsion;      		
  	  		String jsonPayload_Linterface_host2 = "";  		
  	  	
  	  		String l2_interface_response = restcollection.AAIRESTDELETE(uri_Linterface_host2,jsonPayload_Linterface_host2);
  	  		  	  	
	  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
     	execution.setVariable("WorkflowException", "Logical_interface 2 Deletion Fail");
      } catch (IOException e) {
      	e.printStackTrace();
     	execution.setVariable("WorkflowException", "Logical_interface 2 Deletion Fail");
      }   
    	

    }
}
