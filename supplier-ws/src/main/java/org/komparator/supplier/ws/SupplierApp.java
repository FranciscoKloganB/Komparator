package org.komparator.supplier.ws;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws UDDINamingException, Exception {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierApp.class.getName() + " wsURL");
			return;
		}

		// SupplierApp now recieves UDDI address and service name as arguments (P2-g1-2)
		String wsURL = args[0];
		String wsName = args[1];
		String uddiURL = args[2];
		// Create server implementation object
		SupplierEndpointManager endpoint = new SupplierEndpointManager(wsURL, wsName, uddiURL);
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}
	}
}
