package org.komparator.security.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.domain.XMLPrinter;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {
	/**
	* Gets the header blocks that can be processed by this Handler instance.
	* If null, processes all.
	*/
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	* The handleMessage method is invoked for normal processing of inbound and
	* outbound messages.
	*/
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		logToSystemOut(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		logToSystemOut(smc);
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
	/**
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
	 * outgoing or incoming message. Write a brief message to the print stream
	 * and output the message. The writeTo() method can throw SOAPException or
	 * IOException
	 */
	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		System.out.print("INTERCEPTED ");
		if (outbound)
			System.out.print("OUTBOUND");
		else
			System.out.print(" INBOUND");
		System.out.println(" SOAP message: ");
		SOAPMessage message = smc.getMessage();
		try {
			ByteArrayOutputStream bOS = new ByteArrayOutputStream();
			message.writeTo(bOS);
			String unformattedXML = bOS.toString();
			String formattedXML = XMLPrinter.printXmlFromString(unformattedXML);
			System.out.println(formattedXML);
		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		}
	}

}
