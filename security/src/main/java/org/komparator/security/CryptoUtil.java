package org.komparator.security;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.security.*;
import javax.xml.soap.*;
import javax.xml.namespace.QName;
import java.security.cert.Certificate;
import java.nio.charset.StandardCharsets;
import javax.xml.ws.handler.MessageContext;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.cert.CertificateException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class CryptoUtil {
	private static final int CIPHERMODE = 0;
	private static final int DECIPHERMODE = 1;
	private static final int ASYM_KEY_SIZE = 2048;
	private static final String ASYM_ALGO = "RSA";
	private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";

	/*****************************************************************************

			PUBLIC KEY GETTER METHODS

	*****************************************************************************/

	/** Returns the public key from a certificate. */
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}

	/** Returns the public key from a local file resource */
	public static PublicKey getPublicKeyFromCertificateResource(String resourcePath) throws IOException, CertificateException {
		Certificate certificate = getX509CertificateFromResource(resourcePath);
		return getPublicKeyFromCertificate(certificate);
	}

	/*****************************************************************************

			PRIVATE KEY GETTER METHODS

	*****************************************************************************/

	/** Reads a PrivateKey from a key-store. */
	public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
		throws KeyStoreException, UnrecoverableKeyException {
		PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		}
		return key;
	}

	/** Reads a PrivateKey from a key-store resource. */
	public static PrivateKey getPrivateKeyFromKeyStoreResource(String keyStoreResourcePath, char[] keyStorePassword, String keyAlias, char[] keyPassword)
		throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromResource(keyStoreResourcePath, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	/** Reads a PrivateKey from a key store in given file path. */
	public static PrivateKey getPrivateKeyFromKeyStoreFile(String keyStoreFilePath, char[] keyStorePassword, String keyAlias, char[] keyPassword)
		throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		return getPrivateKeyFromKeyStoreFile(new File(keyStoreFilePath), keyStorePassword, keyAlias, keyPassword);
	}

	/** Reads a PrivateKey from a key-store file. */
	public static PrivateKey getPrivateKeyFromKeyStoreFile(File keyStoreFile, char[] keyStorePassword, String keyAlias, char[] keyPassword)
		throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromFile(keyStoreFile, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	/*****************************************************************************

				CERTIFICATE METHODS: BYTES, RESOURCE, STREAM

	*****************************************************************************/

	/** Returns a Certificate object given a string with a certificate in the PEM format. */
	public static Certificate getX509CertificateFromPEMString(String certificateString) throws CertificateException {
		byte[] bytes = certificateString.getBytes(StandardCharsets.UTF_8);
		return getX509CertificateFromBytes(bytes);
	}

	/** Converts a byte array to a Certificate object. Returns null if the bytes do not correspond to a certificate. */
	public static Certificate getX509CertificateFromBytes(byte[] bytes) throws CertificateException {
		InputStream is = new ByteArrayInputStream(bytes);
		return getX509CertificateFromStream(is);
	}

	/** Reads a certificate from a resource (included in the application package). Ideal for private keys */
	public static Certificate getX509CertificateFromResource(String certificateResourcePath) throws IOException, CertificateException {
		InputStream is = getResourceAsStream(certificateResourcePath);
		return getX509CertificateFromStream(is);
	}

	/** Reads a certificate from a file path. Calls for an overloaded version of this method */
	public static Certificate getX509CertificateFromFile(String certificateFilePath) throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificateFromFile(certificateFile);
	}

	/** Reads a certificate from a file. */
	public static Certificate getX509CertificateFromFile(File certificateFile) throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		return getX509CertificateFromStream(fis);
	}

	/** Gets a Certificate object from an input stream. It's called by all the above methods */
	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}

	/*****************************************************************************

				COLLECTION OF CERTIFICATES: FILE

	*****************************************************************************/

	/** Reads a collection of certificates from a file path. */
	public static Collection<Certificate> getX509CertificatesFromFile(String certificateFilePath) throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificatesFromFile(certificateFile);
	}

	/** Reads a collection of certificates from a file. */
	public static Collection<Certificate> getX509CertificatesFromFile(File certificateFile) throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		@SuppressWarnings("unchecked")
		Collection<Certificate> c = (Collection<Certificate>) cf.generateCertificates(fis);
		return c;
	}

	/*****************************************************************************

				KEYSTORE METHODS: BYTES, RESOURCE, STREAM

	*****************************************************************************/

	/** Reads a KeyStore from a resource in application package. */
	public static KeyStore readKeystoreFromResource(String keyStoreResourcePath, char[] keyStorePassword) throws KeyStoreException {
		InputStream is = getResourceAsStream(keyStoreResourcePath);
		return readKeystoreFromStream(is, keyStorePassword);
	}

	/** Reads a KeyStore from a file path. */
	public static KeyStore readKeystoreFromFile(String keyStoreFilePath, char[] keyStorePassword) throws FileNotFoundException, KeyStoreException {
		return readKeystoreFromFile(new File(keyStoreFilePath), keyStorePassword);
	}

	/** Reads a KeyStore from a file. */
	private static KeyStore readKeystoreFromFile(File keyStoreFile, char[] keyStorePassword) throws FileNotFoundException, KeyStoreException {
		FileInputStream fis = new FileInputStream(keyStoreFile);
		return readKeystoreFromStream(fis, keyStorePassword);
	}

	/** Reads a KeyStore from a stream. */
	private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword) throws KeyStoreException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
			keystore.load(keyStoreInputStream, keyStorePassword);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeyStoreException("Could not load key store", e);
		} finally {
			closeStream(keyStoreInputStream);
		}
		return keystore;
	}

	/*****************************************************************************

				CIPHER AND DECIPHER METHODS

	*****************************************************************************/

	/** Ciphers the given data with the given key */
	public static byte[] asymCipher(byte[] data, PublicKey key) throws InvalidKeyException, Exception {
		Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipheredBytes = cipher.doFinal(data);
		return cipheredBytes;
	}

	/** Receives chipered data and a key and returns deciphered data */
	public static byte[] asymDecipher(byte[] data, PrivateKey key) throws InvalidKeyException, Exception {
		Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipheredBytes = cipher.doFinal(data);
		return cipheredBytes;
	}

	/*****************************************************************************

				MESSAGE SIGNATURE METHODS

	*****************************************************************************/

	/** Signs the input bytes with the private key and returns the bytes. If anything goes wrong, null is returned (swallows exceptions). */
	public static byte[] makeDigitalSignature(final String signatureMethod, final PrivateKey privateKey, final byte[] bytesToSign) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initSign(privateKey);
			sig.update(bytesToSign);
			byte[] signatureResult = sig.sign();
			return signatureResult;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return null;
		}
	}

	/** Verify signature of bytes with the public key contained in the certificate. If anything goes wrong, returns false (swallows exceptions). */
	public static boolean verifyDigitalSignature(final String signatureMethod, Certificate publicKeyCertificate, byte[] bytesToVerify, byte[] signature) {
		return verifyDigitalSignature(signatureMethod, publicKeyCertificate.getPublicKey(), bytesToVerify, signature);
	}

	/** Verify signature of bytes with the public key. If anything goes wrong, returns false (swallows exceptions). */
	public static boolean verifyDigitalSignature(final String signatureMethod, PublicKey publicKey, byte[] bytesToVerify, byte[] signature) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initVerify(publicKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return false;
		}
	}

	/** Checks if the certificate was properly signed by the CA with the provided public key. */
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			return false;
		}
		return true;
	}

	/** Checks if the certificate was properly signed by the Certificate Authority with the provided certificate. */
	public static boolean verifySignedCertificate(Certificate certificate, Certificate caCertificate) {
		return verifySignedCertificate(certificate, caCertificate.getPublicKey());
	}

	/*****************************************************************************

				HELPER METHODS

	*****************************************************************************/
	/** Transforms a base 64 String into an array of byte[] */
	public static byte[] stringToByte(String data) {
		return parseBase64Binary(data);
	}

	/** Turns outputStream into a byte array */
	public static byte[] outputStreamToByteArray(String secret) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(CryptoUtil.stringToByte(secret));
		return bos.toByteArray();
	}

	/** Transforms an array of bytes into a String object with plain text */
	public static String byteToString(byte[] data) {
		return printBase64Binary(data);
	}

	/** Transforms a SOAPMessage into a bite[] */
	public static byte[] messageToByte(SOAPMessage message) throws SOAPException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		message.writeTo(bos);
		return bos.toByteArray();
	}

	/** Method used to access resource. */
	private static InputStream getResourceAsStream(String resourcePath) {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		return is;
	}

	/** Do the best effort to close the stream, but ignore exceptions. */
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// ignore
		}
	}

}
