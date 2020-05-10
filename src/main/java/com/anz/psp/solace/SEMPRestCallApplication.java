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
	
	@Value("${solace.env.semp.replication.role.url}")
	private String replicationRoleChangeURL;
	
	@Value("${solace.env.semp.replication.role.change.candidate.vpn.list}")
	private String replicationRoleChangeCandidateVPNsList;
	
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
			log.error("####### Exception in calling Solace Health Check URL :: "  + solaceHealthCheckURLNode1+ ", the error message is :::: ", e);
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
			log.error("####### Exception in calling Solace Health Check URL :: "  + solaceHealthCheckURLNode2+ ", the error message is :::: " , e);
			//e.printStackTrace();
			node2CheckResponse = false;
		}

		return (node1CheckResponse || node2CheckResponse);
		
	}
	
	
	public void changeReplicationRole(boolean active) {
		try {

			HttpEntity<String> entity;

			requestFactory.setConnectTimeout(1000);
			requestFactory.setReadTimeout(1000);

			restTemplate.setRequestFactory(requestFactory);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth(cliUserName, cliPassword);
			if (active) {
				entity = new HttpEntity<String>(replicationJsonPayloadActive, headers);
			} else {
				entity = new HttpEntity<String>(replicationJsonPayloadStandby, headers);
			}

			if (replicationRoleChangeCandidateVPNsList != null) {
				String[] vpnsList = replicationRoleChangeCandidateVPNsList.split(",");
				String fullReplicationRoleChangeURL = null;
				for (String vpnName : vpnsList) {
					try {
						fullReplicationRoleChangeURL = replicationRoleChangeURL.concat(vpnName);
						ResponseEntity<String> response = restTemplate.exchange(fullReplicationRoleChangeURL,
								HttpMethod.PATCH, entity, java.lang.String.class);
						if (response.getStatusCode().equals(HttpStatus.OK)) {
							log.info("####### Replication role change was successful for following URL :: "
									+ fullReplicationRoleChangeURL + "\t, the response code ::: "
									+ response.getStatusCodeValue());
							log.info("\n ######## Replication role change response body :::: \n" + response.getBody());
						} else {
							log.error("####### Replication role change was UNSUCCESSFUL for following URL :: "
									+ fullReplicationRoleChangeURL + "\t, the response code ::: "
									+ response.getStatusCodeValue());
							log.error("\n ######## Replication role change response body :::: \n" + response.getBody());
						}
						fullReplicationRoleChangeURL = null;
					} catch (ResourceAccessException ex) {
						log.error("###### Error while changing the Replication role, URL  :::: "+ fullReplicationRoleChangeURL);
						log.error("###### Error details are :::: " ,ex);
						//ex.printStackTrace(System.err);
					} catch (Exception e) {
						log.error("###### Error while changing the Replication role, URL  :::: "+ fullReplicationRoleChangeURL);
						log.error("###### Error details are :::: " ,e);
						//e.printStackTrace(System.err);
					}
				}

			}
		} catch (Exception e) {
			log.error("###### Error while changing the Replication role, error details are  :::: ", e);
			//e.printStackTrace(System.err);
		}
	}

	

	

}
