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

public class RequestIdMediatorClientHandler implements SOAPHandler<SOAPMessageContext> {
	private SecurityManager secM = SecurityManager.getInstance();
	public static final String REQUEST_ID = "requestIdentifier";


	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		String requestId, requestedOperation;
		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();
		Name requestIdName;
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

			if (isOutbound) {
				if (header == null) {
						header = envelope.addHeader();
					}
				// Get REQUEST_ID from request context then place it on the SOAPMessage Header.
				requestId = (String)smc.get(REQUEST_ID);
				requestIdName = envelope.createName("REQUESTID", "requestId", "http://rqsId");
				requestIdHeaderElement = header.addHeaderElement(requestIdName);
				requestIdHeaderElement.addTextNode(requestId);
				return true;
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
