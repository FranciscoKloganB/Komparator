package org.komparator.security.handler;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.security.*;
import javax.xml.soap.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.komparator.security.CryptoUtil;
import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

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
public class CCMediatorClientHandler implements SOAPHandler<SOAPMessageContext> {
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
		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();

		try {

			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPBody body = envelope.getBody();
			SOAPHeader header = envelope.getHeader();
			Name wsName_name = envelope.createName("ENDPOINT_NAME", "card", "http://cc");
			QName operation_name = (QName)smc.get(MessageContext.WSDL_OPERATION);
			String requestedOperation = operation_name.getLocalPart();
			String secret;
			byte[] data;

			if (!requestedOperation.equals("buyCart")) {
				return true;
			}

			if (isOutbound) {
				if (header == null)
					header = envelope.addHeader();
				// Sets the name of the endpoint of this outgoing message on the header
				String wsName = (String) smc.get(ENDPOINT_NAME);
				SOAPHeaderElement wsNameElement = header.addHeaderElement(wsName_name);
				wsNameElement.addTextNode(wsName);
				// Use security manager to get a publickey with endpoint name
				PublicKey key = secM.getPublicKeyFromCA(wsName);
				// Get message nodes with relevant arguments
				NodeList children = body.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node argument = children.item(i);
					// If children is sensible being ciphering
					if (argument.getNodeName().equals("creditCardNr")) {
						secret = argument.getTextContent();
						data = CryptoUtil.outputStreamToByteArray(secret);
						data = CryptoUtil.asymCipher(data, key);
						secret = CryptoUtil.byteToString(data);
						argument.setTextContent(secret);
						message.saveChanges();
						return true;
					}
				}
			}
		} catch (Exception e) {
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
