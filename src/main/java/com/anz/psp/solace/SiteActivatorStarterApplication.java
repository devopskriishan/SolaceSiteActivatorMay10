
package com.anz.psp.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.solace.services.core.model.SolaceServiceCredentials;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.SpringJCSMPFactoryCloudFactory;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;

@SpringBootApplication
public class SiteActivatorStarterApplication {
	

    public static void main(String[] args) {
        SpringApplication.run(SiteActivatorStarterApplication.class, args);
    }
  
    
    @Component
    static class Runner implements CommandLineRunner {

        private static final Logger logger = LoggerFactory.getLogger(Runner.class);

        private final Topic topic = JCSMPFactory.onlyInstance().createTopic("tutorial/topic");

        @Autowired private SpringJCSMPFactory solaceFactory;

        // Examples of other beans that can be used together to generate a customized SpringJCSMPFactory
        @Autowired(required=false) private SpringJCSMPFactoryCloudFactory springJCSMPFactoryCloudFactory;
        @Autowired(required=false) private SolaceServiceCredentials solaceServiceCredentials;
        @Autowired(required=false) private JCSMPProperties jcsmpProperties;
        @Autowired SEMPRestCallApplication sempRestCallApplication;
   
        public SiteActivatorMessageConsumer msgConsumer = new SiteActivatorMessageConsumer();
        
        @Value("${application.mode}")
    	private String applicationMode;
        
	    public void run(String... strings) throws Exception {
        	
        
        	try {
				msgConsumer.setSEMPRestCallApplication(sempRestCallApplication);
				msgConsumer.setApplicationMode(applicationMode);
				
				final JCSMPSession session = solaceFactory.createSession();
				
				XMLMessageConsumer cons = session.getMessageConsumer(msgConsumer);

				session.addSubscription(topic);
				logger.info("#################  Session with Solace established successfully. Consumer connection established. Awaiting message... ######################");
				cons.start();
			} catch (Exception e) {
				logger.error("######## Exception occured while creating Solace session, exception is :::: ",e);
				throw e;
			}
            // Consumer session is now hooked up and running!

 
        }
    }
}
