package com.anz.psp.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;



@Component
public class SEMPRestCallApplication {

	private static final Logger log = LoggerFactory.getLogger(SEMPRestCallApplication.class);
	
	RestTemplate restTemplate = new RestTemplate();

	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	
	@Value("${semp.redundancy.status.call.payload}")
	private String redundancyStatusPayload;
	
	@Value("${redundancy.status.start}")
	private String REDUNDANCY_STATUS;
	
	@Value("${redundancy.status.closing}")
	private String REDUNDANCY_STATUS_CLOSING;
	
	@Value("${solace.cli.username}")
	private String cliUserName;
	
	@Value("${solace.cli.password}")
	private String cliPassword;
	
	@Value("${application.semp.url}")
	private String sempURL;
	
	@Value("${semp.replication.role.active.json.payload}")
	private String replicationJsonPayloadActive;
	
	@Value("${semp.replication.role.standby.json.payload}")
	private String replicationJsonPayloadStandby;
	
	@Value("${semp.replication.role.url}")
	private String replicationRoleURL;
	
	@Value("${semp.msgvpn.enable.json.payload}")
	private String vpnEnableJsonPayload;
	
	@Value("${solace.healthcheck.url.node1}")
	private String solaceHealthCheckURLNode1;
	
	@Value("${solace.healthcheck.url.node2}")
	private String solaceHealthCheckURLNode2;
	
	
	public boolean solaceHealthCheck() {
		requestFactory.setConnectTimeout(1000);
		requestFactory.setReadTimeout(1000);
		restTemplate.setRequestFactory(requestFactory);
		boolean node1CheckResponse = false;
		boolean node2CheckResponse = false;

		try {
			ResponseEntity<String> response = restTemplate.getForEntity(solaceHealthCheckURLNode1, java.lang.String.class, 1);
			if(response != null) {
				log.info(" ######## Solace Health Check URL  :::: " + solaceHealthCheckURLNode1 + " \n The response is :::: " + response.toString());
				if(response.getStatusCode() == HttpStatus.OK) {
					node1CheckResponse = true;
				}	
			}
		} catch (Exception e) {
			log.error("####### Exception in calling Solace Health Check URL ########" + e.getMessage());
			//e.printStackTrace();
			node1CheckResponse = false;
		}

		try {
			ResponseEntity<String> response2 = restTemplate.getForEntity(solaceHealthCheckURLNode2, java.lang.String.class, 1);
			if(response2 != null) {
				log.info(" ######## Solace Health Check URL  :::: " + solaceHealthCheckURLNode2 + " \n The response is :::: " + response2.toString());
				if(response2.getStatusCode() == HttpStatus.OK) {
					node2CheckResponse = true;
				}	
			}
		} catch (Exception e) {
			log.error("####### Exception in calling Solace Health Check URL ########" + e.getMessage());
			//e.printStackTrace();
			node2CheckResponse = false;
		}

		if(node1CheckResponse || node2CheckResponse) {
			return true;
		}else {
			return false;
		}
	}
	
	public void changeDMREnabled() {

		try {
			requestFactory.setConnectTimeout(1000);
			requestFactory.setReadTimeout(1000);

			restTemplate.setRequestFactory(requestFactory);

			String jsonInput2 = "{\"dmrEnabled\":false}";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth("admin", "admin");

			HttpEntity<String> entity = new HttpEntity<String>(jsonInput2, headers);
			String response = restTemplate.patchForObject("http://localhost:8080/SEMP/v2/config/msgVpns/testVPN",
					entity, java.lang.String.class);
			System.out.println("###### Update DMREnabled status done !!");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public String getRedundancyStatus() {
		String redundancyStatus = null;
		try {
			/* RestTemplate restTemplate = new RestTemplate(); */
			log.info("Payload for redundancy status check ::: " + redundancyStatusPayload);
			requestFactory.setConnectTimeout(1000);
			requestFactory.setReadTimeout(1000);
			restTemplate.setRequestFactory(requestFactory);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_XML);
			headers.setBasicAuth(cliUserName,cliPassword);

			HttpEntity<String> entity = new HttpEntity<String>(redundancyStatusPayload, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(sempURL, entity,
					java.lang.String.class);
			if(response != null) {
				String responseBody = response.getBody().trim();
				log.info("####### Redundancy status check response :::: \n" + responseBody);
				redundancyStatus = responseBody.substring(
						responseBody.indexOf(REDUNDANCY_STATUS) + REDUNDANCY_STATUS.length(),
						responseBody.indexOf(REDUNDANCY_STATUS_CLOSING));
				
			}
			
		} catch (Exception e) {
			log.error("####### Exception in redundancy status check #####");
			e.printStackTrace();
			return redundancyStatus;
		}
		return redundancyStatus;

	}
	
	public boolean changeReplicationRole(boolean active) {
			try {
			
				HttpEntity<String> entity;
				
				requestFactory.setConnectTimeout(1000);
				requestFactory.setReadTimeout(1000);

				restTemplate.setRequestFactory(requestFactory);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setBasicAuth(cliUserName, cliPassword);
				if(active) {
					 entity = new HttpEntity<String>(replicationJsonPayloadActive, headers);
				}else {
					entity = new HttpEntity<String>(replicationJsonPayloadStandby, headers);
				}
				
				ResponseEntity<String> response = restTemplate.exchange(replicationRoleURL, HttpMethod.PATCH, entity, java.lang.String.class);
				if(response.getStatusCode().equals(HttpStatus.OK)) {
					log.info("####### Replication role change was successful with response code ::: " + response.getStatusCodeValue());
					log.info("######## Replication role change response body :::: \n" + response.getBody());
					return true;
				}else {
					return false;
				}
			}catch (ResourceAccessException ex) {
				log.error("###### Error while changing the Replication role :::: " + ex.getMessage());
				ex.printStackTrace();
				return false;
			}catch (Exception e) {
				log.error("###### Error while changing the Replication role :::: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
	}

	
	public void enableVPN() {
		try {
			
			requestFactory.setConnectTimeout(1000);
			requestFactory.setReadTimeout(1000);

			restTemplate.setRequestFactory(requestFactory);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth(cliUserName, cliPassword);

			HttpEntity<String> entity = new HttpEntity<String>(vpnEnableJsonPayload, headers);
			String response = restTemplate.patchForObject(replicationRoleURL, entity, java.lang.String.class);
			log.info("####### Msg VPN Enabled Status Response ::: " + response);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
}

}
