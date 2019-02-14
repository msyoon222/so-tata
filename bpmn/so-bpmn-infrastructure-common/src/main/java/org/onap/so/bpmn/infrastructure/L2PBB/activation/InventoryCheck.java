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
 
public class InventoryCheck implements JavaDelegate {    
   
    private final static Logger logger = Logger.getLogger(InventoryCheck.class.getName());
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {        	

        //ONAP Component URL
    	String SDNC_URL = "http://10.0.115.140:30202";
    	String AAI_URL = "https://10.0.115.140:30233";
    	//String SDNC_URL = "http://10.0.115.128:8282";
    	//String AAI_URL = "https://10.0.115.127:8443";
    	
    	//rest util class import
    	RESTCollection restcollection = new RESTCollection();
    	
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
        String Host1_request = Host1_Port+"."+Egress_Outer_VID;
        String Host2_request = Host2_Port+"."+Egress_Outer_VID;
        String Host1_DBMAC = "";
        String Host2_DBMAC = "";
        String Available="unavailable";
        String Available_host1 = "unavailable";
        String Available_host2 = "unavailable";
        String Host1_Inventory_Status="";
        String Host2_Inventory_Status="";
        String Host1_check ="";
        String Host2_check ="";
        
        String Host1_response = "";
        String Host2_response = "";

       //Query physical-interface exist host 1
        String uri_pinterface_host1 = AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host1+"/p-interfaces/p-interface/"+URLEncoder.encode(Host1_Port,"UTF-8");
        String pinterface_exist_h1="";
        try {
        	Host1_response = restcollection.AAIRESTGET(uri_pinterface_host1,"");
        	pinterface_exist_h1="yes";
        }catch(Exception e) {
        	pinterface_exist_h1="no";
        }
        
        //Query Sub-interface Host1	
        if(pinterface_exist_h1.equals("yes")==true){
          try{
    		
        	String uri_Linterface_host1 = AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host1+"/p-interfaces/p-interface/"+URLEncoder.encode(Host1_Port,"UTF-8")+"/l-interfaces";   		
    		String jsonPayload_Linterface_host1 = "";
    		
    		
  		  //Parse JSON Response Host1
  		  //String Host1_response = response.toString();
    	  Host1_response = restcollection.AAIRESTGET(uri_Linterface_host1,jsonPayload_Linterface_host1);
    	  
  		  JSONParser parser = new JSONParser();   		
  		  JSONObject obj = (JSONObject)parser.parse(Host1_response);  		
  		  JSONArray l_interface_array = (JSONArray)obj.get("l-interface");
  		  Iterator i = l_interface_array.iterator();
  		  List<String> l_interface_list_Host1 = new ArrayList<String>();
  		  
  		  while(i.hasNext()) {
  			JSONObject innterobj = (JSONObject)i.next();
  			l_interface_list_Host1.add(innterobj.get("interface-name").toString());
  			//System.out.println("interfaceName:"+innterobj.get("interface-name"));
  		  }   		
   		
  		  //Configuration Availability Check
  		  if(l_interface_list_Host1.contains(Host1_request)==true){
  			  	Available_host1 = "unavailable";
      			Host1_check = "unavailable";
      			Host1_Inventory_Status = "Host1 already has the same sub-interface";
      			execution.setVariable("Host1_check",Host1_check);
      			execution.setVariable("Host2_Inventory_Status",Host1_Inventory_Status);
  		  }
  		  else {
  			Available_host1 = "available";
  			execution.setVariable("Host1_check","available");
  		  }                  
        } catch(Exception e) {
        	Available_host1 = "available";
  			execution.setVariable("Host1_check","available");
       	}
        
       }
       else if (pinterface_exist_h1.equals("no")==true){
    	   Available_host1 = "unavailable";
    	   Host1_check = "unavailable";
    	   Host1_Inventory_Status = "Host1 P-interface does not exist.";
    	   execution.setVariable("Host1_check",Host1_check);
    	   execution.setVariable("Host1_Inventory_Status",Host1_Inventory_Status);
       }
       
       //Query P-interface host 2
       String uri_pinterface_host2 = AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host2+"/p-interfaces/p-interface/"+URLEncoder.encode(Host2_Port,"UTF-8");
       String pinterface_exist_h2="";
       try {
       	Host1_response = restcollection.AAIRESTGET(uri_pinterface_host2,"");
       	pinterface_exist_h2="yes";
       }catch(Exception e) {
       	pinterface_exist_h2="no";
       }
       
       //Query Sub-interface Host2	
      
       if(pinterface_exist_h2.equals("yes")==true){
       
        try{
    		
        	String uri_Linterface_host2 = AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host2+"/p-interfaces/p-interface/"+URLEncoder.encode(Host2_Port,"UTF-8")+"/l-interfaces";        	
    		String jsonPayload_Linterface_host2 = "";
    		
    		    		
  		 //Parse JSON Response Host2
    		Host2_response = restcollection.AAIRESTGET(uri_Linterface_host2,jsonPayload_Linterface_host2);
  		  //String Host2_response = response.toString();  		
  		  JSONParser parser = new JSONParser();   		
  		  JSONObject obj = (JSONObject)parser.parse(Host2_response);
  		  JSONArray l_interface_array = (JSONArray)obj.get("l-interface");
  		  Iterator i = l_interface_array.iterator();
  		  List<String> l_interface_list_Host2 = new ArrayList<String>();
  		  
  		  while(i.hasNext()) {
  			JSONObject innterobj = (JSONObject)i.next();
  			l_interface_list_Host2.add(innterobj.get("interface-name").toString());
  			//System.out.println("interfaceName:"+innterobj.get("interface-name"));
  		  }   		
   		
		  //Configuration Availability Check
  		  if(l_interface_list_Host2.contains(Host2_request)==true){
  			Available_host2 = "unavailable";
  			Host2_check = "unavailable";
  			Host2_Inventory_Status = "Host2 already has the same sub-interface";
  			execution.setVariable("Host2_check",Host2_check);
  			execution.setVariable("Host2_Inventory_Status",Host2_Inventory_Status);
  		  }  	
  		  else {
  			Available_host2 = "available";  
  			execution.setVariable("Host2_check","available");
    	  }
        } 
        catch(Exception e) {
        	Available_host2 = "available";  
  			execution.setVariable("Host2_check","available");
        }
        
       }else if(pinterface_exist_h2.equals("no")==true){
    	   Available_host2 = "unavailable";
    	   Host2_check = "unavailable";
    	   Host2_Inventory_Status = "Host2 P-interface does not exist.";
    	   execution.setVariable("Host1_check",Host2_check);
    	   execution.setVariable("Host1_Inventory_Status",Host2_Inventory_Status); 
       }
        
       
       
      if(Available_host1.equals("available")==true && Available_host2.equals("available")==true) {
    	  Available ="available";
      }
        
	    //Query Host 1 BMAC Address  
	     try{  
	    	String uri_host1= AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host1;
	   		
		  	String jsonPayload_host1 = "";
			
		      //Read Rest Response
		  	String	l_interface_response = restcollection.AAIRESTGET(uri_host1,jsonPayload_host1);
			
		  	JSONParser parser = new JSONParser();   		
	 		JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response);
	 		  
	 		//Assign Host1 BMAC address for Host2_DBMAC address 
	 		Host2_DBMAC =l_interface_obj.get("pnf-name2").toString();
	 		  
	   } catch (MalformedURLException e) {
	   	e.printStackTrace();
	   } catch (IOException e) {
	   	e.printStackTrace();
	   }    
	     
	   //Query Host2 BMAC Address
	     try{  
	    	 String uri_host2= AAI_URL+"/aai/v14/network/pnfs/pnf/"+Host2;
		   	 String jsonPayload_host2 = "";
			
		    //Rest Rest Response	
		  	 String l_interface_response = restcollection.AAIRESTGET(uri_host2,jsonPayload_host2);
		     //String l_interface_response = response.toString();			
		  	 JSONParser parser = new JSONParser();   		
	 		 JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response);
	 		  
	 		//Assign Host2 BMAC address for Host1_DBMAC address 
	 		  Host1_DBMAC =l_interface_obj.get("pnf-name2").toString();
	 		  	 		  
	   } catch (MalformedURLException e) {
	   	e.printStackTrace();
	   } catch (IOException e) {
	   	e.printStackTrace();
	   }  
     //configuration availability result   
      execution.setVariable("Availability_check",Available);
      //DBMAC address result
      execution.setVariable("Host1_DBMAC",Host1_DBMAC);
      execution.setVariable("Host2_DBMAC",Host2_DBMAC);
      
     //workflowexception message creation
      if(Available.equals("unavailable")==true) {
    	 String Availability_response = "{\"Device Check\":{\"Host1\":\""+Host1_check+"\",\"Host1 Status\":\""+Host1_Inventory_Status+"\" ,\"Host2\":\""+Host2_check+"\", \"Host2 Status\":\""+Host2_Inventory_Status+"\" }}";
     	 execution.setVariable("WorkflowException", Availability_response); 
      }

    }
}
