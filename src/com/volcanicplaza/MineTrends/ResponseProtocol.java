package com.volcanicplaza.Minetrends;


import javax.annotation.Generated;

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
	
	@JsonProperty("RESPONSE_CODE")
	public int getRESPONSE_CODE() {
	return RESPONSE_CODE;
	}
	
	@JsonProperty("RESPONSE_MESSAGE")
	public String getRESPONSE_MESSAGE() {
	return RESPONSE_MESSAGE;
	}

}