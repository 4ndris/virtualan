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

package io.virtualan.custom.message;

import java.io.Serializable;

import javax.ws.rs.core.Response;

import org.springframework.http.ResponseEntity;

/**
 * This is ResponseException.
 * 
 * @author  Elan Thangamani
 * 
 **/
public class ResponseException extends RuntimeException implements Serializable {

    public ResponseException() {
        super();
    }

    Response response;

    ResponseEntity responseEntity;

    private static final long serialVersionUID = 1169426381288170661L;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }

}
