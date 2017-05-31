package org.komparator.security.handler;

import java.io.*;
import java.util.*;
import java.security.*;
import javax.xml.soap.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.domain.SecurityManager;
/**
* This SOAPHandler ciphers and deciphers the credit card number if mediator-cli
* and outbound and deciphers if mediator and inbound
*/
public class RequestIdMediatorHandler implements SOAPHandler<SOAPMessageContext> {
	private SecurityManager secM = SecurityManager.getInstance();
	public static final String REQUEST_ID = "requestIdentifier";


	/** Gets the header blocks that can be processed by this Handler instance. */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** This method is invoked for normal processing of inbound and outbound messages. */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		String requestId, requestedOperation;
		Iterator requestIdIterator;
		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();
		Name requestIdName;
		SOAPElement requestIdElement;
		SOAPHeaderElement requestIdHeaderElement;

		try {
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			QName operation_name = (QName)smc.get(MessageContext.WSDL_OPERATION);

			requestedOperation = operation_name.getLocalPart();
			if (!requestedOperation.equals("buyCart")) {
			 	if (!requestedOperation.equals("addToCart")) {
					if (!requestedOperation.equals("clear")) {
						return true;
					}
				}
			}

			if (!isOutbound) {
				if (header == null) {
						throw new RuntimeException("RequestMediatorHandler found null header on incomming message.");
					}
				// Get REQUEST_ID from header
				requestIdName = envelope.createName("REQUESTID", "requestId", "http://rqsId");
				requestIdIterator = header.getChildElements(requestIdName);
				if (!requestIdIterator.hasNext()) {
					throw new RuntimeException("RequestMediatorHandler did not find a text node on requestId header element.");
				}
				requestIdElement = (SOAPElement)requestIdIterator.next();
				requestId = requestIdElement.getValue();
				// Make it available to Mediator app.
				smc.put(REQUEST_ID, requestId);
				smc.setScope(REQUEST_ID, Scope.APPLICATION);
			}
		} catch (SOAPException sE) {
			throw new RuntimeException(sE.getMessage());
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
