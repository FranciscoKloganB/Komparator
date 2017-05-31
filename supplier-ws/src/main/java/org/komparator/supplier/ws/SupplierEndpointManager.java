package org.komparator.supplier.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import org.komparator.security.domain.SecurityManager;


/** End point manager */
public class SupplierEndpointManager {

	private String uddiURL = null;
	private String wsName = null;
	private String wsURL = null;
	private Endpoint endpoint = null;
	private UDDINaming uddiNaming = null;
	private boolean verbose = true;
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

	public String getUddiUrl() {
		return uddiURL;
	}

	public String getWsName() {
		return wsName;
	}

	public String wsURL() {
		return wsURL;
	}

	public SupplierPortType getPort() {
		return portImpl;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public SupplierEndpointManager(String wsURL) {
		if (wsURL == null) {
			throw new NullPointerException("Webservice unified resource locater can't be null!");
		}
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL, uddiURLs for service publication and the associated service name */
	public SupplierEndpointManager(String wsURL, String wsName, String uddiURL) {
		if (wsURL == null || wsName == null || uddiURL == null) {
			throw new NullPointerException("WebService unified resource locater, UDDI address and service name can't be null!");
		}
		SecurityManager secM = SecurityManager.getInstance();
		secM.setEntityName(wsName);
		this.wsURL = wsURL;
		this.wsName = wsName;
		this.uddiURL = uddiURL;
	}

	/* end point management */
	public void start() throws UDDINamingException, Exception {
		// publish endpoint and publish to UDDI
		try {
			uddiNaming = new UDDINaming(uddiURL);
			endpoint = Endpoint.create(this.portImpl);

			if (verbose) { System.out.printf("Starting %s%n", wsURL); }
			endpoint.publish(wsURL);

			if (verbose) { System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL); }
			uddiNaming.rebind(wsName, wsURL);

		} catch (UDDINamingException uNE) {
			endpoint = null;
			uddiNaming = null;
			if (verbose) { System.out.println("Caught uddi naming exception when trying to publish webservice:" + uNE); }
			throw uNE;
		} catch (Exception e) {
			endpoint = null;
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) { System.out.printf("Caught i/o exception when awaiting requests: %s%n", e); }
		}
	}

	public void stop() throws UDDINamingException, Exception {
		try {
			if (endpoint != null) {
				endpoint.stop();
				if (verbose) { System.out.printf("Stopped %s%n", wsURL); }
			}
			if (uddiNaming != null) {
				uddiNaming.unbind(wsName);
				System.out.printf("Deleted '%s' from UDDI%n", wsName);
			}
		} catch (UDDINamingException uNE) {
			if (verbose) { System.out.printf("Caught uddiNamingException when stopping: %s%n", uNE); }
		} catch (Exception e) {
			if (verbose) { System.out.printf("Caught exception when stopping: %s%n", e); }
		}
		this.portImpl = null;
	}
}
