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

public class AAICreate implements JavaDelegate {    
   

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
        
        
        //CREATE Logical-Link Instance
        try{
        	
        	String uri_logical_link = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link";        	
            String jsonPayload_logical_link ="{\"link-name\":\""+service_instance_name+"_link\",\"in-maint\":\"false\",\"link-type\":\"evc\",\"speed-value\":\""+CIR+"\",\"speed-units\":\"Mbps\"}";
        	
            //RESTCALL AAIPUT
            String connection_response_LLI = restcollection.AAIRESTPUT(uri_logical_link,jsonPayload_logical_link);
            

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }                
        
        //Create Logical-Interface Host1
        try{
        
        String uri_logical_Logical_if_host1 = AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host1+"/p-interfaces/p-interface/"+URLEncoder.encode(Host1_Port,"UTF-8")+"/l-interfaces/l-interface/"+URLEncoder.encode(Host1_Port,"UTF-8")+"."+Egress_Outer_VID;        	
        String jsonPayload_logical_if_host1 =         "        {\r\n" + 
                "        	\"interface-name\" : \""+Host1_Port+"."+Egress_Outer_VID+"\",\r\n" + 
                "        	\"in-maint\" : \"false\",\r\n" + 
                "        	\"is-port-mirrored\" : \"false\",\r\n" + 
                "        	\"is-ip-unnumbered\" : \"false\",\r\n" + 
                "        	\"interface-role\" : \"sub-interface\",\r\n" + 
                "        	\r\n" + 
                "        	\"vlans\":{\r\n" + 
                "        			\"vlan\":[\r\n" + 
                "        			{\r\n" + 
                "                    	\"vlan-interface\": \"EBD1506"+Egress_Outer_VID+"\",\r\n" + 
                "                    	\"vlan-id-inner\": "+Ingress_Outer_VID+",\r\n" + 
          		"                       \"vlan-id-outer\" : \""+Egress_Outer_VID+"\",\r\n" + 
                "                    	\"vlan-description\": \"Edge-Bridges\",\r\n" + 
                "                    	\"in-maint\": false,\r\n" + 
                "                    	\"is-ip-unnumbered\": false\r\n" + 
                "                \r\n" + 
                "        			}	\r\n" + 
                "        			]\r\n" + 
                "        	}\r\n" + 
                "        }";  
        
        	String connection_response_LI1 = restcollection.AAIRESTPUT(uri_logical_Logical_if_host1,jsonPayload_logical_if_host1);        
        	
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }       
            
        
        //Create Logical-Interface Host2        
        try{
       	String uri_logical_Logical_if_host2 =AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host2+"/p-interfaces/p-interface/"+URLEncoder.encode(Host2_Port,"UTF-8")+"/l-interfaces/l-interface/"+URLEncoder.encode(Host2_Port,"UTF-8")+"."+Egress_Outer_VID;        	
        String jsonPayload_logical_if_host2 =         "        {\r\n" + 
                "        	\"interface-name\" : \""+Host2_Port+"."+Egress_Outer_VID+"\",\r\n" + 
                "        	\"in-maint\" : \"false\",\r\n" + 
                "        	\"is-port-mirrored\" : \"false\",\r\n" + 
                "        	\"is-ip-unnumbered\" : \"false\",\r\n" + 
                "        	\"interface-role\" : \"sub-interface\",\r\n" + 
                "        	\r\n" + 
                "        	\"vlans\":{\r\n" + 
                "        			\"vlan\":[\r\n" + 
                "        			{\r\n" + 
                "                    	\"vlan-interface\": \"EBD1506"+Egress_Outer_VID+"\",\r\n" + 
                "                    	\"vlan-id-inner\": "+Ingress_Outer_VID+",\r\n" + 
          		"                       \"vlan-id-outer\" : \""+Egress_Outer_VID+"\",\r\n" + 
                "                    	\"vlan-description\": \"Edge-Bridges\",\r\n" + 
                "                    	\"in-maint\": false,\r\n" + 
                "                    	\"is-ip-unnumbered\": false\r\n" + 
                "                \r\n" + 
                "        			}	\r\n" + 
                "        			]\r\n" + 
                "        	}\r\n" + 
                "        }";   

    	String connection_response_LI2 = restcollection.AAIRESTPUT(uri_logical_Logical_if_host2,jsonPayload_logical_if_host2);        

    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }       
                   
      //Wait until service instance is created
        String serviceInstance_response="";
        Thread.sleep(2000);
        int j=100;
      //Query if the service instance is created
        while(j>0) {        
          try{
        	String uri_Svc= AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances";            
            String jsonPayload_SI = "";      		
          
    		  //Parse JSON Response Host1
            	String SI_response = restcollection.AAIRESTGET(uri_Svc,jsonPayload_SI);           	

    		  JSONParser parser = new JSONParser(); 		
    		  JSONObject SVC_obj = (JSONObject)parser.parse(SI_response);   
    	
    		  JSONArray SI_obj_requestList = (JSONArray)parser.parse(SVC_obj.get("service-instance").toString());
    		  System.out.println("SI_obj_requestList:"+SI_obj_requestList+"\n");  
    		  JSONObject SVC_request = new JSONObject();
    		  for(int i=0;i<SI_obj_requestList.size();i++) {
    			  
    			  SVC_request = (JSONObject)SI_obj_requestList.get(i); 
    			  if(SVC_request.get("service-instance-name").equals(service_instance_name)) {
    				  serviceInstance_response = SVC_request.get("service-instance-id").toString();
    				break;
    			  }
    		  }    		
    		  if(serviceInstance_response.equals(serviceInstanceId)) {
    			  break;
    		  }
    		  
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
    		j=j-1;
       } 
        
        //Create Relate Service Instance and Logical-Link
        try{
        String uri_service_relationship = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link/relationship-list/relationship";       
  		String jsonPayload_service_relationship = "{\r\n" + 
  				"        \"related-to\":\"service-instance\",\r\n" + 
  				"        \"related-link\" : \"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances/service-instance/"+serviceInstance_response+"\"\r\n"+
  				"       }";
  	    
  		
  		String connection_response_RSL = restcollection.AAIRESTPUT(uri_service_relationship,jsonPayload_service_relationship);
        
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
                 
        //Create Relationship Logical-Link and Logical-Interface Host1        
        try{    		
        	String uri_link_relationship_host1 = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link/relationship-list/relationship";        	
      		String jsonPayload_link_relationship_host1 = "       {\r\n" + 
      				"        \"related-to\":\"l-interface\",\r\n" + 
      				"        \"related-link\" : \"/aai/v14/network/pnfs/pnf/"+Host1+"/p-interfaces/p-interface/"+URLEncoder.encode(Host1_Port,"UTF-8")+"/l-interfaces/l-interface/"+URLEncoder.encode(Host1_Port,"UTF-8")+"."+Egress_Outer_VID+"\"}";
      		
      		String connection_response_RLL1 = restcollection.AAIRESTPUT(uri_link_relationship_host1,jsonPayload_link_relationship_host1);
      		
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
                
        //Create Relate Logical-Link and Logical-Interface Host2        
        try{
    		String uri_link_relationship_host2 = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link/relationship-list/relationship";      		
      		String jsonPayload_link_relationship_host2 = "       {\r\n" + 
      				"        \"related-to\":\"l-interface\",\r\n" + 
      				"        \"related-link\" : \"/aai/v11/network/pnfs/pnf/"+Host2+"/p-interfaces/p-interface/"+URLEncoder.encode(Host2_Port,"UTF-8")+"/l-interfaces/l-interface/"+URLEncoder.encode(Host2_Port,"UTF-8")+"."+Egress_Outer_VID+"\"}";
 
      		String connection_response_RLL1 = restcollection.AAIRESTPUT(uri_link_relationship_host2,jsonPayload_link_relationship_host2);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
        
        //Create realationship logical-link and BVID logical-link
        try{
    		String uri_logical_link = AAI_URL+"/aai/v14/network/logical-links/logical-link/"+service_instance_name+"_link/relationship-list/relationship";      		
      		String jsonPayload = "       {\r\n" + 
      				"        \"related-to\":\"logical-link\",\r\n" + 
      				"        \"related-link\" : \"aai/v11/network/logical-links/logical-link/"+BVID+"\"}";
 
      		String connection_response_RLL1 = restcollection.AAIRESTPUT(uri_logical_link,jsonPayload);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
        
    }
}
