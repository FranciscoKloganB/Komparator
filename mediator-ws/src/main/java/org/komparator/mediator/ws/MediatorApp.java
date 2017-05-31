package org.komparator.mediator.ws;

import java.util.Timer;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class MediatorApp {
	private static final int MAX_WAITS = 10;
	private static final long TASK_DELAY = 4000;
	private static final long PERIOD = 5000;
	private static final Timer timer = new Timer(true);

	public static int getMaxWaits() {
		return MAX_WAITS;
	}

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}

		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		MediatorEndpointManager endpoint = null;

		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
		}
		try {
			endpoint.start();
			LifeProof task = new LifeProof(endpoint);
			timer.scheduleAtFixedRate(task, TASK_DELAY, PERIOD);
			endpoint.setLifeProofObj(task);
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
			timer.cancel();
			timer.purge();
		}
	}
}
