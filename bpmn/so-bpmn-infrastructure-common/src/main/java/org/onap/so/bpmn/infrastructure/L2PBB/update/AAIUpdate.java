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

package org.openecomp.mso.bpmn.infrastructure.L2PBB.update;

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
public class AAIUpdate implements JavaDelegate {    
   

    private final static Logger logger = Logger.getLogger(AAIUpdate.class.getName());
    
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
    	 * read service update user parameter 
    	 */	   	
    	String Host1 = execution.getVariable("Host1").toString();
    	String Host2 = execution.getVariable("Host2").toString();
        String serviceInstanceId =  execution.getVariable("serviceInstanceId").toString();
    	String service_instance_name = execution.getVariable("serviceInstanceName").toString();
    	String CIR =  execution.getVariable("CIR").toString();
    	String logical_link_resource_version =  execution.getVariable("logical_link_resource_version").toString();    	
        String msoRequestId=execution.getVariable("msoRequestId").toString();
        execution.setVariable("msoRequestId",msoRequestId);
        execution.setVariable("serviceInstanceId",serviceInstanceId);
        //
            
        //UPDATE logical-link instance     
        try{
        	
        	String uri_logical_link = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link";        	
            String jsonPayload_logical_link ="{\r\n" + 
            		"    \"link-name\": \""+service_instance_name+"_link\",\r\n" + 
            		"    \"link-type\": \"evc\",\r\n" + 
            		"    \"speed-value\": \""+CIR+"\",\r\n" + 
            		"    \"speed-units\": \"Mbps\",\r\n" + 
            		"    \"resource-version\": \""+logical_link_resource_version+"\"\r\n" + 
            		"\r\n" + 
            		"    }";
           
            String connection_response_LLI = restcollection.AAIRESTPUT(uri_logical_link,jsonPayload_logical_link);           

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }       
    }
}
