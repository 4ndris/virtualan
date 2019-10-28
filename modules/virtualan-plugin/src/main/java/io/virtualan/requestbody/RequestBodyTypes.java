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

package io.virtualan.requestbody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is Virtual Service request body types supported.
 * 
 * @author  Elan Thangamani
 * 
 **/
public enum RequestBodyTypes {
    NO_REQUEST_PARAM("NO_REQUEST_PARAM") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) {
            return null;
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) {
            return null;
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) {
            return ((requestBody.getExpectedInput() == null)
                    && (requestBody.getActualInput() == null));
        }
    },
    BOOLEAN("java.lang.Boolean") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) {
            return new Boolean(requestBody.getInputRequest());
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) {
            return "true/false";
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) {
            return Boolean.parseBoolean(
                    requestBody.getExpectedInput()) == ((Boolean) requestBody.getActualInput())
                            .booleanValue();
        }
    },
    STRING("java.lang.String") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) {
            return requestBody.getInputRequest();
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) {
            return "Enter your data";
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) {
            return requestBody.getExpectedInput().equals(requestBody.getActualInput());
        }
    },
    BIGDECIMAL("java.math.BigDecimal") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) {
            return new BigDecimal(requestBody.getInputRequest());
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) {
            return "0.0";
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) {
            return new BigDecimal(requestBody.getExpectedInput())
                    .compareTo((BigDecimal) requestBody.getActualInput()) == 0;
        }
    },
    MAP("java.util.Map") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) throws IOException {
            return requestBody.getObjectMapper().readValue(requestBody.getInputRequest(),
                    HashMap.class);
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) {
            return "{\"additionalProp1\": \"string\",  " + "\"additionalProp2\": \"string\",  "
                    + "\"additionalProp3\": \"string\"}";
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) throws IOException {
            Map expectedMap =
                    new ObjectMapper().readValue(requestBody.getExpectedInput(), HashMap.class);
            for (Map.Entry<String, String> actualMap : ((Map<String, String>) requestBody
                    .getActualInput()).entrySet()) {
                if (requestBody.getExcludeList() == null
                        || !requestBody.getExcludeList().contains(actualMap.getKey())) {
                    if (!actualMap.getValue().equals(expectedMap.get(actualMap.getKey()))) {
                        return false;
                    }
                }
            }
            return true;
        }
    },
    DEFAULT("Default") {
        @Override
        public Object getValidMockRequestBody(RequestBody requestBody) throws IOException {
            return requestBody.getObjectMapper().readValue(requestBody.getInputRequest(),
                    requestBody.getInputObjectType());
        }

        @Override
        public String getDefaultMessageBody(RequestBody requestBody) throws IOException {
            try {
                return requestBody.getObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(requestBody.getInputObjectType().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
            }
            return null;
        }

        @Override
        public boolean compareRequestBody(RequestBody requestBody) throws IOException {
            if (requestBody.getExpectedInput() == null) {
                return true;
            }
            if (requestBody.getInputRequest() != null) {
                return EqualsBuilder.reflectionEquals(
                        requestBody.getObjectMapper().readValue(requestBody.getExpectedInput(),
                                requestBody.getInputObjectType()),
                        requestBody.getObjectMapper().readValue(requestBody.getInputRequest(),
                                requestBody.getInputObjectType()),
                        requestBody.getExcludeList());

            } else {
                return EqualsBuilder.reflectionEquals(
                        requestBody.getObjectMapper().readValue(requestBody.getExpectedInput(),
                                requestBody.getInputObjectType()),
                        requestBody.getActualInput(), requestBody.getExcludeList());

            }
        }
    };



    String type;

    public String getType() {
        return type;
    }

    RequestBodyTypes(String type) {
        this.type = type;
    }

    public abstract Object getValidMockRequestBody(RequestBody requestBody) throws IOException;

    public abstract String getDefaultMessageBody(RequestBody requestBody) throws IOException;

    public abstract boolean compareRequestBody(RequestBody requestBody) throws IOException;

    public static RequestBodyTypes fromString(String requestBodyType) {
        for (RequestBodyTypes currentType : RequestBodyTypes.values()) {
            if (requestBodyType.equals(currentType.getType())) {
                return currentType;
            } else if ("java.util.HashMap".equals(currentType.getType())
                    || "java.util.LinkedHashMap".equals(currentType.getType())) {
                return MAP;
            }
        }
        return DEFAULT;
    }


}
