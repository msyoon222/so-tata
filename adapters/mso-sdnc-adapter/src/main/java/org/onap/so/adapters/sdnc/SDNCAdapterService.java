/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.sdnc;


import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;

/**
 * This class was generated by Apache CXF 2.7.11.redhat-3
 * 2015-01-27T18:25:50.994-05:00
 * Generated source version: 2.7.11.redhat-3
 *
 */
//BPEL SDNCAdapter SOAP WebService
@WebServiceClient(name = "SDNCAdapterService",
                  wsdlLocation = "main/resources/SDNCAdapter.wsdl",
                  targetNamespace = "http://org.onap/workflow/sdnc/adapter/wsdl/v1")
public class SDNCAdapterService extends Service {

	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, SDNCAdapterService.class);
    private static final String SDNC_ADAPTER_WSDL="SDNCAdapter.wsdl";
	
    public static final URL WSDL_LOCATION;

    public static final QName SERVICE = new QName("http://org.onap/workflow/sdnc/adapter/wsdl/v1", "SDNCAdapterService");
    public static final QName SDNCAdapterSoapHttpPort = new QName("http://org.onap/workflow/sdnc/adapter/wsdl/v1", "SDNCAdapterSoapHttpPort");
    static {
        URL wsdlUrl = null;
        try {
        	wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("main/resources/SDNCAdapter.wsdl");
        } catch (Exception e) {
            logger.error(MessageEnum.RA_WSDL_NOT_FOUND, SDNC_ADAPTER_WSDL, "", "", MsoLogger.ErrorCode.DataError, "Exception - WSDL not found", e);
        }
        if(wsdlUrl == null) {
        	logger.error(MessageEnum.RA_WSDL_NOT_FOUND, SDNC_ADAPTER_WSDL, "", "", MsoLogger.ErrorCode.DataError, "WSDL not found");
    	} else {
    		try {
				logger.info(MessageEnum.RA_PRINT_URL, SDNC_ADAPTER_WSDL, wsdlUrl.toURI().toString(), "");
			} catch (Exception e) {
				logger.error(MessageEnum.RA_WSDL_URL_CONVENTION_EXC, SDNC_ADAPTER_WSDL, "", "", MsoLogger.ErrorCode.DataError, "Exception - print URL", e);
			}
    	}
        WSDL_LOCATION = wsdlUrl;
    }

    public SDNCAdapterService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public SDNCAdapterService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SDNCAdapterService() {
        super(WSDL_LOCATION, SERVICE);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCAdapterService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCAdapterService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCAdapterService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns SDNCAdapterPortType
     */
    @WebEndpoint(name = "SDNCAdapterSoapHttpPort")
    public SDNCAdapterPortType getSDNCAdapterSoapHttpPort() {
        return super.getPort(SDNCAdapterSoapHttpPort, SDNCAdapterPortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SDNCAdapterPortType
     */
    @WebEndpoint(name = "SDNCAdapterSoapHttpPort")
    public SDNCAdapterPortType getSDNCAdapterSoapHttpPort(WebServiceFeature... features) {
        return super.getPort(SDNCAdapterSoapHttpPort, SDNCAdapterPortType.class, features);
    }
}
