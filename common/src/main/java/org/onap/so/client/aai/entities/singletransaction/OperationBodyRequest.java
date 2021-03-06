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

package org.onap.so.client.aai.entities.singletransaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"action",
"uri",
"body"
})
public class OperationBodyRequest {

@JsonProperty("action")
private String action;
@JsonProperty("uri")
private String uri;
@JsonProperty("body")
@JsonSerialize(using = OperationBodyRequestSerializer.class)
private Object body;


public String getAction() {
	return action;
}

public void setAction(String action) {
	this.action = action;
}

public OperationBodyRequest withAction(String action) {
	this.action = action;
	return this;
}
@JsonProperty("uri")
public String getUri() {
return uri;
}

@JsonProperty("uri")
public void setUri(String uri) {
this.uri = uri;
}

public OperationBodyRequest withUri(String uri) {
this.uri = uri;
return this;
}

@JsonProperty("body")
public Object getBody() {
return body;
}

@JsonProperty("body")
public void setBody(Object body) {
this.body = body;
}

public OperationBodyRequest withBody(Object body) {
this.body = body;
return this;
}

}
