package org.komparator.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.*;
import org.junit.*;

import static org.junit.Assert.*;
import javax.crypto.Cipher;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import org.komparator.security.CryptoUtil;
import java.security.cert.CertificateException;


import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class CryptoUtilTest {
  private static final int ASYM_KEY_SIZE = 2048;
  private static final String ASYM_ALGO = "RSA";
  private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";
  private static final String ccNumber = "4556648855991861";

  // one-time initialization and clean-up
  @BeforeClass
  public static void oneTimeSetUp() {}
  @AfterClass
  public static void oneTimeTearDown() {}
  // initialization and clean-up for each test
  @Before
  public void setUp() {}
  @After
  public void tearDown() {}
    //stringToCharArray
  /** Tests if credit card number is equal before and after convertion */
  @Test
  public void testCreditCardConvertionAndDeconvertion() throws Exception {
    byte[] ccNumberBytes;
    String auxString = "default_value";
    // Obtain mediator's RSA key pair
    PublicKey publicKey = CryptoUtil.getPublicKeyFromCertificateResource("example.cer");
    PrivateKey privateKey = CryptoUtil.getPrivateKeyFromKeyStoreResource("example.jks", "1nsecure".toCharArray(), "example", "ins3cur3".toCharArray());
    // Print credit card information in String and Binary formats before starting covertion
    ccNumberBytes = CryptoUtil.stringToByte(ccNumber);
    System.out.println("Credit card number before cipher: " + ccNumber);
    System.out.println("ccNumberBytes before cipher: " + printHexBinary(ccNumberBytes));
    // Cipher process
    ccNumberBytes = CryptoUtil.asymCipher(ccNumberBytes, publicKey);
    auxString = CryptoUtil.byteToString(ccNumberBytes);
    System.out.println("ccNumberBytes after cipher: " + printHexBinary(ccNumberBytes));
    System.out.println("Credit card number after cipher: " + auxString);
    // Test if credit card number was actually encrypted with public key.
    if(ccNumber.equals(auxString)) {
      fail("Convertion did not happen. Expected different Strings after ciphering!");
    }
    // Convert auxString into ccNumberBytes, cannot use old value and decipher.
    ccNumberBytes = CryptoUtil.stringToByte(auxString);
    ccNumberBytes = CryptoUtil.asymDecipher(ccNumberBytes, privateKey);
    auxString = CryptoUtil.byteToString(ccNumberBytes);
    System.out.println("ccNumberBytes after decipher: " + printHexBinary(ccNumberBytes));
    System.out.println("Started with this credit card number: " + ccNumber + ", ended with: " + auxString);
    // Finally test if credit card values ended up being the same.
    assertEquals(ccNumber, auxString);
  }

}
