package com.anz.psp.solace;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;


public class SiteActivatorMessageConsumer implements XMLMessageListener  {

	private SEMPRestCallApplication sempRestCallApplication;

	private String applicationMode;
	
	private boolean solaceHealthCheckResponse;

	public void setApplicationMode(String applicationMode) {
		this.applicationMode = applicationMode;
	}

	public void setSEMPRestCallApplication(SEMPRestCallApplication sempRestCallApplication) {
		this.sempRestCallApplication = sempRestCallApplication;
	}

	private CountDownLatch latch = new CountDownLatch(1);
	private static final Logger logger = LoggerFactory.getLogger(SiteActivatorMessageConsumer.class);

	public void onReceive(BytesXMLMessage msg) {
		if (msg instanceof TextMessage) {
			logger.info("============= TextMessage received: " + ((TextMessage) msg).getText());
		} else {
			logger.info("============= Message received.");
		}
		//latch.countDown(); // unblock main thread
	}

	public void onException(JCSMPException e) {

		
		logger.info("####### Message consumer connection with Solace is disrupted, Siteactivator will perform further verfication and actions. ######");

		logger.error("\n######## Exception details are :::: " , e);
		
		logger.info("####### Checking the value of current application mode :: " + applicationMode);

		if (applicationMode != null && applicationMode.equalsIgnoreCase("normal")) {
			logger.info(
					"####### Application is running in NORMAL mode, site activator will call the health check URLs before enabling cross site VPN");
			try {
				solaceHealthCheckResponse = sempRestCallApplication.solaceHealthCheck();
			} catch (Exception e1) {
				logger.error("####### Exception in Solace health check ###### ",e1);
			}
			if (!solaceHealthCheckResponse) {
				logger.info("######## Health Check Status is DOWN for both the nodes, changing the replication role of VPN on cross site to Acive ########");
				sempRestCallApplication.changeReplicationRole(true);
			}else {
				logger.info("######## Solace health check indicates that one of the two nodes is UP, hence site activator is not doing any update on Solace ######## ");
			}
		}else {
			logger.error("####### Application is running in Maintenance mode, Site activator will not take any action ########");
		}


	}


	public CountDownLatch getLatch() {
		return latch;
	}

	
}


