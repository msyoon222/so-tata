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

package org.openecomp.mso.bpmn.infrastructure.L2PBB.utils;

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

public class RESTCollection{    
   
	
	public static String AAIRESTGET(String uri, String message) throws Exception{
		
		URL uri_address= new URL(uri);
		String response="";
		   try
	        {
	            // Create a trust manager that does not validate certificate chains
	            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	            };

	            // Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance("SSL");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	            // Create all-trusting host name verifier
	            HostnameVerifier allHostsValid = new HostnameVerifier() {
	                public boolean verify(String hostname, SSLSession session) {
	                    return true;
	                }
	            };

	            // Install the all-trusting host verifier
	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		   
	
			   HttpURLConnection connection = (HttpURLConnection) uri_address.openConnection();
			   connection.setRequestMethod("GET");
			   connection.setDoOutput(true);
			   connection.setRequestProperty("X-FromAppId", "Appclient");
			   connection.setRequestProperty("Accept", "application/json");
			   connection.setRequestProperty("Content-type", "application/json");
			   connection.setRequestProperty("Authorization", "Basic QUFJOkFBSQ==");
			   connection.setRequestProperty("X-TransactionId", "application");
             
			   BufferedReader in = new BufferedReader(
     		        new InputStreamReader(connection.getInputStream()));
			   String inputLine;
			   StringBuffer response_SvcName = new StringBuffer();

			   while ((inputLine = in.readLine()) != null) {
				   response_SvcName.append(inputLine);
			   }
			   in.close();
     		
			   response=response_SvcName.toString();

		   
     		return response;
     		  
	}

	public static String AAIRESTPUT(String uri, String message) throws Exception{
		
		URL uri_address= new URL(uri);
		String response="";
		   try
	        {
	            // Create a trust manager that does not validate certificate chains
	            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	            };
	            // Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance("SSL");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	            // Create all-trusting host name verifier
	            HostnameVerifier allHostsValid = new HostnameVerifier() {
	                public boolean verify(String hostname, SSLSession session) {
	                    return true;
	                }
	            };
	            // Install the all-trusting host verifier
	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		   
		try{   
		   HttpURLConnection connection = (HttpURLConnection) uri_address.openConnection();
		   connection.setRequestMethod("PUT");
		   connection.setDoOutput(true);
		   connection.setRequestProperty("X-FromAppId", "Appclient");
		   connection.setRequestProperty("Accept", "application/json");
		   connection.setRequestProperty("Content-type", "application/json");
		   connection.setRequestProperty("Authorization", "Basic QUFJOkFBSQ==");
		   connection.setRequestProperty("X-TransactionId", "application");
             
	        OutputStreamWriter osw_RSL = new OutputStreamWriter(connection.getOutputStream());
	        osw_RSL.write(message);
	        osw_RSL.flush();
	        osw_RSL.close();
	        
	        int connection_response = connection.getResponseCode(); 
     		response=Integer.toString(connection_response);
     		
     		
     		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}   
		return response;
	}
	
	public static String AAIRESTDELETE(String uri, String message) throws Exception{
		
		URL uri_address= new URL(uri);
		String response="";
		   try
	        {
	            // Create a trust manager that does not validate certificate chains
	            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	            };

	            // Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance("SSL");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	            // Create all-trusting host name verifier
	            HostnameVerifier allHostsValid = new HostnameVerifier() {
	                public boolean verify(String hostname, SSLSession session) {
	                    return true;
	                }
	            };

	            // Install the all-trusting host verifier
	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		   
		   try {
			   HttpURLConnection connection = (HttpURLConnection) uri_address.openConnection();
			   connection.setRequestMethod("DELETE");
			   connection.setDoOutput(true);
			   connection.setRequestProperty("X-FromAppId", "Appclient");
			   connection.setRequestProperty("Accept", "application/json");
			   connection.setRequestProperty("Content-type", "application/json");
			   connection.setRequestProperty("Authorization", "Basic QUFJOkFBSQ==");
			   connection.setRequestProperty("X-TransactionId", "application");
             
			   BufferedReader in = new BufferedReader(
     		        new InputStreamReader(connection.getInputStream()));
			   String inputLine;
			   StringBuffer response_SvcName = new StringBuffer();

			   while ((inputLine = in.readLine()) != null) {
				   response_SvcName.append(inputLine);
			   }
			   in.close();
     		
			   response=response_SvcName.toString();
	   		} catch (MalformedURLException e) {
				e.printStackTrace();
				response="";
			} catch (IOException e) {
				e.printStackTrace();
				response="";
			}
		   
     		return response;
     		  
	}
	
	
	public static List SDNCPOST(String uri, String message) throws Exception{
		URL uri_address= new URL(uri);
		List <String> SDNCresult=new ArrayList<>();
		   try
	        {
	            // Create a trust manager that does not validate certificate chains
	            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
	            }
	            };

	            // Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance("SSL");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	            // Create all-trusting host name verifier
	            HostnameVerifier allHostsValid = new HostnameVerifier() {
	                public boolean verify(String hostname, SSLSession session) {
	                    return true;
	                }
	            };

	            // Install the all-trusting host verifier
	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		   
		try {
  		  HttpURLConnection connection_SDNC = (HttpURLConnection) uri_address.openConnection();
  		  connection_SDNC.setRequestMethod("POST");
  		  connection_SDNC.setDoOutput(true);

  		  connection_SDNC.setRequestProperty("Accept", "application/json");
  		  connection_SDNC.setRequestProperty("Content-type", "application/json");
  		  connection_SDNC.setRequestProperty("Authorization", "Basic YWRtaW46S3A4Yko0U1hzek0wV1hsaGFrM2VIbGNzZTJnQXc4NHZhb0dHbUp2VXkyVQ==");
      
            
  		  //OPEN Connection
  		  OutputStreamWriter osw_SDNC = new OutputStreamWriter(connection_SDNC.getOutputStream());
  		  osw_SDNC.write(message);
  		  osw_SDNC.flush();
  		  
            
  		  //Receive Response Body              
  		  String json_response = "";
  		  InputStreamReader in = new InputStreamReader(connection_SDNC.getInputStream());
  		  BufferedReader br = new BufferedReader(in);
  		  String text = "";
  		  
  		  while ((text = br.readLine()) != null) {
  			  json_response += text;
          }
            
  		  //Close Connection
  		  osw_SDNC.close();
            
  		  //Parse response body
  	  	  JSONParser parser = new JSONParser();   		
  	  	  JSONObject SDNC_RESPONSE = (JSONObject)parser.parse(json_response);	 
  	  	  String SDNC_output =SDNC_RESPONSE.get("output").toString();	  
  	  	  JSONObject SDNC_output_json = (JSONObject)parser.parse(SDNC_output);
  	  	  //json response
  	  	  String SDNC_Response =SDNC_output_json.get("response-code").toString();	  
  	  	  String SDNC_Message =SDNC_output_json.get("response-message").toString();	  
          
          SDNCresult.add(SDNC_Response);
          SDNCresult.add(SDNC_Message);
    	  	  
	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return SDNCresult;
	}
	
	
    
}
