package org.komparator.security.handler;

import java.io.IOException;

import java.lang.RuntimeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
public class FreshnessSupplierHandler implements SOAPHandler<SOAPMessageContext> {
	private SecurityManager secM = SecurityManager.getInstance();

	/** Gets the header blocks that can be processed by this Handler instance. */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** This method is invoked for normal processing of inbound and outbound messages. */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		String recievedTimeStamp, requestId;
		SOAPElement timestampElement, requestIdElement;
		Name timestampName, requestIdName;
		Iterator timestampIterator, requestIdIterator;

		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();
		String timeStamp = secM.generateTimeStamp();

		try {
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			timestampName = envelope.createName("TIMESTAMP", "freshness", "http://ff");
			requestIdName = envelope.createName("SREQUESTID", "freshness", "http://ff");
			if (!isOutbound) {
				if (header == null)
					throw new RuntimeException("Operation message had no header.");
				timestampIterator = header.getChildElements(timestampName);
				requestIdIterator = header.getChildElements(requestIdName);
				if (!timestampIterator.hasNext() || !requestIdIterator.hasNext())
					throw new RuntimeException("Could not find timestamp or request values inside header.");
				timestampElement = (SOAPElement)timestampIterator.next();
				requestIdElement = (SOAPElement)requestIdIterator.next();
				recievedTimeStamp = timestampElement.getValue();
				requestId = requestIdElement.getValue();
				if (!secM.freshMessage(recievedTimeStamp, timeStamp)) {
					throw new RuntimeException("Request is not fresh.");
				} if (!secM.shouldAnswer(requestId)) {
					throw new RuntimeException("Request was already answered.");
				}
				return true;
			}
		} catch (SOAPException e) {
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
