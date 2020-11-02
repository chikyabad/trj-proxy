package com.cac;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

public class BackendHandler {

	Logger log = LoggerFactory.getLogger(BackendHandler.class);

	DestinationConfiguration destConfiguration;

	HttpURLConnection connection = null;

	public BackendHandler(String destination) {

		try {
			this.destConfiguration = null;
			Context ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
					.lookup("java:comp/env/connectivityConfiguration");

			this.destConfiguration = configuration.getConfiguration(destination);

		} catch (NamingException e) {
			e.printStackTrace();
			log.error("Error while fetching destination" + e);
		}

	}

	public String getServiceUrl() throws Exception {
		String url = null;
		if (this.destConfiguration != null) {

			// get the destination URL
			url = destConfiguration.getProperty("URL");

			log.info("Destination URL: " + url);
		}

		return url;
	}

	public String getUserId() throws Exception {
		String userid = null;
		if (this.destConfiguration != null) {

			// get the destination user
			userid = destConfiguration.getProperty("User");

			log.info("Destinatinon Userid: " + userid);
		}

		return userid;
	}

	public String getPassword() throws Exception {
		String pwd = null;
		if (this.destConfiguration != null) {

			// get the destination password
			pwd = destConfiguration.getProperty("Password");

		}

		return pwd;
	}
	
	public String getLocationId() throws Exception {
		String locationId = null;
		if (this.destConfiguration != null) {
			
			// get the destination location id
			locationId = destConfiguration.getProperty(DestinationConfiguration.DESTINATION_CLOUDCONNECTOR_LOCATION_ID);
		}
		
		return locationId;
	}
	
	public Proxy getProxy() {

        String proxyHost = null;
        String proxyPortString = null;

        proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
        proxyPortString = System.getenv("HC_OP_HTTP_PROXY_PORT");

        log.info("proxyHost = {} / proxyPort = {}", proxyHost, proxyPortString);

        if (proxyPortString != null && proxyHost != null) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPortString)));
        } else {
            return Proxy.NO_PROXY;
        }
    }

}