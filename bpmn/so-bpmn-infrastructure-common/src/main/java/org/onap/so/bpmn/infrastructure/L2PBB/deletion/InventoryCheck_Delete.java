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

public class InventoryCheck_Delete implements JavaDelegate {    
   
    private final static Logger logger = Logger.getLogger(InventoryCheck_Delete.class.getName());
    
    @Override
    public void execute(DelegateExecution execution) throws Exception { 
    	//rest util import
    	RESTCollection restcollection = new RESTCollection();
        //ONAP Component URL
    	String SDNC_URL = "http://10.0.115.140:30202";
    	String AAI_URL = "https://10.0.115.140:30233";
    	//String SDNC_URL = "http://10.0.115.128:8282";
    	//String AAI_URL = "https://10.0.115.127:8443";
    	
    	//read serviceInstanceName from user parameter
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
        String serviceinstance_resource_version ="";
        String CIR="";
        String CBS="";
        
              
        //Find serviceInstanceID 
        try{
        	
        	String uri_SvcId= AAI_URL+"/aai/v14/business/customers/customer/TESTCUSTOMER/service-subscriptions/service-subscription/vIMS/service-instances";           
      	  	String jsonPayload_SI = "";     		    		  
      	  	
      	  	//Parse JSON Response Host1
      	  	String SI_response = restcollection.AAIRESTGET(uri_SvcId,jsonPayload_SI);      	  
    		  
      	  	JSONParser parser = new JSONParser(); 		
      	  	JSONObject SVC_obj = (JSONObject)parser.parse(SI_response);   
    	
      	  	JSONArray SI_obj_requestList = (JSONArray)parser.parse(SVC_obj.get("service-instance").toString());
      	  	System.out.println("SI_obj_requestList:"+SI_obj_requestList+"\n");  
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
  	  		serviceinstance_resource_version = serviceInstance_obj.get("resource-version").toString();
  	  		
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
  	    *Query Logical-Link Instance 
  	   */
        try{  
        	String logical_link_URI= AAI_URL+""+logical_link_path;
  	  		String jsonPayload_logical_link_URI = "";
  
  	  		String logical_link_response = restcollection.AAIRESTGET(logical_link_URI,jsonPayload_logical_link_URI);  	  	
  	  	
  	  		JSONParser parser = new JSONParser();   		
  	  		JSONObject logical_link_obj = (JSONObject)parser.parse(logical_link_response);  		
  	  		CIR = logical_link_obj.get("speed-value").toString();      		        		  
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
        				  }
        				  if(innerobj2.get("relationship-key").equals("p-interface.interface-name")) {
        					  P_interface_list.add(innerobj2.get("relationship-value").toString());        					  
        				  }
        				  if(innerobj2.get("relationship-key").equals("l-interface.interface-name")) {
        					  L_interface_list.add(innerobj2.get("relationship-value").toString());        					  
        				  }
        			  }
        			  
      			  }
      		  }      		
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }   
        
      /*
  	   * Query L-Interface 1 resource-version
  	   */
        try{  
        	String Linterface1_URI=AAI_URL+""+l_interface_path_list.get(0).toString();
  	  		String jsonPayload_Linterface1 = "";  		
  	  	
  	  		String l_interface_response =restcollection.AAIRESTGET(Linterface1_URI,jsonPayload_Linterface1);
  	     
  	  		JSONParser parser = new JSONParser();   		
  	  		JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response);	 
  	  		l_interface_1_resource_verrsion =l_interface_obj.get("resource-version").toString();	  
    		  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
      } catch (IOException e) {
      	e.printStackTrace();
      }   
       
      /*
  	   * Query L-Interface 2 resource-version
  	   */
        try{  
        	String Linterface2_URI=AAI_URL+""+l_interface_path_list.get(1).toString();
  	  		String jsonPayload_Linterface2 = "";
  		
  	  		String l_interface_response =restcollection.AAIRESTGET(Linterface2_URI,jsonPayload_Linterface2);  	    	
  	  	  	JSONParser parser = new JSONParser();   		
  	  	  	JSONObject l_interface_obj = (JSONObject)parser.parse(l_interface_response); 
  	  	  	l_interface_2_resource_verrsion =l_interface_obj.get("resource-version").toString();
	  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
      } catch (IOException e) {
      	e.printStackTrace();
      }   
        
   	  /*
  	   * Query L-Interface 1 vlan
  	   */         
        try{  
        	String uri_vlan1= AAI_URL+""+l_interface_path_list.get(0).toString()+"/vlans";      		
  	  		String jsonPayload_vlan1 = "";
  	  		
  	  		String l_interface_vlan_response =restcollection.AAIRESTGET(uri_vlan1,jsonPayload_vlan1);
  	     
  	  		JSONParser parser = new JSONParser();   		
  	  		JSONObject vlan_obj = (JSONObject)parser.parse(l_interface_vlan_response);    		  
  	  		JSONArray vlan_array = (JSONArray)vlan_obj.get("vlan");	    	
  	  		Iterator i = vlan_array.iterator();
  	  		List<String> vlan_arrays = new ArrayList<String>();
  	  		String svlanId = "";
  	  		String cvlanId = "";
  		  
  	  		while(i.hasNext()) {
    			  JSONObject innterobj = (JSONObject)i.next();    			    	
    			  cvlanId_list.add(innterobj.get("vlan-id-inner").toString());
    			  svlanId_list.add(innterobj.get("vlan-id-outer").toString());    					
  	  		}	  
      } catch (MalformedURLException e) {
      	e.printStackTrace();
      } catch (IOException e) {
      	e.printStackTrace();
      }   
        
   	  /*
       * Query L-Interface 2 vlan
       */           
          try{  
        	  	String uri_vlan2 = AAI_URL+""+l_interface_path_list.get(1).toString()+"/vlans";        	
    	  		String jsonPayload_vlan2 = "";
    		
    	  		String l_interface_vlan_response =restcollection.AAIRESTGET(uri_vlan2,jsonPayload_vlan2);
    	
    	  		JSONParser parser = new JSONParser();   		
    	  		JSONObject vlan_obj = (JSONObject)parser.parse(l_interface_vlan_response);      		  
    	  		JSONArray vlan_array = (JSONArray)vlan_obj.get("vlan");    	    	
    	  		Iterator i = vlan_array.iterator();
    	  		List<String> vlan_arrays = new ArrayList<String>();
    	  		String svlanId = "";
    	  		String cvlanId = "";
    		  
    	  		while(i.hasNext()) {
      			  JSONObject innterobj = (JSONObject)i.next();      			    	
      			  cvlanId_list.add(innterobj.get("vlan-id-inner").toString());
      			  svlanId_list.add(innterobj.get("vlan-id-outer").toString());
 	
      		  }   		  
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }   
          
         /*
          * A&AI information query results
          */        
        String l_interface1_path = l_interface_path_list.get(0);
        String l_interface2_path = l_interface_path_list.get(1);
        String Host1 = PNF_list.get(0);
        String Host2 = PNF_list.get(1);
        String Host1_Port = P_interface_list.get(0);
        String Host2_Port = P_interface_list.get(1);         
    	String Ingress_Outer_VID=cvlanId_list.get(0);
    	String Egress_Outer_VID=svlanId_list.get(0);    	
    	String WorkflowException=null;
    	String QueryResult="Yes";
    	
    	//workloadexception error handling &message creation
    	try {
    		execution.setVariable("logical_link_path",logical_link_path);
    	}catch(Exception e) {
    		WorkflowException="A&AI Query Result:"+'\n'+"logical_link information missing";
    		QueryResult="No";
    	}
    	try {
    		execution.setVariable("l_interface1_path",l_interface1_path);
    		execution.setVariable("l_interface_1_resource_verrsion",l_interface_1_resource_verrsion);
    	}
    	catch(Exception e) {
    		WorkflowException=WorkflowException+'\n'+"l_interface1 information missing";
    		QueryResult="No";
    	}
    	try {
            execution.setVariable("l_interface2_path",l_interface2_path);            
            execution.setVariable("l_interface_2_resource_verrsion",l_interface_2_resource_verrsion);	
    	}catch(Exception e) {
     		WorkflowException=WorkflowException+'\n'+"l_interface2 information missing";
    		QueryResult="No";
    	}
    	try {
    		execution.setVariable("Host1",Host1);
    		execution.setVariable("Host1_Port",Host1_Port);
    	}catch(Exception e) {
    		WorkflowException=WorkflowException+'\n'+"Host1 information missing";
    		QueryResult="No";
    	}
    	try {
    		execution.setVariable("Host2",Host2);
    		execution.setVariable("Host2_Port",Host2_Port);
    	}catch(Exception e) {
    		WorkflowException=WorkflowException+'\n'+"Host2 information missing";
    		QueryResult="No";
    	}
    	try {
    		execution.setVariable("Ingress_Outer_VID",Ingress_Outer_VID);
            execution.setVariable("Egress_Outer_VID",Egress_Outer_VID);	
    	}catch(Exception e) {
    		WorkflowException=WorkflowException+'\n'+"VLAN information missing";
    		QueryResult="No";
    	} 
    	
    	try {
    		execution.setVariable("serviceinstance_resource_version",serviceinstance_resource_version);
            
    	}catch(Exception e) {
    		WorkflowException=WorkflowException+'\n'+"Service Instance Resource Version information missing";
    		QueryResult="No";
    	} 
    	
		int tempCBS= Integer.parseInt(CIR)*1000;
  		CBS=Integer.toString(tempCBS);
  	
    	execution.setVariable("CIR",CIR);
    	execution.setVariable("CBS",CBS);	
    	
    	execution.setVariable("WorkflowException",WorkflowException);
    	execution.setVariable("QueryResult",QueryResult);    	
    }
}
