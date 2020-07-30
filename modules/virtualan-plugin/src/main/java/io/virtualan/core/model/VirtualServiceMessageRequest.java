/*
 * Copyright 2018 Virtualan Contributors (https://virtualan.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */



package io.virtualan.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Virtual Service Request from external system.
 * 
 * @author  Elan Thangamani
 * 
 **/
public class VirtualServiceMessageRequest {

    private long id;
    private String requestTopicOrQueueName;
    private String httpStatusCode;
    private String brokerUrl;
    private String type;
    private long usageCount;
    private int priority;
    private String responseTopicOrQueueName;
    private Class inputObjectType;
    private String outputObjectType;
    private String input;
    private String rule;
    private String output;
    private List<VirtualServiceKeyValue> availableParams = new ArrayList<>();
    private List<VirtualServiceKeyValue> headerParams  = new ArrayList<>();
    private Map<String, VirtualServiceApiResponse> responseType;
    private String excludeList;
    private String resource;
    private String desc;
    private VirtualServiceStatus mockStatus;
    private java.util.Calendar lastUsedDateTime;

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }

    public List<VirtualServiceKeyValue> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<VirtualServiceKeyValue> headerParams) {
        this.headerParams = headerParams;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Map<String, VirtualServiceApiResponse> getResponseType() {
        return responseType;
    }

    public void setResponseType(Map<String, VirtualServiceApiResponse> responseType) {
        this.responseType = responseType;
    }


    public Object getHeaderParam(String param) {
        return getHeaderParams().stream().filter(x -> x.getKey().equalsIgnoreCase(param)).map(x -> x.getValue());
    }

    public Object getAvailableParam(String param) {
        return getAvailableParams().stream().filter(x -> x.getKey().equalsIgnoreCase(param)).map(x -> x.getValue());
    }

    public String getRequestTopicOrQueueName() {
        return requestTopicOrQueueName;
    }

    public void setRequestTopicOrQueueName(String requestTopicOrQueueName) {
        this.requestTopicOrQueueName = requestTopicOrQueueName;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getResponseTopicOrQueueName() {
        return responseTopicOrQueueName;
    }

    public void setResponseTopicOrQueueName(String responseTopicOrQueueName) {
        this.responseTopicOrQueueName = responseTopicOrQueueName;
    }

    public String groovyTemplateObj() {
        return " def executeScript(mockServiceRequest, responseObject) { \n" +
            "     int age = getAge(mockServiceRequest.getInput().getBirthday()); \n" +
            "    String postalCode = mockServiceRequest.getInput().getPostalCode(); \n" +
            "    int riskFactor = computeRiskFactor(age, postalCode); \n" +
            "    responseObject.setHttpStatusCode('200'); \n" +
            "    responseObject.setOutput(String.valueOf(riskFactor)); \n" +
            "    return responseObject.builder();\n" +
            " }} \n";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(String excludeList) {
        this.excludeList = excludeList;
    }

    private Map<String, String> httpStatusMap;

    public VirtualServiceMessageRequest(long id,  String input, String output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    public Map<String, String> getHttpStatusMap() {
        return httpStatusMap;
    }

    public void setHttpStatusMap(Map<String, String> httpStatusMap) {
        this.httpStatusMap = httpStatusMap;
    }

    public VirtualServiceMessageRequest() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<VirtualServiceKeyValue> getAvailableParams() {
        return availableParams;
    }

    public void setAvailableParams(List<VirtualServiceKeyValue> availableParams) {
        this.availableParams = availableParams;
    }


    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }


    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public Class getInputObjectType() {
        return inputObjectType;
    }

    public void setInputObjectType(Class inputObjectType) {
        this.inputObjectType = inputObjectType;
    }

    public String getOutputObjectType() {
        return outputObjectType;
    }

    public void setOutputObjectType(String outputObjectType) {
        this.outputObjectType = outputObjectType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public VirtualServiceStatus getMockStatus() {
        return mockStatus;
    }

    public void setMockStatus(VirtualServiceStatus mockStatus) {
        this.mockStatus = mockStatus;
    }

	public java.util.Calendar getLastUsedDateTime() {
		return lastUsedDateTime;
	}

	public void setLastUsedDateTime(java.util.Calendar lastUsedDateTime) {
		this.lastUsedDateTime = lastUsedDateTime;
	}

	@Override
	public String toString() {
		return "VirtualServiceRequest [id=" + id + ", requestQueue=" + requestTopicOrQueueName + ", httpStatusCode=" + httpStatusCode
				+ ", brokerurl=" + brokerUrl + ", usageCount=" + usageCount + ", responseQuque=" + responseTopicOrQueueName + ", inputObjectType="
				+ inputObjectType + ", outputObjectType=" + outputObjectType + ", input=" + input + ", output=" + output
				+ ", availableParams=" + availableParams + ", headerParams=" + headerParams + ", responseType="
				+ responseType + ", excludeList=" + excludeList + ", resource=" + resource + ", desc=" + desc
				+ ", mockStatus=" + mockStatus + ", lastUsedDateTime=" + lastUsedDateTime + ", httpStatusMap="
				+ httpStatusMap + "]";
	}

}
