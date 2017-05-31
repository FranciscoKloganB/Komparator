package org.komparator.security.handler;

import java.io.IOException;

import java.lang.RuntimeException;

import java.text.ParseException;

import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.komparator.security.domain.SecurityManager;

import javax.xml.soap.*;
import javax.xml.namespace.QName;
import java.security.NoSuchAlgorithmException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler adds an header with date and time to client's outgoing messages.
 * If the message is inbound on server it instead reads the header instead.
 * If the inbound message was recieved with a time window bigger then 3s after
 * the outgoing messag was sent, then the message will be rejected before actual
 * server delivery.
 */
public class FreshnessSupplierClientHandler implements SOAPHandler<SOAPMessageContext> {
	public static final String TIMESTAMP = "timestamp";
	public static final String ENDPOINT_NAME = "EndpointName";
	private SecurityManager secM = SecurityManager.getInstance();

	/** Gets the header blocks that can be processed by this Handler instance. */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** This method is invoked for normal processing of inbound and outbound messages. */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		SOAPHeaderElement requestIdHeaderElement, timestampHeaderElement;
		String recievedTimeStamp, requestId;
		Name timestampName, requestIdName;
		Iterator tdsIt, rIdIt;

		String timeStamp = secM.generateTimeStamp();
		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();

		try {
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			timestampName = envelope.createName("TIMESTAMP", "freshness", "http://ff");
			requestIdName = envelope.createName("SREQUESTID", "freshness", "http://ff");
			if (isOutbound) {
				if (header == null)
					header = envelope.addHeader();
				timestampHeaderElement = header.addHeaderElement(timestampName);
				requestIdHeaderElement = header.addHeaderElement(requestIdName);
				timestampHeaderElement.addTextNode(timeStamp);
				requestIdHeaderElement.addTextNode(secM.generateSecureNumber());
				return true;
			}
		} catch (SOAPException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/** Called prior to the JAX-WS runtime dispatching a message, fault or exception. */
	@Override
	public void close(MessageContext messageContext) {}

}
