package com.cac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRJAppProxy extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String REFINERY_ECC_LIN_BASIC = "Refinery_ECC_LIN_BASIC";
	Logger log = LoggerFactory.getLogger(BackendHandler.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("Executing GET operation.");
		proxyTrjApp(request,response);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Executing POST operation.");
	}
		
	private void proxyTrjApp(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		try {
			
			// Get destination details
			BackendHandler backendhandler = new BackendHandler(REFINERY_ECC_LIN_BASIC);
			
			// Build backend URL	
			String languageParameter = "?sap-language=en";
			String userParameter = "&GV_USRID=" + request.getUserPrincipal().getName();
			String referenceNumParameter = request.getParameter("GV_REFNO");
			String backendURL = backendhandler.getServiceUrl() + languageParameter +userParameter;
			if (referenceNumParameter != null){
				backendURL.concat("&GV_REFNO=" + referenceNumParameter);
			}
			URL url = new URL(backendURL);
			log.info("Backend URL: "+ backendURL);
			
			// Create connection object
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			// Set request HTTP method
			connection.setRequestMethod(request.getMethod());
			
			// Set request HTTP header
			connection.setRequestProperty("Content-Type", request.getContentType());
     		String auth = backendhandler.getUserId() + ":" + backendhandler.getPassword(); 
     		String authHeaderValue = Base64.getEncoder().encodeToString(auth.getBytes("utf-8")); 
			connection.setRequestProperty("Authorization", authHeaderValue);
			connection.setRequestProperty("SAP-Connectivity-SCC-Location_ID", backendhandler.getLocationId());
			
			// Execute connection
			connection.connect();
			
			// Reset response
			response.reset();
			
			// Set response header
			response.setContentType(connection.getContentType());
			log.info("Backend Response Content-Type:" + connection.getContentType());

			// Set response HTTP code
			response.setStatus(connection.getResponseCode());
			log.info("Backend Response Code:" + String.valueOf(connection.getResponseCode()));
			
			// Set response body
			OutputStream osRes = response.getOutputStream();
			this.copyStream(connection.getInputStream(), osRes);
			osRes.flush();
			osRes.close();		
			
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.info("IOException: "+ e);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.info("Exception: "+ e);
		}
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}
