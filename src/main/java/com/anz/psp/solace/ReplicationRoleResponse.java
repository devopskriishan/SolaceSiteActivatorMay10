package com.anz.psp.solace;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class ReplicationRoleResponse {

	private String responseCode;
	
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	@Override
	public String toString() {
		return "ReplicationRoleResponse [responseCode=" + responseCode+ "]";
	}

	
	

}