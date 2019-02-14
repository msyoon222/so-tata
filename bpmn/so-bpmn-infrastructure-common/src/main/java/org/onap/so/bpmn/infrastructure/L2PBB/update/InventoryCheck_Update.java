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
public class InventoryCheck_Update implements JavaDelegate {    
   
    private final static Logger logger = Logger.getLogger(InventoryCheck_Update.class.getName());
    
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
    	String serviceInstanceName = execution.getVariable("serviceInstanceName").toString();  
        String Available = "";
        String serviceInstanceId = "";
        String logical_link_path ="";
        
        List<String> l_interface_path_list = new ArrayList<String>();
        List<String> PNF_list = new ArrayList<String>();
        List<String> P_interface_list = new ArrayList<String>();
        List<String> L_interface_list = new ArrayList<String>();
        List<String> cvlanId_list = new ArrayList<String>();
        List<String> svlanId_list = new ArrayList<String>();
        String l_interface_1_resource_verrsion = "";
        String l_interface_2_resource_verrsion = "";
        String logical_link_resource_version ="";
        
                
        //Find serviceInstanceID 
        try{    		
        	String uri_SvcName= AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances";
      	  	String jsonPayload_SI = "";
      		
      	  	//Parse JSON Response Host1
      	  	String SI_response = restcollection.AAIRESTGET(uri_SvcName,jsonPayload_SI);
      	  	
      	  	JSONParser parser = new JSONParser(); 		
      	  	JSONObject SVC_obj = (JSONObject)parser.parse(SI_response);   
      	  	
      	  	JSONArray SI_obj_requestList = (JSONArray)parser.parse(SVC_obj.get("service-instance").toString());
      	  	JSONObject SVC_request = new JSONObject();
      	  	for(int i=0;i<SI_obj_requestList.size();i++) {
    			  
      	  		SVC_request = (JSONObject)SI_obj_requestList.get(i); 
      	  		if(SVC_request.get("service-instance-name").equals(serviceInstanceName)) {
      	  			serviceInstanceId = SVC_request.get("service-instance-id").toString();
    				break;
      	  		}
    		  }    		  
      	  	execution.setVariable("serviceInstanceId",serviceInstanceId);    		
    		
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
         
  	  	/*	
		  * Query Service Instance 
		 */
        try{
        	String serviceinstance_URI= AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances/service-instance/"+serviceInstanceId;
  	  		String jsonPayload_service_instance_URI = "";

  	  		String serviceInstance_response = restcollection.AAIRESTGET(serviceinstance_URI,jsonPayload_service_instance_URI);
  	  		JSONParser parser = new JSONParser();   		
  	  		JSONObject serviceInstance_obj = (JSONObject)parser.parse(serviceInstance_response);  
  	  		JSONObject serviceinstance_relationship_list = (JSONObject)parser.parse(serviceInstance_obj.get("relationship-list").toString());      		 
  	  		JSONArray serviceinstance_relationship_array = (JSONArray)serviceinstance_relationship_list.get("relationship");      		    	
  	  		Iterator i = serviceinstance_relationship_array.iterator();
  	  		List<String> serviceinstance_relationship = new ArrayList<String>();
  	  		
  	  		while(i.hasNext()) {
  	  			JSONObject innterobj = (JSONObject)i.next();
  	  			if(innterobj.get("related-to").toString().equals("logical-link")) {
  	  				logical_link_path=innterobj.get("related-link").toString();
  	  			}
  	  		}  	  	  		 
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }     

        /*
  	   * Query Logical-Link Instance 
  	   */
        try{  
        	String logical_link_URI= AAI_URL+""+logical_link_path;	        
  	  		String jsonPayload_logical_link_URI = "";  		
  	  	
  	  		String logical_link_response = restcollection.AAIRESTGET(logical_link_URI,jsonPayload_logical_link_URI);
  	  			
  	  		JSONParser parser = new JSONParser();   		
  	  		JSONObject logical_link_obj = (JSONObject)parser.parse(logical_link_response);  		      		 
  	  		logical_link_resource_version = logical_link_obj.get("resource-version").toString();      		  
  	  		JSONObject logical_link_relationship_list = (JSONObject)parser.parse(logical_link_obj.get("relationship-list").toString());      		 
  	  		JSONArray logical_link_relationship_array = (JSONArray)logical_link_relationship_list.get("relationship");      		    	
  	  		Iterator i = logical_link_relationship_array.iterator();

  	  		while(i.hasNext()) {
  	  			JSONObject innerobj = (JSONObject)i.next();
  	  			if(innerobj.get("related-to").toString().equals("l-interface")) {
  	  				
  	  				l_interface_path_list.add(innerobj.get("related-link").toString());
  	  				
  	  				JSONArray innerarray = (JSONArray)innerobj.get("relationship-data");
        			  
  	  				Iterator j = innerarray.iterator();  
  	  				//Query PNF, P-interface, L-interface data
  	  				while(j.hasNext()) {
  	  					JSONObject innerobj2=(JSONObject)j.next();
  	  					if(innerobj2.get("relationship-key").equals("pnf.pnf-name")) {
  	  						PNF_list.add(innerobj2.get("relationship-value").toString());
  	  						System.out.println("pnf name:"+innerobj2.get("relationship-value"));
  	  					}
  	  					if(innerobj2.get("relationship-key").equals("p-interface.interface-name")) {
  	  						P_interface_list.add(innerobj2.get("relationship-value").toString());
  	  						System.out.println("pnf name:"+innerobj2.get("relationship-value"));
  	  					}
  	  					if(innerobj2.get("relationship-key").equals("l-interface.interface-name")) {
  	  						L_interface_list.add(innerobj2.get("relationship-value").toString());
  	  						System.out.println("pnf name:"+innerobj2.get("relationship-value"));
  	  					}
  	  				}
        			  
  	  			}
      		  }   	      		
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }   
        
        //Update parameters && workflow exception treatment
        String Host1 = PNF_list.get(0);
        String Host2 = PNF_list.get(1);
        String QueryResult="Yes";
        String WorkflowException = null;        
        try { 
        	execution.setVariable("logical_link_path",logical_link_path);
        	execution.setVariable("logical_link_resource_version",logical_link_resource_version);
        }catch(Exception e) {
    		WorkflowException="logical_link information missing";
    		QueryResult="No";
        }
        
        try {
        	execution.setVariable("Host1",Host1);
        }catch(Exception e) {
        	WorkflowException=WorkflowException+'\n'+"Host1 information missing";
    		QueryResult="No";
        }
        try {
        	execution.setVariable("Host2",Host2);
        }catch(Exception e) {
           	WorkflowException=WorkflowException+'\n'+"Host2 information missing";
    		QueryResult="No";
        }        
        
    	execution.setVariable("WorkflowException",WorkflowException);
    	execution.setVariable("QueryResult",QueryResult);
    	
        
    }
}
