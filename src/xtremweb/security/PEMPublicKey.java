/*
 * Copyrights     : CNRS
 * Author         : Oleg Lodygensky
 * Acknowledgment : XtremWeb-HEP is based on XtremWeb 1.8.0 by inria : http://www.xtremweb.net/
 * Web            : http://www.xtremweb-hep.org
 * 
 *      This file is part of XtremWeb-HEP.
 *
 *    XtremWeb-HEP is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    XtremWeb-HEP is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with XtremWeb-HEP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package xtremweb.security;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;

import xtremweb.common.Logger;

/**
 * This reads X509 public key from PEM files
 * 
 * @since 7.4.0
 */
public class PEMPublicKey {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	/**
	 * This is the certifiate read from file
	 */
	private X509CertificateObject certificate;
	/**
	 * This retrieves this public key certificate
	 */
	public X509Certificate getCertificate() {
		return certificate;
	}

	/**
	 * This does nothing
	 */
	public PEMPublicKey() {
		certificate = null;
	}

	/**
	 * This reads public key from file
	 * 
	 * @param keyPath
	 *            is the public key file path
	 */
	public PublicKey read(String keyPath) throws CertificateException,
	FileNotFoundException, IOException {

		File f = null;
		try {
			f = new File(keyPath);
			return read(f);
		} finally {
			f = null;
		}
	}

	/**
	 * This reads public key from file
	 * 
	 * @param keyFile
	 *            is the public key file path
	 */
	public PublicKey read(File keyFile) throws CertificateException,
	FileNotFoundException, IOException {

		FileReader fr = null;
		try {
			fr = new FileReader(keyFile);
			return read(fr);
		} catch (final ClassCastException e) {
			throw new CertificateException(e);
		} finally {
			try {
				fr.close();
			} catch (final Exception ignore) {
			}
			fr = null;
		}
	}

	/**
	 * This reads public key from file
	 * 
	 * @param reader
	 *            is the reader to read key from
	 * @exception CertificateException
	 *                on certificate format or validity error
	 */
	public PublicKey read(Reader reader) throws CertificateException,
	FileNotFoundException, IOException {

		PEMReader r = null;
		try {
			r = new PEMReader(reader);
			certificate = (X509CertificateObject) r.readObject();
			if (certificate == null) {
				throw new CertificateException("invalid certificate file");
			}
			certificate.checkValidity();
			return certificate.getPublicKey();
		} catch (final ClassCastException e) {
			throw new CertificateException(e);
		} finally {
			try {
				r.close();
			} catch (final Exception ignore) {
			}
			r = null;
		}
	}

	/**
	 * This retrieves the subject of the most trusted cert of this proxy
	 */
	public X500Principal getSubject() {
		if (certificate == null) {
			return null;
		}
		return certificate.getSubjectX500Principal();
	}

	/**
	 * This retrieves the subject distinguished name of this proxy
	 * 
	 * @see #getSubject()
	 */
	public String getSubjectName() {
		X500Principal subject = getSubject();
		if (subject == null) {
			return null;
		}
		final String ret = subject.getName();
		subject = null;
		return ret;
	}

	/**
	 * This retrieves the subject of the most trusted cert of this proxy
	 */
	public X500Principal getIssuer() {
		if (certificate == null) {
			return null;
		}
		return certificate.getIssuerX500Principal();
	}

	/**
	 * This retrieves the subject distinguished name of this proxy
	 * 
	 * @see #getSubject()
	 */
	public String getIssuerName() {
		X500Principal issuer = getIssuer();
		if (issuer == null) {
			return null;
		}
		final String ret = issuer.getName();
		issuer = null;
		return ret;
	}

	/**
	 * This is for testing only
	 */
	public boolean authenticate(InputStream inStream, PublicKey publicKey)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException,
			SignatureException {

		final DataInputStream in = new DataInputStream(inStream);

		final String user = in.readUTF();
		final long t = in.readLong();
		final double q = in.readDouble();
		final int length = in.readInt();
		final byte[] signature = new byte[length];
		in.readFully(signature);

		final Signature s = Signature.getInstance("SHA256WithRSAEncryption");
		s.initVerify(publicKey);
		s.update(Protection.makeBytes(t, q));
		return s.verify(signature);
	}

	/**
	 * This is for testing only
	 */
	public static void main(String[] args) throws Exception {

		final Logger logger = new Logger();

		if (args.length < 1) {
			logger.fatal("Usage : PublicKey file [--server]\n " +
					"Where : file is the public key file\n" +
					"        --server to accept connection from localhost:79999 for testing\n" +
					"        (then you can start PrivateKeyReader to connect - see PrivateKeyReader)");
		}
		final PEMPublicKey reader = new PEMPublicKey();
		final PublicKey publicKey = reader.read(args[0]);

		try {
			final X509ProxyValidator validator = new X509ProxyValidator();
			try {
				validator.validate(reader.certificate);
				logger.info("PEMPublic key validated against CA cert path :)");
			} catch (final Exception e) {
				logger.exception("Can't validate", e);
			}
		} catch (final Exception e) {
			logger.exception("Can't create ProxyValidator", e);
		}

		if (args.length > 1) {

			logger.info("Accepting on port 7999");

			final int port = 7999;
			final ServerSocket s = new ServerSocket(port);
			final Socket client = s.accept();
			
			logger.info("Client logged in = " + reader.authenticate(client.getInputStream(), publicKey));

			s.close();
		}
	}
}
