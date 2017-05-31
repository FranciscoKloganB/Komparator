package org.komparator.mediator.ws;

import java.io.IOException;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

import org.komparator.security.domain.SecurityManager;

/** End point manager */
public class MediatorEndpointManager {
	private Endpoint endpoint = null;
	private UDDINaming uddiNaming = null;
	private String uddiURL = null;
	private String wsName = null;
	private String wsURL = null;
	private String lastLifeProof = null;
	private boolean isPrimaryMediator;
	private MediatorPortImpl portImpl = new MediatorPortImpl(this);
	private LifeProof lifeProofObj;
	private static final String secondaryWsURL = "http://localhost:8072/mediator-ws/endpoint";

	public MediatorPortType getPort() {
    return portImpl;
	}

	public String getUddiUrl() {
		return uddiURL;
	}

	public String getWsName() {
		return wsName;
	}

	public String wsURL() {
		return wsURL;
	}

	public Endpoint getEndPoint() {
		return endpoint;
	}

	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	/** Returns the secondary mediator wsURL */
	public String getSecondaryWsURL() {
		return secondaryWsURL;
	}

	/**
	* Returns a boolean value that defines if this endpoint manager belongs to a
	* primary Mediator or to a secondary Mediator.
	*/
	public boolean getMediatorStatus() {
		return isPrimaryMediator;
	}

	/** Changes the boolean value of mediator primacy according to current value */
	public void toggleMediatorStatus() {
		if (isPrimaryMediator) {
			this.isPrimaryMediator = false;
		} else {
			this.isPrimaryMediator = true;
		}
	}

	/**
	* Returns the time when this secondary mediator recieved proof of life from the
	* primary mediator.
	*/
	public String getLastLifeProof() {
		return lastLifeProof;
	}

	public MediatorClient getSecondaryMediatorClient() {
		return lifeProofObj.getSecondaryMediatorClient();
	}

	/**
	* Sets lastLifeProof attribute to the string representation of a date
	* that represents the last time a primary mediator told this secondary mediator
	* that he is still up and running.
	*/
	public void setlastLifeProof(String date) {
		this.lastLifeProof = date;
	}

	public void setLifeProofObj(LifeProof obj) {
		this.lifeProofObj = obj;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL) {
		SecurityManager secM = SecurityManager.getInstance();
		secM.setEntityName(wsName);
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public MediatorEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web service unified resource locator can't be null!");
		this.wsURL = wsURL;
	}

	private boolean registeredMediators() throws UDDINamingException {
		if (uddiNaming.lookupRecord(wsName) == null) {
			return false;
		}
		return true;
	}

	public void start() throws Exception {
		try {
			uddiNaming = new UDDINaming(uddiURL);
			endpoint = Endpoint.create(this.portImpl);
			System.out.printf("Starting %s%n", wsURL);
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			System.out.printf("Caught exception when starting: %s%n", e);
			throw e;
		}
		if (!registeredMediators() & !wsURL.equals(secondaryWsURL)) {
			System.out.println("Starting " + wsName + " as primary server.");
			publishToUDDI();
			isPrimaryMediator = true;
		} else {
			isPrimaryMediator = false;
			System.out.println("Starting " + wsName + " as secondary server.");
		}
	}

	public void awaitConnections() {
		System.out.println("Awaiting connections");
		System.out.println("Press enter to shutdown");
		try {
			System.in.read();
		} catch (IOException e) {
			System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				endpoint.stop();
				System.out.printf("Stopped %s%n", wsURL);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when stopping: %s%n", e);
		}
		if (isPrimaryMediator) {
			unpublishFromUDDI();
		}
	}

	public void publishToUDDI() throws Exception {
		try {
			if (uddiURL != null) {
				System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
				uddiNaming.rebind(wsName, wsURL);
				isPrimaryMediator = true;
			}
		} catch (Exception e) {
			uddiNaming = null;
			System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			throw e;
		}
	}

	public void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				uddiNaming.unbind(wsName);
				System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				uddiNaming = null;
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when unbinding: %s%n", e);
		}
	}

}
