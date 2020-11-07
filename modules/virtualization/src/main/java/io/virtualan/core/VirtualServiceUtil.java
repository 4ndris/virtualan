/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
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

package io.virtualan.core;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.virtualan.api.ApiType;
import io.virtualan.api.VirtualServiceType;
import io.virtualan.api.WSResource;
import io.virtualan.core.model.ContentType;
import io.virtualan.core.model.MockRequest;
import io.virtualan.core.model.MockResponse;
import io.virtualan.core.model.MockServiceRequest;
import io.virtualan.core.model.ResponseParam;
import io.virtualan.core.model.ResponseProcessType;
import io.virtualan.core.model.VirtualServiceKeyValue;
import io.virtualan.core.model.VirtualServiceRequest;
import io.virtualan.core.model.VirtualServiceStatus;
import io.virtualan.core.soap.SoapFaultException;
import io.virtualan.core.util.BestMatchComparator;
import io.virtualan.core.util.Converter;
import io.virtualan.core.util.ReturnMockResponse;
import io.virtualan.core.util.ScriptErrorException;
import io.virtualan.core.util.VirtualServiceParamComparator;
import io.virtualan.core.util.VirtualServiceValidRequest;
import io.virtualan.core.util.VirtualXPaths;
import io.virtualan.core.util.XMLConverter;
import io.virtualan.core.util.rule.ScriptExecutor;
import io.virtualan.custom.message.ResponseException;
import io.virtualan.mapson.Mapson;
import io.virtualan.params.Param;
import io.virtualan.params.ParamTypes;
import io.virtualan.requestbody.RequestBody;
import io.virtualan.requestbody.RequestBodyTypes;
import io.virtualan.service.VirtualService;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * This class is base utility service class to perform all virtual service operations
 *
 * @author Elan Thangamani
 */

@Service("virtualServiceUtil")
@Slf4j
public class VirtualServiceUtil {

  private final Locale locale = LocaleContextHolder.getLocale();
  @Autowired
  private VirtualServiceValidRequest virtualServiceValidRequest;
  @Autowired
  private VirtualService virtualService;
  @Autowired
  private ScriptExecutor scriptExecutor;
  @Autowired
  private Converter converter;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private VirtualServiceParamComparator virtualServiceParamComparator;

  @Autowired
  private XMLConverter xmlConverter;

  @Autowired
  private ObjectMapper objectMapper;

  private VirtualServiceType virtualServiceType;
  @Autowired
  private VirtualServiceInfoFactory virtualServiceInfoFactory;
  private VirtualServiceInfo virtualServiceInfo;

  public static Object getActualValue(Object object, Map<String, Object> contextObject) {
    String key = object.toString();
    if (key.indexOf('<') != -1) {
      String idkey = key.substring(key.indexOf('<') + 1, key.indexOf('>'));
      if (contextObject.containsKey(idkey)) {
        return key.replaceAll(key.substring(key.indexOf('<'), key.indexOf('>') + 1),
            contextObject.get(idkey).toString());
      }
    }
    return object;
  }

  public static void populateMapParams(Map<String, String> paramMap,
      Map<String, Object> contextObject) {
    for (Map.Entry<String, String> param : paramMap.entrySet()) {
      paramMap
          .put(param.getKey(), getActualValueForAll(getDelimiter(ContentType.JSON) ,param.getValue(), contextObject).toString());
    }
  }

  public static Object getActualValueForAll(Map.Entry<String, String> delimiter,Object object, Map<String, Object> contextObject) {
    String key = object.toString();
    if (key.indexOf(delimiter.getKey()) != -1 && key.indexOf(delimiter.getValue()) != -1) {
      String idkey = key.substring(key.indexOf(delimiter.getKey()) + 1, key.indexOf(delimiter.getValue()));
      if (contextObject.containsKey(idkey)) {
        String prefix = delimiter.getKey().contains("{") ? "\\"  : "";
        String replaceValue = key.replaceAll(prefix+key.substring(key.indexOf(delimiter.getKey()), key.indexOf(delimiter.getValue()) + 1),
            contextObject.get(idkey).toString());
        if (replaceValue.indexOf(delimiter.getKey()) != -1 && replaceValue.indexOf(delimiter.getValue()) != -1) {
          return getActualValueForAll(delimiter, replaceValue, contextObject);
        }
        return replaceValue;
      } else {
        log.error("id key :" + idkey);
      }
    }
    return object;
  }

  public VirtualServiceType getVirtualServiceType() {
    return virtualServiceType;
  }

  public void setVirtualServiceType(VirtualServiceType virtualServiceType) {
    if (virtualServiceType != null) {
      setVirtualServiceInfo(
          virtualServiceInfoFactory.getVirtualServiceInfo(virtualServiceType.getType()));
      this.virtualServiceType = virtualServiceType;
    }

  }

  @PostConstruct
  @Order(1)
  public void init() throws ClassNotFoundException, JsonProcessingException,
      InstantiationException, IllegalAccessException {
    setVirtualServiceType(ApiType.findApiType());
    if (getVirtualServiceType() != null) {
      virtualServiceInfo = getVirtualServiceInfo();
      virtualServiceInfo.loadVirtualServices();
      virtualServiceInfo.setResourceParent(virtualServiceInfo.loadMapper());
    } else if (getVirtualServiceType() == null) {
      setVirtualServiceType(VirtualServiceType.NON_REST);
      virtualServiceInfo = getVirtualServiceInfo();
    }
  }

  public VirtualServiceInfo getVirtualServiceInfo() {
    return virtualServiceInfo;
  }

  private void setVirtualServiceInfo(VirtualServiceInfo virtualServiceInfo) {
    this.virtualServiceInfo = virtualServiceInfo;
  }

  private ObjectMapper getObjectMapper() {
    objectMapper.findAndRegisterModules();
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    return objectMapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
        // ,DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
    );
  }

  public Map<String, String> getHttpStatusMap() {
    final Map<String, String> map = new LinkedHashMap<>();
    for (final HttpStatus status : HttpStatus.values()) {
      map.put(String.valueOf(status.value()), status.name());
    }
    return map;
  }

  public Map<MockRequest, MockResponse> readDynamicResponse(String resource, String operationId) {
    final Map<MockRequest, MockResponse> mockResponseMap = new HashMap<>();
    try {
      final List<VirtualServiceRequest> mockTransferObjectList =
          virtualService.readByOperationId(resource, operationId);
      for (final VirtualServiceRequest mockTransferObject : mockTransferObjectList) {
        final String input =
            mockTransferObject.getInput() != null ? mockTransferObject.getInput().toString() : null;
        final String output =
            mockTransferObject.getOutput() != null ? mockTransferObject.getOutput().toString()
                : null;

        Set<String> excludeSet = null;
        if (mockTransferObject.getExcludeList() != null) {
          excludeSet = new HashSet<>(
              Arrays.asList(mockTransferObject.getExcludeList().split(",")));
        }
        final MockRequest mockRequest = new MockRequest();
        mockRequest.setVirtualServiceId(mockTransferObject.getId());
        mockRequest.setUsageCount(mockTransferObject.getUsageCount());
        mockRequest.setInput(input);
        mockRequest.setContentType(mockTransferObject.getContentType());
        mockRequest.setRule(mockTransferObject.getRule());
        mockRequest.setType(mockTransferObject.getType());
        mockRequest.setExcludeSet(excludeSet);
        mockRequest.setAvailableParams(mockTransferObject.getAvailableParams());
        mockRequest.setMethod(mockTransferObject.getMethod());
        if (mockTransferObject.getType() != null && !mockTransferObject.getType().isEmpty()) {
          mockRequest.setResponseProcessType(
              ResponseProcessType.valueOf(mockTransferObject.getType().toUpperCase()));
        }
        final MockResponse mockResponse =
            new MockResponse(output, mockTransferObject.getHttpStatusCode());
        mockResponse.setHeaderParams(mockTransferObject.getHeaderParams());
        mockResponseMap.put(mockRequest, mockResponse);
      }
    } catch (final Exception e) {
      VirtualServiceUtil.log.error("Rest Mock API Response for " + operationId
          + " has not loaded : " + e.getMessage());
    }
    return mockResponseMap;
  }

  public void findOperationIdForService(VirtualServiceRequest mockLoadRequest) {
    if (mockLoadRequest.getOperationId() == null && virtualServiceInfo != null) {
      final String resourceUrl = mockLoadRequest.getUrl().substring(1);
      final List<String> resouceSplitterList =
          new LinkedList<>(Arrays.asList(resourceUrl.split("/")));
      if (!resouceSplitterList.isEmpty()) {
        final String operationId =
            virtualServiceInfo.getOperationId(mockLoadRequest.getMethod(),
                virtualServiceInfo.getResourceParent(), resouceSplitterList);
        mockLoadRequest.setOperationId(operationId);
        mockLoadRequest.setResource(resouceSplitterList.get(0));
      }
    }
  }

  public ResponseEntity<VirtualServiceStatus> checkIfServiceDataAlreadyExists(
      VirtualServiceRequest virtualServiceRequest) throws IOException, JAXBException {
    final Object response = isMockAlreadyExists(virtualServiceRequest);
    if (response instanceof Long) {
      final ResponseEntity<VirtualServiceStatus> virtualServiceStatus = getVirtualServiceStatusResponseEntity(
          virtualServiceRequest, (Long) response);
      if (virtualServiceStatus != null) {
        return virtualServiceStatus;
      }
    }
    return null;
  }

  public ResponseEntity<VirtualServiceStatus> getVirtualServiceStatusResponseEntity(
      VirtualServiceRequest virtualServiceRequest, Long response) {
    if (response != 0) {
      final VirtualServiceStatus virtualServiceStatus = new VirtualServiceStatus(
          messageSource.getMessage("VS_DATA_ALREADY_EXISTS", null, locale));
      virtualServiceRequest.setId(response);
      virtualServiceRequest = Converter.convertAsJson(virtualServiceRequest);
      virtualServiceStatus.setVirtualServiceRequest(virtualServiceRequest);
      return new ResponseEntity<>(virtualServiceStatus,
          HttpStatus.BAD_REQUEST);
    }
    return null;
  }

  public Object isMockAlreadyExists(VirtualServiceRequest mockTransferObject)
      throws IOException, JAXBException {

    try {
      final Map<MockRequest, MockResponse> mockDataSetupMap = readDynamicResponse(
          mockTransferObject.getResource(), mockTransferObject.getOperationId());
      MockServiceRequest mockServiceRequest = buildMockServiceRequest(mockTransferObject);

      //validate if it is a valid script
      if (mockServiceRequest.getRule() != null) {
        scriptExecutor.executeScript(mockServiceRequest, new MockResponse(),
            mockServiceRequest.getRule().toString());
      }

      if (mockServiceRequest.getInputObjectType() != null
          && mockTransferObject.getInput() != null) {
        mockServiceRequest.setInput(getObjectMapper()
            .readValue(mockTransferObject.getInput().toString(),
                mockServiceRequest.getInputObjectType()));
      } else if (mockTransferObject.getInput() != null) {
        mockServiceRequest.setInput(mockTransferObject.getInput().toString());
      }

      final Map<Integer, ReturnMockResponse> returnMockResponseMap =
          isResponseExists(mockDataSetupMap, mockServiceRequest);

      if (returnMockResponseMap.size() > 0) {
        return isResposeExists(mockTransferObject, mockServiceRequest.getInputObjectType(),
            mockServiceRequest,
            returnMockResponseMap);
      }


    } catch (final Exception e) {
      VirtualServiceUtil.log.error("isMockAlreadyExists :: " + e.getMessage());
      throw e;
    }
    return null;
  }

  private long isResposeExists(VirtualServiceRequest mockTransferObject,
      final Class inputObjectType, final MockServiceRequest mockServiceRequest,
      final Map<Integer, ReturnMockResponse> returnMockResponseMap)
      throws IOException, JAXBException {
    final List<ReturnMockResponse> returnMockResponseList =
        new ArrayList<>(returnMockResponseMap.values());
    Collections.sort(returnMockResponseList, new BestMatchComparator());
    VirtualServiceUtil.log.debug("Sorted list : " + returnMockResponseList);
    final ReturnMockResponse rMockResponse = returnMockResponseList.iterator().next();
    if (rMockResponse != null && rMockResponse.getHeaderResponse() != null) {
      final RequestBody requestBody = buildRequestBody(mockTransferObject, inputObjectType,
          rMockResponse);
      boolean isBodyMatch = false;
      if (inputObjectType != null) {
        isBodyMatch = RequestBodyTypes.fromString(inputObjectType.getTypeName())
            .compareRequestBody(requestBody);
      }
      return checkExistsInEachCatagory(mockTransferObject, mockServiceRequest, rMockResponse,
          isBodyMatch);
    }
    return 0;
  }

  private RequestBody buildRequestBody(VirtualServiceRequest mockTransferObject,
      final Class inputObjectType, final ReturnMockResponse rMockResponse) {
    final RequestBody requestBody = new RequestBody();
    requestBody.setObjectMapper(getObjectMapper());
    requestBody.setExcludeList(rMockResponse.getMockRequest().getExcludeSet());
    requestBody.setExpectedInput(rMockResponse.getMockRequest().getInput());
    requestBody.setInputObjectType(inputObjectType);
    requestBody.setInputRequest(
        mockTransferObject.getInput() != null ? mockTransferObject.getInput().toString() : null);
    requestBody.setContentType(rMockResponse.getMockRequest().getContentType());
    return requestBody;
  }

  private long checkExistsInEachCatagory(VirtualServiceRequest mockTransferObject,
      final MockServiceRequest mockServiceRequest, final ReturnMockResponse rMockResponse,
      boolean isBodyMatch) {
    if (mockServiceRequest.getParams() == null || mockServiceRequest.getParams().isEmpty()) {
      return checkExistsInEachCatagoryForNoParam(mockServiceRequest, rMockResponse);
    } else if (mockServiceRequest.getParams() != null
        && mockServiceRequest.getParams().size() > 0
        && mockTransferObject.getInput() != null) {
      if (rMockResponse.getMockRequest().getAvailableParams().size() == mockServiceRequest
          .getParams().size() && isBodyMatch) {
        return checkExistsInEachCatagoryForNoParam(mockServiceRequest, rMockResponse);
      }
    } else if (mockServiceRequest.getParams() != null
        && mockServiceRequest.getParams().size() > 0) {
      return checkExistsInEachCatagoryForParam(mockServiceRequest, rMockResponse);
    } else if (mockTransferObject.getInput() != null && isBodyMatch) {
      return rMockResponse.getMockRequest().getVirtualServiceId();
    }
    return 0;
  }

  private long checkExistsInEachCatagoryForParam(final MockServiceRequest mockServiceRequest,
      final ReturnMockResponse rMockResponse) {
    if (rMockResponse.getMockRequest().getAvailableParams().size() == mockServiceRequest
        .getParams().size()
        && virtualServiceParamComparator.isAllParamPresent(mockServiceRequest,
        rMockResponse)) {
      return rMockResponse.getMockRequest().getVirtualServiceId();
    }
    return 0;
  }

  private long checkExistsInEachCatagoryForNoParam(final MockServiceRequest mockServiceRequest,
      final ReturnMockResponse rMockResponse) {
    if (virtualServiceParamComparator.isAllParamPresent(mockServiceRequest, rMockResponse)) {
      return rMockResponse.getMockRequest().getVirtualServiceId();
    }
    return 0;
  }

  // REDO - Is this validation needed?
  public boolean isMockResponseBodyValid(VirtualServiceRequest mockTransferObject)
      throws InvalidMockResponseException {
    try {
      if(ContentType.XML.equals(mockTransferObject.getContentType())) {
        XMLConverter.xmlToObject(mockTransferObject.getResponseObjectType(),mockTransferObject.getOutput().toString());
      } else {
        VirtualServiceRequest
          mockTransferObjectActual = virtualServiceInfo.getResponseType(mockTransferObject);
        if (mockTransferObjectActual != null && mockTransferObjectActual.getResponseType() != null
            &&
            !mockTransferObjectActual.getResponseType().isEmpty()) {
          virtualServiceValidRequest
              .validResponse(mockTransferObjectActual, mockTransferObject);
        }
      }
    } catch (final Exception e) {
      throw new InvalidMockResponseException(e);
    }
    return true;
  }

  public Map<Integer, ReturnMockResponse> validateBusinessRules(
      final Map<MockRequest, MockResponse> mockDataSetupMap,
      MockServiceRequest mockServiceRequest) {
    return virtualServiceValidRequest.validBusinessRuleForInputObject(mockDataSetupMap,
        mockServiceRequest);
  }

  public Map<Integer, ReturnMockResponse> isResponseExists(
      final Map<MockRequest, MockResponse> mockDataSetupMap,
      MockServiceRequest mockServiceRequest) throws IOException, JAXBException {

    if ((mockServiceRequest.getParams() == null || !mockServiceRequest.getParams().isEmpty())
        && mockServiceRequest.getInput() == null) {
      return virtualServiceValidRequest.validForNoParam(mockDataSetupMap,
          mockServiceRequest);
    } else if (ResponseProcessType.RULE.name().equalsIgnoreCase(mockServiceRequest.getType()) ||
        ((ResponseProcessType.RESPONSE.name().equalsIgnoreCase(mockServiceRequest.getType())
            || mockServiceRequest.getType() == null) &&
            (mockServiceRequest.getParams() == null || mockServiceRequest.getParams().isEmpty())
            && mockServiceRequest.getInput() != null)) {
      return virtualServiceValidRequest.validForInputObject(mockDataSetupMap,
          mockServiceRequest);
    } else if (mockServiceRequest.getType() == null
        || ResponseProcessType.RESPONSE.name().equalsIgnoreCase(mockServiceRequest.getType())
        && mockServiceRequest.getParams() != null
        && mockServiceRequest.getParams().size() > 0) {
      return virtualServiceValidRequest.validForParam(mockDataSetupMap,
          mockServiceRequest);
    }
    return null;
  }

  private MockServiceRequest buildMockServiceRequest(VirtualServiceRequest mockTransferObject) {

    MockServiceRequest mockServiceRequest = new MockServiceRequest();

    if(mockTransferObject.getInputObjectType() == null){
      Class inputObjectType = getVirtualServiceInfo().getInputType(mockTransferObject);
      mockServiceRequest.setInputObjectType(inputObjectType);
    } else {
      mockServiceRequest.setInputObjectType(mockTransferObject.getInputObjectType());
    }
    mockServiceRequest.setResponseObjectType(mockTransferObject.getResponseObjectType());
    mockServiceRequest
        .setHeaderParams(Converter.converter(mockTransferObject.getHeaderParams()));
    mockServiceRequest.setOperationId(mockTransferObject.getOperationId());
    mockServiceRequest.setContentType(mockTransferObject.getContentType());
    mockServiceRequest.setType(mockTransferObject.getType());
    mockServiceRequest.setRule(mockTransferObject.getRule());
    mockServiceRequest
        .setParams(Converter.converter(mockTransferObject.getAvailableParams()));
    mockServiceRequest.setResource(mockTransferObject.getResource());
    mockServiceRequest.setInput(mockTransferObject.getInput());
    mockServiceRequest.setOutput(mockTransferObject.getOutput());
    return mockServiceRequest;
  }

  public Map<Integer, ResponseParam> handleParameterizedRequest(
      VirtualServiceRequest mockTransferObject) {
    MockServiceRequest mockServiceRequest = buildMockServiceRequest(mockTransferObject);
    final Map<MockRequest, MockResponse> mockDataSetupMap = readDynamicResponse(
        mockServiceRequest.getResource(), mockServiceRequest.getOperationId());
    return handleParameterizedRequest(
        mockDataSetupMap, mockServiceRequest);
  }

  private Map<Integer, ReturnMockResponse> getParameterizedResponse(
      Map<MockRequest, MockResponse> mockDataSetupMap,
      MockServiceRequest mockServiceRequest) {
    Map<Integer, ReturnMockResponse> matchResponse = null;
    for (Map.Entry<MockRequest, MockResponse> entry : mockDataSetupMap.entrySet()) {
      Map map = processParamComparison(entry, mockServiceRequest);
      if (map != null) {
        return map;
      }
    }
    return matchResponse;
  }

  private Map processParamComparison(Entry<MockRequest, MockResponse> entry,
      MockServiceRequest mockServiceRequest) {
    if (ResponseProcessType.PARAMS.name().equalsIgnoreCase(entry.getKey().getType())) {
      JSONArray paramArray = new JSONArray(entry.getKey().getRule());
      for (int i = 0; i < paramArray.length(); i++) {
        try {
          HashMap context =
              new ObjectMapper().readValue(paramArray.getJSONObject(i).toString(), HashMap.class);
          if (isAnyMatchPresent(mockServiceRequest, entry, context)) {
            return getMatchingResponse(mockServiceRequest, entry, context);
          }
        } catch (Exception e) {
          log.warn(" {}", e.getMessage());
        }
      }
    }
    return null;
  }

  private boolean isAnyMatchPresent(MockServiceRequest mockServiceRequest,
      Entry<MockRequest, MockResponse> entry, HashMap context) throws JsonProcessingException {
    if (mockServiceRequest.getParams() != null &&
        mockServiceRequest.getInput() == null &&
        matchParameters(mockServiceRequest, entry, context)) {
      return true;
    }
    return performParameterized(mockServiceRequest, entry, context);
  }

  private boolean performParameterized(MockServiceRequest mockServiceRequest,
      Entry<MockRequest, MockResponse> entry, HashMap context) throws JsonProcessingException {
    if ((mockServiceRequest.getParameters() != null && !mockServiceRequest.getParameters()
        .isEmpty()) && mockServiceRequest.getInput() == null &&
        matchParameters(mockServiceRequest, entry, context)) {
      return true;
    } else if (mockServiceRequest.getParameters() != null && mockServiceRequest.getInput() != null
        &&
        (matchParameters(mockServiceRequest, entry, context) && getMatchByInput(mockServiceRequest,
            entry, context))) {
      return true;
    } else if (
        (mockServiceRequest.getParameters() == null || mockServiceRequest.getParameters().isEmpty())
            && mockServiceRequest.getInput() != null && getMatchByInput(mockServiceRequest, entry,
            context)) {
      return true;
    }
    return false;
  }

  private static SimpleEntry<String, String> getDelimiter(ContentType contentType) {
    String start_param = "<";
    String end_param = ">";
    if (ContentType.XML.equals(contentType)) {
      start_param = "{";
      end_param = "}";
    }
    return new SimpleEntry<>(start_param, end_param);
  }

  private boolean getMatchByInput(MockServiceRequest mockServiceRequest,
      Entry<MockRequest, MockResponse> entry, Map<String, Object> context)
      throws JsonProcessingException {
    Map<String, String> requestObjectMap = null;
    Map<String, String> requestActualValueParam = null;
    Map<String, String> replacedValueMap = null;
    Map.Entry<String, String> delimiter = getDelimiter(entry.getKey().getContentType());
    if(ContentType.XML.equals(entry.getKey().getContentType())) {
      List<String> existingPaths = VirtualXPaths.readXPaths(entry.getKey().getInput());
      List<String> input =  null;
      if(mockServiceRequest.getInput().toString().contains("</")) {
          input = VirtualXPaths.readXPaths(mockServiceRequest.getInput().toString());
      } else {
          input = VirtualXPaths.readXPaths(XMLConverter.objectToXML(
              mockServiceRequest.getInputObjectType(), mockServiceRequest.getInput()));
        }
      List<String> filterList =  existingPaths.stream().filter(
          x -> x.contains("{") && x
              .contains("}")).map(x -> x.substring(x.lastIndexOf(':'), x.lastIndexOf('='))).collect(Collectors.toList());
      List<String> filteredListWithValue = existingPaths.stream()
          .filter(  x -> x.contains("{") && x.contains("}")).
              map(x -> x.substring(x.lastIndexOf(':'))).map(x ->
              getActualValueForAll(delimiter,x, context).toString()).collect(Collectors.toList());

      List<String> matches = input.stream().filter(x -> x.lastIndexOf(':') < x.lastIndexOf('=')).filter(
                x -> filterList.contains(x.substring(x.lastIndexOf(':'), x.lastIndexOf('='))))
                .map(x -> x.substring(x.lastIndexOf(':'))).collect(
                  Collectors.toList());

      return  matches.containsAll(filteredListWithValue);
    } else {
      requestObjectMap = Mapson.buildMAPsonFromJson(entry.getKey().getInput());
      if (isJSONValid(mockServiceRequest.getInput().toString())) {
        requestActualValueParam = Mapson
            .buildMAPsonFromJson(mockServiceRequest.getInput().toString());
      } else {
        requestActualValueParam = Mapson
            .buildMAPsonFromJson(
                getObjectMapper().writeValueAsString(mockServiceRequest.getInput()));
      }
      replacedValueMap = Mapson
          .buildMAPsonFromJson(getActualValueForAll(delimiter, entry.getKey().getInput(), context).toString());
      List<String> parameters = requestObjectMap.entrySet().stream().filter(
          x -> x.getValue().startsWith(delimiter.getKey()) && x.getValue()
              .endsWith(delimiter.getValue()))
          .map(Entry::getKey).collect(Collectors.toList());
      Map<String, String> matches = replacedValueMap.entrySet().stream().filter(
          x -> parameters.contains(x.getKey())).collect(
          Collectors.toMap(Entry::getKey, Entry::getValue));
      return requestActualValueParam.entrySet().containsAll(matches.entrySet());
    }
  }

  private boolean isJSONValid(String test) {
    try {
      new JSONObject(test);
    } catch (JSONException ex) {
      try {
        new JSONArray(test);
      } catch (JSONException ex1) {
        return false;
      }
    }
    return true;
  }

  public Map<Integer, ReturnMockResponse> getMatchingResponse(
      MockServiceRequest mockServiceRequest,
      Entry<MockRequest, MockResponse> entry, Map<String, Object> context) {
    Map<Integer, ReturnMockResponse> matchResponse;
    matchResponse = new HashMap<>();
    entry.getValue()
        .setOutput(getActualValueForAll(getDelimiter(entry.getKey().getContentType()), entry.getValue().getOutput(), context).toString());
    final ReturnMockResponse returnMockResponse = virtualServiceValidRequest
        .returnMockResponse(mockServiceRequest,
            entry, 1);
    returnMockResponse.setExactMatch(true);
    matchResponse.put(1, returnMockResponse);
    return matchResponse;
  }

  public boolean matchParameters(MockServiceRequest mockServiceRequest,
      Entry<MockRequest, MockResponse> entry,
      Map<String, Object> context) {

    Map.Entry<String, String> delimiter = getDelimiter(entry.getKey().getContentType());
    List<String> requestMatchingParam = entry.getKey().getAvailableParams().stream()
        .filter(x -> x.getValue().startsWith(delimiter.getKey()) &&
            x.getValue().endsWith(delimiter.getValue()))
        .map(VirtualServiceKeyValue::getKey).collect(Collectors.toList());
    List<VirtualServiceKeyValue> replacedParamMap = new ArrayList<>();
    for (VirtualServiceKeyValue param : entry.getKey().getAvailableParams()) {
      replacedParamMap.add(new VirtualServiceKeyValue(param.getKey(),
          getActualValueForAll(delimiter, param.getValue(), context).toString()));
    }
    boolean matches = replacedParamMap.stream().filter(
        x -> requestMatchingParam.contains(x.getKey())).allMatch(
        y -> getObjectValue(mockServiceRequest.getParams().get(y.getKey()), y.getValue()));
    return !requestMatchingParam.isEmpty() && matches;
  }

  public boolean getObjectValue(Object actual, Object object) {
    try {
        Param param = new Param();
      param.setType(actual.getClass());
      param.setActualValue(actual.toString());
      param.setExpectedValue(object.toString());
      return ParamTypes.fromString(object.getClass().getCanonicalName()).compareParam(param);
    } catch (ClassCastException e) {
      return false;
    }
  }

  public Map<Integer, ResponseParam> handleParameterizedRequest(
      Map<MockRequest, MockResponse> mockDataSetupMap,
      MockServiceRequest mockServiceRequest) {
    JSONArray paramArray = new JSONArray(mockServiceRequest.getRule().toString());
    Map<Integer, ResponseParam> responseMap = new HashMap<>();
    for (int i = 0; i < paramArray.length(); i++) {
      ResponseParam response = new ResponseParam();
      try {
        JSONObject map = paramArray.getJSONObject(i);
        Map<String, Object> context = map.toMap();
        checkRequest(mockDataSetupMap, mockServiceRequest, response, context);
        checkResponse(mockServiceRequest, response, context);
      } catch (Exception e) {
        response.getRecords().put("error", e.getMessage());
      }
      if (!response.getRecords().isEmpty()) {
        responseMap.put(i, response);
      }
    }
    return responseMap;
  }

  public void checkResponse(MockServiceRequest mockServiceRequest, ResponseParam response,
      Map<String, Object> context) throws InvalidMockResponseException {
    if (mockServiceRequest.getOutput() != null) {
      mockServiceRequest
          .setOutput(getActualValueForAll(getDelimiter(mockServiceRequest.getContentType()), mockServiceRequest.getOutput(), context));
      VirtualServiceRequest request = new VirtualServiceRequest();
      BeanUtils.copyProperties(mockServiceRequest, request);
      boolean isValidResponse = isMockResponseBodyValid(request);
      if (!isValidResponse) {
        response.getRecords().put(
            "response", "Invalid response!");
      }
    }
  }

  public Map<Integer, ReturnMockResponse> checkRequest(
      Map<MockRequest, MockResponse> mockDataSetupMap,
      MockServiceRequest mockServiceRequest, ResponseParam response, Map<String, Object> context)
      throws IOException, JAXBException {

    Map<Integer, ReturnMockResponse> returnMockResponseMap = new HashMap<>();

    boolean checkIfExists = false;
    if (mockServiceRequest.getParams() != null && mockServiceRequest.getInput() == null){
      populateMapParams(mockServiceRequest.getParams(), context);
      returnMockResponseMap = virtualServiceValidRequest
        .validForParam(mockDataSetupMap, mockServiceRequest);
      checkIfExists  = returnMockResponseMap == null || returnMockResponseMap.isEmpty();
    }

    if (mockServiceRequest.getInput() != null) {
      mockServiceRequest
          .setInput(getActualValueForAll(getDelimiter(mockServiceRequest.getContentType()), mockServiceRequest.getInput(), context));
          returnMockResponseMap = virtualServiceValidRequest
          .validObject(mockDataSetupMap, mockServiceRequest);
      checkIfExists  = returnMockResponseMap == null || returnMockResponseMap.isEmpty();
    }
    if (checkIfExists && mockDataSetupMap.entrySet().stream().
            anyMatch(x -> ResponseProcessType.PARAMS.name()
                .equalsIgnoreCase(x.getKey().getType().toLowerCase()))) {
      returnMockResponseMap = getParameterizedResponse(mockDataSetupMap, mockServiceRequest);
    }

    if (returnMockResponseMap != null && !returnMockResponseMap.isEmpty()) {
      response.getRecords()
          .put("request", "Mock already Present!");
    }
    return returnMockResponseMap;
  }

  public Object returnResponse(Method method, MockServiceRequest mockServiceRequest)
      throws IOException, JAXBException {
    VirtualServiceUtil.log
        .info(" mockServiceRequest.getResource() : " + mockServiceRequest.getResource());
    final Map<MockRequest, MockResponse> mockDataSetupMap = readDynamicResponse(
        mockServiceRequest.getResource(), mockServiceRequest.getOperationId());

    //Rule Execution
    Map<Integer, ReturnMockResponse> returnMockResponseMap =
        validateBusinessRules(mockDataSetupMap, mockServiceRequest);

    returnMockResponseMap = getParameterizedResponse(mockDataSetupMap, mockServiceRequest);

    //No Rule conditions exists/met then run the script
    if (returnMockResponseMap == null || returnMockResponseMap.isEmpty()) {
      try {
        returnMockResponseMap = virtualServiceValidRequest
            .checkScriptResponse(mockDataSetupMap, mockServiceRequest);
      } catch (ScriptErrorException e) {
        log.error("Error  in Script configuration :" + e.getMessage());
      }
    }

    //No script conditions exists/met then run the mock response
    if (returnMockResponseMap == null || returnMockResponseMap.isEmpty()) {
      returnMockResponseMap =
          isResponseExists(mockDataSetupMap, mockServiceRequest);
    }

    VirtualServiceUtil.log.debug("number of matches : {}",
        returnMockResponseMap != null ? returnMockResponseMap.size() : "Not found");
    ReturnMockResponse rMockResponse = null;
    if (returnMockResponseMap != null && !returnMockResponseMap.isEmpty()) {
      final List<ReturnMockResponse> returnMockResponseList =
          new ArrayList<>(returnMockResponseMap.values());
      Collections.sort(returnMockResponseList, new BestMatchComparator());
      VirtualServiceUtil.log.debug("Sorted list : " + returnMockResponseList);
      rMockResponse = returnMockResponseList.stream()
          .filter(ReturnMockResponse::isExactMatch).findAny().orElse(null);
      if (rMockResponse != null) {
        return getResponse(method, returnMockResponseList);
      }
    } else {
      VirtualServiceUtil.log.error(
          " Unable to find matching for the given request >>> " + mockServiceRequest);
    }
    return mockResponseNotFoundorSet(method, mockDataSetupMap);
  }

  public Object getResponse(Method method, List<ReturnMockResponse> returnMockResponseList)
      throws JAXBException {
    ReturnMockResponse rMockResponse;
    ResponseEntity<String> responseEntity;
    rMockResponse = returnMockResponseList.iterator().next();
    if (WSResource.isExists(method)) {
      return returnSoapResponse(method, rMockResponse);
    }
    if (rMockResponse.getHeaderResponse() != null) {
      responseEntity = buildResponseEntity(rMockResponse.getMockResponse(),
          rMockResponse.getHeaderResponse());
    } else {
      responseEntity = buildResponseEntity(rMockResponse.getMockResponse(), null);
    }
    virtualService.updateUsageTime(rMockResponse.getMockRequest());
    return returnResponse(method, responseEntity, responseEntity.getBody());
  }


  private Object returnSoapResponse(Method method, ReturnMockResponse rMockResponse)
      throws JAXBException {
    if (rMockResponse.getMockResponse().getOutput() != null) {
      if (ContentType.XML.equals(rMockResponse.getMockRequest().getContentType())) {
        return XMLConverter
            .xmlToObject(method.getReturnType(), rMockResponse.getMockResponse().getOutput());
      }
      Type mySuperclass = null;

      try {
        mySuperclass = method.getGenericReturnType();
        return this.objectMapper.readValue(rMockResponse.getMockResponse().getOutput(),
            this.objectMapper.constructType(mySuperclass));
      } catch (Exception ex) {
        log.error(" GenericReturnType  >>> mySuperclass " + mySuperclass);
        throw new SoapFaultException(
            "MOCK NOT FOUND (" + ex.getMessage() + ")  GenericReturnType  >>> mySuperclass "
                + method.getReturnType());
      }
    } else {
      throw new SoapFaultException("MOCK NOT FOUND");
    }
  }

  private Object returnResponse(Method method, ResponseEntity<String> responseEntity,
      String response) {
    VirtualServiceUtil.log.debug(" responseEntity.getHeaders() :" + responseEntity.getHeaders());
    if (response != null) {
      final String responseOut = xmlConverter.returnAsXml(method, responseEntity, response);
      if (method.getReturnType().equals(ResponseParam.class)) {
        return Response.status(responseEntity.getStatusCode().value()).entity(responseOut)
            .build();
      } else if (method.getReturnType().equals(ResponseEntity.class)) {
        return new ResponseEntity<>(responseOut, responseEntity.getHeaders(),
            responseEntity.getStatusCode());
      }
      Type mySuperclass = null;
      try {
        mySuperclass = method.getGenericReturnType();
        objectMapper.readValue(response, objectMapper.constructType(mySuperclass));
        return objectMapper.readValue(response, objectMapper.constructType(mySuperclass));
      } catch (final Exception e) {
        VirtualServiceUtil.log
            .error(" GenericReturnType  >>> mySuperclass " + mySuperclass);
      }
    }

    if (WSResource.isExists(method)) {
      String faultMsg = "MOC SERVER ERROR";
      if (responseEntity.getBody() != null) {
        faultMsg = responseEntity.getBody();
      }
      throw new SoapFaultException(faultMsg);
    }
    final ResponseException responseException = new ResponseException();
    if (VirtualServiceType.CXF_JAX_RS.compareTo(getVirtualServiceType()) == 0) {
      responseException
          .setResponse(Response.status(responseEntity.getStatusCode().value())
              .entity(responseEntity.getBody()).build());
      throw new WebApplicationException(responseException.getResponse());
    } else if (VirtualServiceType.SPRING.compareTo(getVirtualServiceType()) == 0) {
      responseException.setResponseEntity(responseEntity);
      throw responseException;
    }
    return null;
  }


  // covert response as XML if the accept is xml
  private Object mockResponseNotFoundorSet(Method method,
      Map<MockRequest, MockResponse> mockDataSetupMap) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON); // TO-DO
    if (mockDataSetupMap.size() > 0) {
      return returnResponse(method,
          new ResponseEntity<>(
              messageSource.getMessage("VS_RESPONSE_NOT_FOUND", null, locale),
              headers, HttpStatus.INTERNAL_SERVER_ERROR),
          null);
    } else {
      VirtualServiceUtil.log.error("Mock Response was not defined for the given input");
      return returnResponse(method,
          new ResponseEntity<>(messageSource.getMessage("VS_DATA_NOT_SET", null, locale),
              headers, HttpStatus.INTERNAL_SERVER_ERROR),
          null);
    }
  }

  private ResponseEntity<String> buildResponseEntity(MockResponse mockResponse,
      Map<String, String> headerMap) {
    final HttpHeaders headers = buildHeader(mockResponse, headerMap);
    return new ResponseEntity<>(mockResponse.getOutput(), headers,
        HttpStatus.valueOf(Integer.parseInt(mockResponse.getHttpStatusCode())));
  }

  private HttpHeaders buildHeader(MockResponse mockResponse, Map<String, String> headerMap) {
    final HttpHeaders headers = new HttpHeaders();
    try {
      if (MediaType.valueOf(headerMap.get("accept")).includes(MediaType.ALL)) {
        headers.setContentType(MediaType.APPLICATION_JSON);
      } else {
        headers.setContentType(MediaType.valueOf(headerMap.get("accept")));
      }
      for (final VirtualServiceKeyValue keyValuePair : mockResponse.getHeaderParams()) {
        headers.add(keyValuePair.getKey(), keyValuePair.getValue());
      }
    } catch (final Exception e) {
      log.warn("buildHeader unexpected {}", e.getMessage());
    }
    return headers;
  }
}
