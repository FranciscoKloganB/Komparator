package org.komparator.mediator.ws.domain;

import java.util.*;
import java.lang.*;
import java.io.*;
import javax.xml.ws.*;

import java.net.SocketTimeoutException;

public class FrontEndConfigurator {
	protected static final int FIRST_ATTEMPT = 1;
	protected static final int MAX_TIMEOUTS = 2;
	protected static final int MAX_RECONNECT_ATTEMPTS = 3;
	protected static final int CONNECTION_TIMEOUT = 5000;
	protected static final int RECEIVE_TIMEOUT = 3000;
	protected static final long THREAD_NAP = 9000;
	protected static final long THREAD_WAIT = 5000;
	protected static final boolean PERSISTENT_PROPERTY = false;
	protected static final List<String> CONN_TIME_PROPS = Collections.unmodifiableList(
		new ArrayList<String>() {{
				add("com.sun.xml.ws.connect.timeout");
				add("com.sun.xml.internal.ws.connect.timeout");
				add("javax.xml.ws.client.connectionTimeout");
		}});
	protected static final List<String> RECV_TIME_PROPS = Collections.unmodifiableList(
		new ArrayList<String>() {{
				add("com.sun.xml.ws.request.timeout");
				add("com.sun.xml.internal.ws.request.timeout");
				add("javax.xml.ws.client.receiveTimeout");
		}});

	public static int getFirstAttemptValue() {
		return FIRST_ATTEMPT;
	}

	public static int getMaxTimeoutsValue() {
		return MAX_TIMEOUTS;
	}

	public static int getMaxReconnectAttemptsValue() {
		return MAX_RECONNECT_ATTEMPTS;
	}

	public static int getConnectionTimeoutValue() {
		return CONNECTION_TIMEOUT;
	}

	public static int getRecieveTimeoutValue() {
		return RECEIVE_TIMEOUT;
	}

	public static long getThreadNapValue() {
		return THREAD_NAP;
	}

	public static long getThreadWaitValue() {
		return THREAD_WAIT;
	}

	public static List<String> getConnTimeProps() {
		return CONN_TIME_PROPS;
	}

	public static List<String> getRecvTimeProps() {
		return RECV_TIME_PROPS;
	}

		/** Filters the type of of WebServiceException that happened on a remote invocation */
	protected boolean socketTimeOut(Throwable cause) {
		if (cause != null && cause instanceof SocketTimeoutException)  {
			System.out.println("Client recieved timeout exception: " + cause.getMessage());
			System.out.println("Retrying remote service invocation...");
			return true;
		} else {
			System.out.println("Client recieved a non-timeout exception: " + cause.getMessage());
			System.out.println("Trying to reconnect to a mediator...");
			return false;
		}
	}
}
