package org.komparator.security.domain;

import java.util.*;
import java.io.*;
import javax.crypto.*;
import javax.xml.soap.*;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.DateTimeException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.UnsupportedTemporalTypeException;
import org.komparator.security.CryptoUtil;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/** Domain Root. */
public class SecurityManager {
  private CAClient caClient;
  private String entityName = "none";
  private final List<String> answeredRequests = new Vector<String>();
  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
  private final String caURL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL";
  private final String password = "YwM1zPUR";

  /**
  * Instantiates a new SecurityManager object belonging to entity with no name;
  * Security manager can be used only for abstraction.
  * However, if an instance of SecurityManager has an owner, meaning that the
  * entity running on a local machine has given it's name to the it's security
  * manager, then it will be possible to make use of such information during
  * soap message exchanges. See setEntityName(String name).
  */
	private SecurityManager() {
    try {
      this.caClient = new CAClient(caURL);
    } catch (CAClientException caCE) {
      System.out.println("Could not initiate a certificate authority client.");
    }
  }

  /** Makes constructor of this class not directly accessible */
	private static class SecurityManagerHolder {
		private static final SecurityManager INSTANCE = new SecurityManager();
	}

  /** Upon invocation returns the singleton instance of security manager to the caller */
	public static synchronized SecurityManager getInstance() {
		return SecurityManagerHolder.INSTANCE;
	}

  /** Sets the owner of a SecurityManager instance on local machine */
  public void setEntityName(String name) {
    entityName = name;
  }

  /**
  * Returns the name of the service of the local machine in which this
  * singleton instance of SecurityManager is running.
  */
  public String getEntityName() {
    return entityName;
  }

  public char[] getPassword() {
    return password.toCharArray();
  }

  /**
  * Computes the SHA-1 hash over a true-random seed value concatenated with a
  * 64-bit counter which is incremented by 1 for each operation.
  * This method is used to generate a random secure number string, in order to
  * avoid attack by repetition. Client outgoing messages place one of these in
  * their headers.
  */
  public String generateSecureNumber() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    final byte array[] = new byte[32];
    random.nextBytes(array);
    return printHexBinary(array);
  }

  /**
  * Creates a new string with the time and date taken from system at the moment
  * method instantiated Date object. Remember there is no global clock.
  */
  public String generateTimeStamp() {
    return dateFormatter.format(new Date());
  }

  /** Helper method that calculates the time passed between two instants */
  private long timeDifferential(String t1, String t2) {
    try {
      Instant firstInstant = dateFormatter.parse(t1).toInstant();
      Instant secondInstant = dateFormatter.parse(t2).toInstant();
      return firstInstant.until(secondInstant, ChronoUnit.valueOf("SECONDS"));
    } catch (ParseException pE) {
      System.out.println("Error parsing incomming message time stamp.");
    } catch (DateTimeException dTE) {
      System.out.println(dTE.getMessage());
    }
    return -1;
  }

  /** This method decides if a message should be answered based on freshness */
  public boolean freshMessage(String t1, String t2) {
    long diff = timeDifferential(t1, t2);
    if (0 <= diff && diff <= 3) {
      return true;
    } else {
      return false;
    }
  }

  /** This method verifies if secondary mediator should become primary. */
  public boolean primaryMediatorAlive(String t1, String t2) {
    long diff = timeDifferential(t1, t2);
    if (0 <= diff && diff <= 5) {
      return true;
    } else {
      return false;
    }
  }

  /** This method if a message should be answered based on previous answers */
  public boolean shouldAnswer(String requestId) {
    if (answeredRequests.contains(requestId))
      return false;
    answeredRequests.add(requestId);
    return true;
  }

  /**
  * Mediator client uses this method to obtain a key from a certificate authority
  * By doing this it is possible to return null should the verification on the
  * invocation of getCertificateFromCA return null aswell, allowing handlers to
  * to throw run time exception in some attack attempt.
  */
  public PublicKey getPublicKeyFromCA(String wsName) throws CertificateException, IOException {
    Certificate certificate = getCertificateFromCA(wsName);
    if (certificate == null)
      return null;
    return CryptoUtil.getPublicKeyFromCertificate(certificate);
  }

  /**
  * Private method called by getPublicKeyFromCA, that fetches a certificate from
  * certificate authority CA and checks if the certificate was not given by some
  * other entity.
  */
  private Certificate getCertificateFromCA(String certificateName) throws CertificateException, IOException {
    String certificateS = caClient.getCertificate(certificateName);
    Certificate certificate = CryptoUtil.getX509CertificateFromPEMString(certificateS);
    Certificate caCertificate = CryptoUtil.getX509CertificateFromResource("ca.cer");
    if (CryptoUtil.verifySignedCertificate(certificate, caCertificate)) {
      return certificate;
    }
    return null;
  }
}
