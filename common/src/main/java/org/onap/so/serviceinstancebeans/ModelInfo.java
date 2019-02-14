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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.03.30 at 02:48:23 PM CDT
//


package org.onap.so.serviceinstancebeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "modelInfo")
@JsonInclude(Include.NON_DEFAULT)
public class ModelInfo implements Serializable {

	private static final long serialVersionUID = 5281763573935476852L;
	@JsonProperty("modelCustomizationName")
    protected String modelCustomizationName;
	@JsonProperty("modelInvariantId")
    protected String modelInvariantId;
	@JsonProperty("modelType")
	protected ModelType modelType;
	@JsonProperty("modelId")
	protected String modelId;
	//v2
	@JsonProperty("modelNameVersionId")
    protected String modelNameVersionId;
	@JsonProperty("modelName")
    protected String modelName;
	@JsonProperty("modelVersion")
    protected String modelVersion;
	@JsonProperty("modelCustomizationUuid")
    protected String modelCustomizationUuid;
    //v3
	@JsonProperty("modelVersionId")
    protected String modelVersionId;
	@JsonProperty("modelCustomizationId")
    protected String modelCustomizationId;
	
    //Decomposition fields
	@JsonProperty("modelUuid")
    protected String modelUuid;
	@JsonProperty("modelInvariantUuid")
	protected String modelInvariantUuid;
	@JsonProperty("modelInstanceName")
    protected String modelInstanceName;

	public String getModelCustomizationName() {
		return modelCustomizationName;
	}
	public void setModelCustomizationName(String modelCustomizationName) {
		modelInstanceName = modelCustomizationName;
		this.modelCustomizationName = modelCustomizationName;
	}
	public String getModelNameVersionId() {
		return modelNameVersionId;
	}
	public void setModelNameVersionId(String modelNameVersionId) {
		this.modelNameVersionId = modelNameVersionId;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public ModelType getModelType() {
		return modelType;
	}
	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}
	public String getModelInvariantId() {
		return modelInvariantId;
	}
	public void setModelInvariantId(String modelInvariantId) {
		this.modelInvariantUuid = modelInvariantId;
		this.modelInvariantId = modelInvariantId;
	}
	public String getModelCustomizationUuid() {
		return modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public String getModelVersionId() {
		return modelVersionId;
	}
	public void setModelVersionId(String modelVersionId) {
		this.modelUuid=modelVersionId;
		this.modelVersionId = modelVersionId;
	}
	public String getModelCustomizationId() {
		return modelCustomizationId;
	}
	public void setModelCustomizationId(String modelCustomizationId) {
		this.modelCustomizationUuid = modelCustomizationId;
		this.modelCustomizationId = modelCustomizationId;
	}
    public String getModelUuid() {
		return modelUuid;
	}
    public String getModelId() {
    	return modelId;
    }
	public void setModelUuid(String modelUuid) {
		this.modelId = modelUuid;
		this.modelUuid = modelUuid;
		
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	public String getModelInvariantUuid() {
		return modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}
	public String getModelInstanceName() {
		return modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}
	@Override
	public String toString() {
		return "ModelInfo [modelCustomizationName=" + modelCustomizationName + ", modelInvariantId=" + modelInvariantId
				+ ", modelType=" + modelType + ", modelNameVersionId=" + modelNameVersionId + ", modelName=" + modelName
				+ ", modelVersion=" + modelVersion + ", modelCustomizationUuid=" + modelCustomizationUuid
				+ ", modelVersionId=" + modelVersionId + ", modelCustomizationId=" + modelCustomizationId
				+ ", modelUuid=" + modelUuid + ", modelInvariantUuid=" + modelInvariantUuid + ", modelInstanceName="
				+ modelInstanceName + "]";
	}


}