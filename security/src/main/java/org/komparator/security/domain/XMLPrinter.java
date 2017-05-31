package org.komparator.security.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public final class XMLPrinter {

	private XMLPrinter(){}

	private static class XMLPrinterHolder {
		private static final XMLPrinter INSTANCE = new XMLPrinter();
	}

	public static synchronized XMLPrinter getInstance() {
		return XMLPrinterHolder.INSTANCE;
	}

	public static Document documentFromString(String stringToFormat) throws Exception {
		DocumentBuilderFactory documentBuilderFactory;
		DocumentBuilder documentBuilder;
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setIgnoringComments(true);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		return documentBuilder.parse(new InputSource(new StringReader(stringToFormat)));
	}

	public static String prettyPrintXml(Document xml) throws Exception {
		DOMSource domSource = new DOMSource(xml);
		Writer outPutWritter = new StringWriter();
		StreamResult streamResult = new StreamResult(outPutWritter);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
		transformer.transform(domSource,streamResult);
		return outPutWritter.toString();
	}

	public static String prettyPrintSoapMessage(SOAPMessage sm) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
			Source soapContent = sm.getSOAPPart().getContent();
			ByteArrayOutputStream bOS = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(bOS);
			transformer.transform(soapContent, result);
			return bOS.toString();
		} catch (Exception e) {
			return "Could not prettyPrintSoapMessage...";
		}
	}

	public static String printXmlFromString(String stringToFormat) {
		try {
			return prettyPrintXml(documentFromString(stringToFormat));
		} catch (Exception e) {
			return "Could not pretyPrintXmlFromString...";
		}
	}

}
