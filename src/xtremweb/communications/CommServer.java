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

package xtremweb.communications;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.server.Handler;

import xtremweb.common.Logger;
import xtremweb.common.XWConfigurator;
import xtremweb.common.XWPropertyDefs;

/**
 * This is the communications server part. This listens to requests.
 * 
 * @author Oleg Lodygensky
 * @since RPCXW
 * @see CommHandler
 */
public abstract class CommServer extends Thread {

	/**
	 * This is the logger
	 */
	private Logger logger;
	/**
	 * This is the communication handler
	 */
	private Handler handler;
	/**
	 * This is the port number
	 */
	private int port;
	/**
	 * This is the client host name; for debug purposes only
	 */
	private String remoteName;
	/**
	 * This is the client IP addr; for debug purposes only
	 */
	private String remoteIP;
	/**
	 * This is the client port; for debug purposes only
	 */
	private int remotePort;
	/**
	 * This is the maximum simultaneous connections
	 */
	private int MAXCONNECTIONS;
	/**
	 * This holds the handlers
	 */
	private List connPool;
	/**
	 * This is the current amount of simultaneous connections
	 * 
	 * @since 5.8.0
	 */
	private int nbConnections;
	/**
	 * This is the configuration
	 */
	private XWConfigurator config;

	/**
	 ** This is the only constructor
	 */
	protected CommServer(String n) {
		super(n);
		setLogger(new Logger(this));
		MAXCONNECTIONS = 0;
		nbConnections = 0;
	}

	/**
	 * This initializes communications
	 * 
	 * @param p
	 *            is a Properties object to retrieve configuration (ports...)
	 * @param h
	 *            is a CommHandler object to handle communications
	 */
	protected void initComm(XWConfigurator p, Handler h) throws RemoteException {

		setHandler(h);
		setConfig(p);
		MAXCONNECTIONS = p.getInt(XWPropertyDefs.MAXCONNECTIONS);
		connPool = Collections.synchronizedList(new LinkedList());
		getLogger().config("MAXCONNECTIONS = " + MAXCONNECTIONS);
		getLogger()
				.finest("" + this + " CommServer#initComm() " + getHandler());

		try {
			for (int i = 0; i < MAXCONNECTIONS; i++) {
				final CommHandler commHandler = (CommHandler) getHandler()
						.getClass().newInstance();
				if (commHandler != null) {
					commHandler.setCommServer(this);
					commHandler.start();
					pushConnection(commHandler);
				}
			}
		} catch (final Exception e) {
			getLogger().exception(e);
			getLogger().fatal(e.toString());
		}
	}

	protected String remoteAddresse() {
		return new String("{" + getRemoteName() + "/" + getRemoteIP() + ":"
				+ getRemotePort() + "}");
	}

	protected String msgWithRemoteAddresse(String msg) {
		return remoteAddresse() + " : " + msg;
	}

	/**
	 * This creates a new communication handler
	 */
	private Handler getConnection() throws RemoteException,
			InstantiationException, IllegalAccessException {

		final Handler commHandler = getHandler().getClass().newInstance();
		return commHandler;
	}

	/**
	 * This waits until nbConnections &lt; MAXCONNECTIONS; then this increments
	 * nbConnections and create a new Connection
	 * 
	 * @see #nbConnections
	 */
	protected synchronized CommHandler popConnection() throws RemoteException,
			InstantiationException, IllegalAccessException {

		while (connPool.size() <= 0) {
			try {
				getLogger()
						.debug(msgWithRemoteAddresse("popConnection sleeping ("
								+ nbConnections + " > " + MAXCONNECTIONS + ")"));
				wait();
				getLogger().debug(
						msgWithRemoteAddresse("popConnection woken up"));
			} catch (final InterruptedException e) {
			}
		}
		final CommHandler myhandler = (CommHandler) connPool.remove(0);
		nbConnections++;
		getLogger().debug(
				msgWithRemoteAddresse("popConnection " + nbConnections + " ("
						+ ((Thread) myhandler).getId() + ")"));
		notifyAll();
		return myhandler;
	}

	/**
	 * This decrements nbConnections
	 * 
	 * @see #nbConnections
	 */
	public synchronized void pushConnection(CommHandler h) {
		connPool.add(h);
		nbConnections--;
		if (nbConnections < 0) {
			nbConnections = 0;
		}
		getLogger().debug(
				msgWithRemoteAddresse("pushConnection " + nbConnections + " ("
						+ ((Thread) h).getId() + ")"));
		notifyAll();
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger
	 *            the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 *            the handler to set
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * @return the config
	 */
	public XWConfigurator getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(XWConfigurator config) {
		this.config = config;
	}

	/**
	 * @return the remoteName
	 */
	public String getRemoteName() {
		return remoteName;
	}

	/**
	 * @param remoteName
	 *            the remoteName to set
	 */
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}

	/**
	 * @return the remoteIP
	 */
	public String getRemoteIP() {
		return remoteIP;
	}

	/**
	 * @param remoteIP
	 *            the remoteIP to set
	 */
	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	/**
	 * @return the remotePort
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * @param remotePort
	 *            the remotePort to set
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
}
