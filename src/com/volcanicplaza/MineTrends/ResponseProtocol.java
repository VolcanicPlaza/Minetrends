package com.volcanicplaza.MineTrends;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
"RESPONSE_CODE",
"RESPONSE_MESSAGE"
})
public class ResponseProtocol {
	
	@JsonProperty("RESPONSE_CODE")
	private int RESPONSE_CODE;
	@JsonProperty("RESPONSE_MESSAGE")
	private String RESPONSE_MESSAGE;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	
	@JsonProperty("RESPONSE_CODE")
	public int getRESPONSE_CODE() {
	return RESPONSE_CODE;
	}
	
	@JsonProperty("RESPONSE_CODE")
	public void setRESPONSE_CODE(int RESPONSE_CODE) {
	this.RESPONSE_CODE = RESPONSE_CODE;
	}
	
	@JsonProperty("RESPONSE_MESSAGE")
	public String getRESPONSE_MESSAGE() {
	return RESPONSE_MESSAGE;
	}
	
	@JsonProperty("RESPONSE_MESSAGE")
	public void setRESPONSE_MESSAGE(String RESPONSE_MESSAGE) {
	this.RESPONSE_MESSAGE = RESPONSE_MESSAGE;
	}
	
	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
	return this.additionalProperties;
	}
	
	@JsonAnySetter
	public void setAdditionalProperties(String name, Object value) {
	this.additionalProperties.put(name, value);
	}

}