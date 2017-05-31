package org.komparator.security.handler;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.security.*;
import javax.xml.soap.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

import java.lang.RuntimeException;

import org.komparator.security.domain.SecurityManager;

	/**
 * This SOAPHandler digitally signs an outbound message from supplier-cli to
 * supplier. If it is inbound and supplier-ws, it checks signature.
 */
public class SignatureSupplierClientHandler implements SOAPHandler<SOAPMessageContext> {
	public static final String ENDPOINT_NAME = "EndpointName";
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	private SecurityManager secM = SecurityManager.getInstance();

	/** Gets the header blocks that can be processed by this Handler instance. */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/** This method is invoked for normal processing of inbound and outbound messages. */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {

		SOAPHeaderElement requestorHeaderElement, serverHeaderElement, signatureHeaderElement;
		SOAPElement requestorElement, serverElement, signatureElement;
		String requestor, server, signature, alias, resourcePath, keyStoreResourcePath;
		Name requestorName, serverName, signatureName;
		byte[] signatureData, simpleData;
		char[] password;
		Iterator requestorIterator, serverIterator, signatureIterator;
		PrivateKey privKey;
		PublicKey pubKey;

		Boolean outbound = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		boolean isOutbound = outbound.booleanValue();

		try {
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();

			if (isOutbound) {
				if (header == null) {
					throw new RuntimeException("SignatureSupplierClientHandler recieved an empty header after FreshnessHandler Interception.");
				}
				requestor = secM.getEntityName();
				requestorName = envelope.createName("REQUESTORNAME", "signature", "http://sgn");
				requestorHeaderElement = header.addHeaderElement(requestorName);
				requestorHeaderElement.addTextNode(requestor);

				resourcePath = new String("src/main/resources/" + requestor + ".jks");
				alias = new String(requestor);
				alias = alias.toLowerCase();
				password = secM.getPassword();

				privKey = CryptoUtil.getPrivateKeyFromKeyStoreResource("T64_Mediator.jks", password, alias, password);

				simpleData = CryptoUtil.messageToByte(message);
				signatureData = CryptoUtil.makeDigitalSignature(SIGNATURE_ALGORITHM, privKey, simpleData);
				if (signatureData == null) {
					System.out.println(":: Null signatureData found ::");
					throw new RuntimeException("Could not generate a signature requestor's signature.");
				}
				signature = CryptoUtil.byteToString(signatureData);
				if (signature == null) {
					System.out.println(":: Null signature found ::");
					throw new RuntimeException("Could not convert signatureData to String base64.");
				}

				signatureName = envelope.createName("REQUESTORSIGNATURE", "signature", "http://sgn");
				signatureHeaderElement = header.addHeaderElement(signatureName);
				signatureHeaderElement.addTextNode(signature);

				return true;
			}

			if (!isOutbound) {
				if (header == null) {
					throw new RuntimeException("SignatureSupplierHandler found inbound null Header.");
				}

				serverName = envelope.createName("SERVLETNAME", "signature", "http://sgn");
				serverIterator = header.getChildElements(serverName);
				if (!serverIterator.hasNext()) {
					throw new RuntimeException("SignatureSupplierHandler found no service name comming from Supplier server..");
				}
				serverElement = (SOAPElement)serverIterator.next();
				server = serverElement.getValue();

				signatureName = envelope.createName("SERVLETSIGNATURE", "signature", "http://sgn");
				signatureIterator = header.getChildElements(signatureName);
				if (!signatureIterator.hasNext()) {
					throw new RuntimeException("SignatureSupplierHandler found no signature text node comming from Supplier server.");
				}
				signatureElement = (SOAPElement)signatureIterator.next();
				signature = signatureElement.getValue();
				signatureElement.detachNode();

				signatureData = CryptoUtil.stringToByte(signature);
				pubKey = secM.getPublicKeyFromCA(server);
				if (pubKey == null) {
					throw new RuntimeException("Certificate authority did not provide this key. An attack to communication might have occurred.");
				}
				simpleData = CryptoUtil.messageToByte(message);
				if (!CryptoUtil.verifyDigitalSignature(SIGNATURE_ALGORITHM, pubKey, simpleData, signatureData)) {
					throw new RuntimeException("Supplier signature was not aproved by supplier client handler");
				}
				return true;
			}
			// catch (SOAPException | IOException | CertificateException | KeyStoreException | UnrecoverableKeyException e)
			// catch (SOAPException | IOException | CertificateException e)
		} catch (SOAPException | IOException | CertificateException | KeyStoreException | UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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
