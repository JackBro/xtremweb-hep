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

package xtremweb.common;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.ResultSet;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xtremweb.communications.URI;
import xtremweb.database.SQLRequest;
import xtremweb.security.XWAccessRights;

/**
 * WorkInterface.java
 *
 * Created: Feb 19th, 2002
 *
 * @author <a href="mailto:lodygens /at\ .in2p3.fr>Oleg Lodygensky</a>
 * @version %I%, %G%
 */

/**
 * This describes an XtremWeb work. <br />
 * This describes a row of the works SQL table on server side.<br />
 * This aims to transfer work through the network too.
 */
public class WorkInterface extends Table {
	/**
	 * This is the database table name This was stored in
	 * xtremweb.dispatcher.Work
	 * 
	 * @since 9.0.0
	 */
	public static final String TABLENAME = ("works");

	/**
	 * This enumerates this interface columns
	 */
	public enum Columns implements XWBaseColumn {

		/**
		 * This is the column index of the user X.509 proxy URI, if any
		 */
		USERPROXY,
		/**
		 * This is the column index of the session UID, if any
		 */
		SESSIONUID {
			/**
			 * This creates a new UID from string
			 */
			@Override
			public UID fromString(String v) {
				return new UID(v);
			}
		},
		/**
		 * This is the column index of the group UID, if any
		 */
		GROUPUID {
			/**
			 * This creates a new UID from string
			 */
			@Override
			public UID fromString(String v) {
				return new UID(v);
			}
		},
		/**
		 * This is the column index of the service grid identifier, if any
		 * 
		 * @since 7.2.0
		 */
		SGID {
			/**
			 * This creates an object from String representation for this column
			 * value This cleans the parameter to ensure SQL compliance
			 * 
			 * @param v
			 *            the String representation
			 * @return a Boolean representing the column value
			 * @throws Exception
			 *             is thrown on instantiation error
			 */
			@Override
			public String fromString(final String v) {
				String val = v;
				val = val.replaceAll("[\\n\\s\'\"]+", "_");
				val = val.replaceAll("&amp;", "&");
				val = val.replaceAll("&", "&amp;");
				return val;
			}
		},
		/**
		 * This is the column index of the worker we want to run this work
		 * 
		 * @since RPCXW
		 */
		EXPECTEDHOSTUID {
			/**
			 * This creates a new UID from string
			 */
			@Override
			public UID fromString(String v) {
				return new UID(v);
			}
		},
		/**
		 * This is the column index of the flag which says whether the job is a
		 * service
		 * 
		 * @since RPCXW
		 */
		ISSERVICE {
			@Override
			public Boolean fromString(String v) {
				return new Boolean(v);
			}
		},
		/**
		 * This is the column index of the label, if any
		 */
		LABEL,
		/**
		 * This is column index of the the application UID
		 */
		APPUID {
			/**
			 * This creates a new UID from string
			 */
			@Override
			public UID fromString(String v) {
				return new UID(v);
			}
		},
		/**
		 * This is the column index of the application return code
		 */
		RETURNCODE {
			@Override
			public Integer fromString(String v) {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of either the server name as found from any
		 * DNS, or the server IP address
		 */
		SERVER,
		/**
		 * This is the column index of a comma separated ports list to listen
		 * to. This is used if the job is a server like application
		 * 
		 * @since 8.0.0 (FG)
		 */
		LISTENPORT,
		/**
		 * This is the column index of a comma separated SmartSockets addresses
		 * list to access the listened port. The order is the LISTENPORT one
		 * This is set only if LISTENPORT != null
		 * 
		 * @since 8.0.0 (FG)
		 */
		SMARTSOCKETADDR,
		/**
		 * This is the column index of a semicolon list containing tuple of
		 * SmartSockets address and local port. This helps a job running on
		 * XWHEP worker side to connect to a server like application running on
		 * XWHEP client side. e.g "Saddr1, port1; Saddr2, port2"
		 * 
		 * @since 8.0.0 (FG)
		 */
		SMARTSOCKETCLIENT,
		/**
		 * This is the column index of the environment variables This is a comma
		 * separated of tuples
		 * 
		 * @since 8.0.0 (FG)
		 */
		ENVVARS,
		/**
		 * This is the column index of the command line, if any
		 */
		CMDLINE {
			/**
			 * This creates an object from String representation for this column
			 * value This cleans the parameter to ensure SQL compliance
			 * 
			 * @param v
			 *            the String representation
			 * @return a Boolean representing the column value
			 * @throws Exception
			 *             is thrown on instantiation error
			 */
			@Override
			public String fromString(final String v) {
				String val = v;
				val = val.replaceAll("[\\n\'\"]+", "_");
				val = val.replaceAll("&amp;", "&");
				val = val.replaceAll("&", "&amp;");
				return val;
			}
		},
		/**
		 * This is the column index of the stdin data URI, if any If not set,
		 * this is replaced by AppInterface#DEFAULTSTDINURI
		 * 
		 * @see AppInterface.Columns#DEFAULTSTDINURI
		 */
		STDINURI {
			@Override
			public URI fromString(String v) throws URISyntaxException {
				return new URI(v);
			}
		},
		/**
		 * This is the column index of the environment data URI, if any If not
		 * set, this is replaced by AppInterface#DEFAULTDIRINURI This is
		 * installed before apps.basedirin to ensure this does not override any
		 * of the apps.basedirin files
		 * 
		 * @see AppInterface.Columns#DEFAULTDIRINURI
		 */
		DIRINURI {
			@Override
			public URI fromString(String v) throws URISyntaxException {
				return new URI(v);
			}
		},
		/**
		 * This is the column index of the result data URI, if any
		 */
		RESULTURI {
			@Override
			public URI fromString(String v) throws URISyntaxException {
				return new URI(v);
			}
		},
		/**
		 * This is the column index of the arrival date
		 */
		ARRIVALDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the completed date
		 */
		COMPLETEDDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the date when the worker has downloaded
		 * this work
		 * 
		 * @since 8.0.0
		 */
		READYDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the date when the worker has downloaded
		 * all this work data
		 * 
		 * @since 8.0.0
		 */
		DATAREADYDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the date when the worker has started this
		 * work
		 * 
		 * @since 8.0.0
		 */
		COMPSTARTDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the date when the worker has completed
		 * this work
		 * 
		 * @since 8.0.0
		 */
		COMPENDDATE {
			@Override
			public Date fromString(String v) {
				return XWTools.getSQLDateTime(v);
			}
		},
		/**
		 * This is the column index of the flag to tell whether this is sent to
		 * client
		 */
		SENDTOCLIENT {
			@Override
			public Boolean fromString(String v) throws URISyntaxException {
				return new Boolean(v);
			}
		},
		/**
		 * This is the column index of the flag to tell whether this is a server
		 * local work or a replicated work
		 */
		LOCAL {
			@Override
			public Boolean fromString(String v) throws URISyntaxException {
				return new Boolean(v);
			}
		},
		/**
		 * This is the column index of the active flag
		 */
		ACTIVE {
			@Override
			public Boolean fromString(String v) throws URISyntaxException {
				return new Boolean(v);
			}
		},
		/**
		 * This is the column index of the amount of expected replicas
		 * No replica, if <= 0
		 * @since 10.0.0
		 */
		REPLICATIONS {
			@Override
			public Integer fromString(String v) throws URISyntaxException {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the current amount of submitted replica
		 * @since 10.0.0
		 */
		TOTALR {
			@Override
			public Integer fromString(String v) throws URISyntaxException {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the size of the replica set (how many replica can be computed simultaneously)
		 * @since 10.0.0
		 */
		SIZER {
			@Override
			public Integer fromString(String v) throws URISyntaxException {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of the UID of the original work
		 * @since 10.0.0
		 */
		REPLICATEDUID {
			/**
			 * This creates a new UID from string
			 */
			@Override
			public UID fromString(String v) {
				return new UID(v);
			}
		},
		/**
		 * This is the column index of the UID of the data to drive
		 * @since 10.0.0
		 */
		DATADRIVENURI {
			/**
			 * This creates a new UID from string
			 * @throws URISyntaxException 
			 */
			@Override
			public URI fromString(String v) throws URISyntaxException {
				return new URI(v);
			}
		},
		/**
		 * This is the column index of the max retry
		 */
		MAXRETRY {
			@Override
			public Integer fromString(String v) {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of the wall clock time in seconds
		 * 
		 * @since 8.2.0
		 */
		MAXWALLCLOCKTIME {
			@Override
			public Long fromString(String v) {
				return new Long(v);
			}
		},
		/**
		 * This is the column index of the retry
		 * 
		 * @since 8.0.0
		 */
		RETRY {
			@Override
			public Integer fromString(String v) {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of the needed disk space to run job This is
		 * used when launching a VM; this is in Mb
		 * 
		 * @since 8.0.0
		 */
		DISKSPACE {
			@Override
			public Long fromString(String v) {
				return Long.valueOf(v);
			}
		},
		/**
		 * This is the column index of the minimum memory needed to run job for
		 * this application. This is in Kb
		 */
		MINMEMORY {
			@Override
			public Integer fromString(String v) {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of the minimum CPU spped needed to run job
		 * for this application<br />
		 */
		MINCPUSPEED {
			@Override
			public Integer fromString(String v) {
				return Integer.valueOf(v);
			}
		},
		/**
		 * This is the column index of the status
		 * 
		 * @see StatusEnum
		 */
		STATUS {
			/**
			 * This creates an object from String representation for this column
			 * value
			 * 
			 * @param v
			 *            the String representation
			 * @return an XWStatus representing the column value
			 * @throws Exception
			 *             is thrown on instantiation error
			 */
			@Override
			public StatusEnum fromString(String v) {
				return StatusEnum.valueOf(v.toUpperCase());
			}
		},
		/**
		 * This is the column index of the minimal free mass storage needed by
		 * the application. This is in MegaBytes
		 * 
		 * @since 9.0.0
		 */
		MINFREEMASSSTORAGE {
			/**
			 * This creates an object from String representation for this column
			 * value
			 * 
			 * @param v
			 *            the String representation
			 * @return an Integer representing the column value
			 * @throws Exception
			 *             is thrown on instantiation error
			 */
			@Override
			public Long fromString(String v) {
				return Long.valueOf(v);
			}
		};

		/**
		 * This is the index based on ordinal so that the first value is
		 * TableColumns + 1
		 * 
		 * @see xtremweb.common#TableColumns
		 * @see Enum#ordinal()
		 * @since 8.2.0
		 */
		private int ord;

		/**
		 * This constructor sets the ord member as ord = this.ordinal +
		 * TableColumns.SIZE
		 * 
		 * @since 8.2.0
		 */
		Columns() {
			ord = this.ordinal() + TableColumns.SIZE;
		}

		/**
		 * This retrieves the index based ordinal
		 * 
		 * @return the index based ordinal
		 * @since 8.2.0
		 */
		public int getOrdinal() {
			return ord;
		}

		/**
		 * This creates a new object from String for the given column
		 * 
		 * @param v
		 *            the String representation
		 * @return v
		 */
		public Object fromString(String v) throws Exception {
			return v;
		}

		/**
		 * This creates a new object from SQL result set
		 * 
		 * @param rs
		 *            is the SQL result set
		 * @return the object representing the column
		 * @throws Exception
		 *             is thrown on instantiation error
		 */
		public final Object fromResultSet(ResultSet rs) throws Exception {
			return this.fromString(rs.getString(this.toString()));
		}

		/**
		 * This retrieves an Columns from its integer value
		 * 
		 * @param v
		 *            is the integer value of the Columns
		 * @return an Columns
		 */
		public static XWBaseColumn fromInt(int v)
				throws IndexOutOfBoundsException {
			try {
				return TableColumns.fromInt(v);
			} catch (final Exception e) {
			}
			for (final Columns c : Columns.values()) {
				if (c.getOrdinal() == v) {
					return c;
				}
			}
			throw new IndexOutOfBoundsException(("unvalid Columns value ") + v);
		}
	}

	/**
	 * This is the size, including TableColumns
	 */
	private static final int ENUMSIZE = Columns.values().length;

	/**
	 * This retrieves column label from enum Columns. This takes cares of this
	 * version. If this version is null, this version is prior to 5.8.0. Before
	 * 5.8.0, OWNERUID was known as USERUID
	 * 
	 * @param i
	 *            is an ordinal of an Columns
	 * @since 5.8.0
	 * @return "USERUID" if((version == null) &amp;&amp; (i ==
	 *         OWNERUID.ordinal())); column label otherwise
	 */
	@Override
	public String getColumnLabel(int i) throws IndexOutOfBoundsException {
		try {
			return TableColumns.fromInt(i).toString();
		} catch (final Exception e) {
		}
		return Columns.fromInt(i).toString();
	}

	/**
	 * This is the XML tag
	 */
	public static final String THISTAG = "work";

	/**
	 * This is the default constructor.
	 * There is no replication by default.
	 */
	public WorkInterface() {

		super(THISTAG, TABLENAME);

		setAttributeLength(ENUMSIZE);
		setStatus(StatusEnum.UNAVAILABLE);
		setService(false);
		setSendToClient(false);
		setMaxRetry(Integer.parseInt(XWPropertyDefs.MAXRETRY.defaultValue()));
		setMaxWallClockTime(Long.parseLong(XWPropertyDefs.WALLCLOCKTIMEVALUE
				.defaultValue()));
		setAccessRights(XWAccessRights.DEFAULT);
		setShortIndexes(new int[] { TableColumns.UID.getOrdinal(),
				Columns.STATUS.getOrdinal(),
				Columns.COMPLETEDDATE.getOrdinal(), Columns.LABEL.getOrdinal() });

		setReplicatedUid(null);
		setExpectedReplications(0);
		setTotalReplica(0);
		setReplicaSetSize(0);
	}

	/**
	 * This is a copy constructor
	 * 
	 * @param src
	 *            is the WorkInterface to clone
	 */
	public WorkInterface(final WorkInterface src) throws IOException {

		this();
		this.setUID(src.getUID());
		updateInterface(src);
	}

	/**
	 * This constructs an object from DB
	 * 
	 * @param rs
	 *            is an SQL request result
	 * @exception IOException
	 */
	public WorkInterface(ResultSet rs) throws IOException {
		this();
		fill(rs);
	}

	/**
	 * This fills columns from DB
	 * 
	 * @since 9.0.0
	 * @param rs
	 *            is the SQL data set
	 * @throws IOException
	 */
	@Override
	public void fill(ResultSet rs) throws IOException {

		try {
			setUID((UID) TableColumns.UID.fromResultSet(rs));
			setOwner((UID) TableColumns.OWNERUID.fromResultSet(rs));
			setAccessRights((XWAccessRights) TableColumns.ACCESSRIGHTS
					.fromResultSet(rs));
		} catch (final Exception e) {
			throw new IOException(e.getMessage());
		}

		try {
			setUserProxy((URI) Columns.USERPROXY.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setApplication((UID) Columns.APPUID.fromResultSet(rs));
		} catch (final Exception e) {
			throw new IOException("Work " + getUID() + " has no application");
		}
		try {
			setSgId((String) Columns.SGID.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setStatus((StatusEnum) Columns.STATUS.fromResultSet(rs));
		} catch (final Exception e) {
			setStatus(StatusEnum.UNAVAILABLE);
		}
		try {
			setMinMemory((Integer) Columns.MINMEMORY.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setDiskSpace((Long) Columns.DISKSPACE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setMinCpuSpeed((Integer) Columns.MINCPUSPEED.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setMaxRetry((Integer) Columns.MAXRETRY.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setMaxWallClockTime((Long) Columns.MAXWALLCLOCKTIME
					.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setListenPort((String) Columns.LISTENPORT.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setRetry((Integer) Columns.RETRY.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setReturnCode((Integer) Columns.RETURNCODE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setSession((UID) Columns.SESSIONUID.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setReplicatedUid((UID) Columns.REPLICATEDUID.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setGroup((UID) Columns.GROUPUID.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setExpectedHost((UID) Columns.EXPECTEDHOSTUID.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setStdin((URI) Columns.STDINURI.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setDirin((URI) Columns.DIRINURI.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setResult((URI) Columns.RESULTURI.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setEnvVars((String) Columns.ENVVARS.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setLabel((String) Columns.LABEL.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setCmdLine((String) Columns.CMDLINE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setSmartSocketAddr((String) Columns.SMARTSOCKETADDR
					.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setSmartSocketClient((String) Columns.SMARTSOCKETCLIENT
					.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setErrorMsg((String) TableColumns.ERRORMSG.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setSendToClient((Boolean) Columns.SENDTOCLIENT.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setLocal((Boolean) Columns.LOCAL.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setActive((Boolean) Columns.ACTIVE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setExpectedReplications((Integer) Columns.REPLICATIONS.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setTotalReplica((Integer) Columns.TOTALR.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setReplicaSetSize((Integer) Columns.SIZER.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setDataDriven((URI) Columns.DATADRIVENURI.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setService((Boolean) Columns.ISSERVICE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setArrivalDate((Date) Columns.ARRIVALDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setReadyDate((Date) Columns.READYDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setReadyDate((Date) Columns.DATAREADYDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setCompStartDate((Date) Columns.COMPSTARTDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setCompEndDate((Date) Columns.COMPENDDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
		try {
			setCompletedDate((Date) Columns.COMPLETEDDATE.fromResultSet(rs));
		} catch (final Exception e) {
		}
	}

	/**
	 * This calls this(StreamIO.stream(input));
	 * 
	 * @param input
	 *            is a String containing an XML representation
	 */
	public WorkInterface(String input) throws IOException, SAXException {
		this(StreamIO.stream(input));
	}

	/**
	 * This constructs a new object from an XML file
	 * 
	 * @param f
	 *            is the XML file
	 * @see #WorkInterface(InputStream)
	 */
	public WorkInterface(File f) throws IOException, SAXException {
		this(new FileInputStream(f));
	}

	/**
	 * This creates a new object that will be retrieved with a complex SQL
	 * request
	 */
	public WorkInterface(SQLRequest r) {
		this();
		setRequest(r);
	}

	/**
	 * This constructs a new object from input stream providing XML
	 * representation
	 * 
	 * @param input
	 *            is the input stream
	 * @see XMLReader#read(InputStream)
	 * @throws IOException
	 *             on XML error
	 */
	public WorkInterface(InputStream input) throws IOException, SAXException {
		this();
		final XMLReader reader = new XMLReader(this);
		try {
			reader.read(input);
		} catch (final InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This constructs a new object from XML attributes received from input
	 * stream
	 * 
	 * @param attrs
	 *            contains attributes XML representation
	 * @see Table#fromXml(Attributes)
	 * @throws IOException
	 *             on XML error
	 */
	public WorkInterface(Attributes attrs) {
		this();
		super.fromXml(attrs);
	}

	/**
	 * This updates this object from interface.
	 */
	@Override
	public void updateInterface(Table witf) throws IOException {

		final WorkInterface itf = (WorkInterface) witf;

		if (itf.getOwner() == null) {
			throw new IOException("Owner can't be null");
		}
		setOwner(itf.getOwner());
		setGroup(itf.getGroup());
		setSession(itf.getSession());
		setTotalReplica(itf.getTotalReplica());
		setReplicaSetSize(itf.getReplicaSetSize());
		setExpectedReplications(itf.getExpectedReplications());
		setReplicatedUid(itf.getReplicatedUid());
		setDataDriven(itf.getDataDriven());
		setExpectedHost(itf.getExpectedHost());
		setAccessRights(itf.getAccessRights());
		setService(itf.isService());
		setSendToClient(itf.isSendToClient());
		setLocal(itf.isLocal());
		setActive(itf.isActive());
		setLabel(itf.getLabel());
		setApplication(itf.getApplication());
		setReturnCode(itf.getReturnCode());
		setCmdLine(itf.getCmdLine());
		setStdin(itf.getStdin());
		setDirin(itf.getDirin());
		setResult(itf.getResult());
		setArrivalDate(itf.getArrivalDate());
		setCompletedDate(itf.getCompletedDate());
		setErrorMsg(itf.getErrorMsg());
		setUserProxy(itf.getUserProxy());
		setSgId(itf.getSgId());
		setMaxRetry(itf.getMaxRetry());
		setMaxWallClockTime(itf.getMaxWallClockTime());
		setMinMemory(itf.getMinMemory());
		setMinCpuSpeed(itf.getMinCpuSpeed());
		setStatus(itf.getStatus());
		setSmartSocketAddr(itf.getSmartSocketAddr());
		setSmartSocketClient(itf.getSmartSocketClient());
		setEnvVars(itf.getEnvVars());
		setReadyDate(itf.getReadyDate());
		setDataReadyDate(itf.getDataReadyDate());
		setCompStartDate(itf.getCompStartDate());
		setCompEndDate(itf.getCompEndDate());
		setRetry(itf.getRetry());
		setListenPort(itf.getListenPort());
		setDiskSpace(itf.getDiskSpace());
	}

	/**
	 * This retrieves the session of this work
	 * 
	 * @return this attribute, or null if not set
	 */
	public UID getSession() {
		try {
			return (UID) getValue(Columns.SESSIONUID);
		} catch (final Exception e) {
			return null;
		}
	}
	/**
	 * This retrieves the data driven by this work
	 * 
	 * @return this attribute, or null if not set
	 * @since 10.0.0
	 */
	public URI getDataDriven() {
		try {
			return (URI) getValue(Columns.DATADRIVENURI);
		} catch (final Exception e) {
			return null;
		}
	}
	/**
	 * This retrieves the original work
	 * @since 10.0.0
	 * @return this attribute, or null if not set
	 */
	public UID getReplicatedUid() {
		try {
			return (UID) getValue(Columns.REPLICATEDUID);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * This gets an attribute
	 * 
	 * @return this attribute, or null if not set
	 */
	public UID getGroup() {
		try {
			return (UID) getValue(Columns.GROUPUID);
		} catch (final NullPointerException e) {
			return null;
		}
	}

	/**
	 * This retrieves the user proxy certificate URI
	 * 
	 * @return this attribute, or null if not set
	 */
	public URI getUserProxy() {
		return (URI) getValue(Columns.USERPROXY);
	}

	/**
	 * This gets the expected host UID
	 * 
	 * @return this attribute, or null if not set
	 * @since RPCXW
	 */
	public UID getExpectedHost() {
		try {
			return (UID) getValue(Columns.EXPECTEDHOSTUID);
		} catch (final NullPointerException e) {
			return null;
		}
	}

	/**
	 * This retrieves the service flag<br />
	 * If not set, this attr is forced to false
	 * 
	 * @return true if the work should execute a service, false otherwise
	 * @since RPCXW
	 */
	public final boolean isService() {
		try {
			return ((Boolean) getValue(Columns.ISSERVICE)).booleanValue();
		} catch (final NullPointerException e) {
			setService(false);
			return false;
		}
	}

	/**
	 * This gets this work application UID
	 * 
	 * @return this work application UID
	 * @exception IOException
	 *                is thrown is attribute is nor set, neither well formed
	 */
	public UID getApplication() throws IOException {
		try {
			return (UID) getValue(Columns.APPUID);
		} catch (final Exception e) {
			throw new IOException(
					"WorkInterface#getApplication() : attribute not set");
		}
	}

	/**
	 * @return this attribute, or null if not set
	 * @since 8.0.0 (FG)
	 */
	public String getEnvVars() {
		try {
			return ((String) getValue(Columns.ENVVARS));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This gets the job name as defined by user at submission time. <br>
	 * Label is optional
	 * 
	 * @return this attribute, or null if not set
	 */
	public String getLabel() {
		try {
			return ((String) getValue(Columns.LABEL));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This returns XWStatus.fromInt(getValue(Columns.STATUSID))
	 * 
	 * @return the status
	 */
	public StatusEnum getStatus() {
		try {
			return (StatusEnum) getValue(Columns.STATUS);
		} catch (final NullPointerException e) {
			setStatus(StatusEnum.UNAVAILABLE);
			return StatusEnum.UNAVAILABLE;
		}
	}

	/**
	 * This gets an attribute
	 * 
	 * @return job return code, 0 on error
	 */
	public int getReturnCode() {
		try {
			final Integer ret = (Integer) getValue(Columns.RETURNCODE);
			return ret.intValue();
		} catch (final NullPointerException e) {
			return 0;
		}
	}

	/**
	 * This retrieves the max retry if attr is not set it is forced to 0
	 * 
	 * @return attribute value or 0 if not set
	 */
	public int getMaxRetry() {
		final Integer ret = (Integer) getValue(Columns.MAXRETRY);
		if (ret != null) {
			return ret.intValue();
		}
		setMaxRetry(0);
		return 0;
	}

	/**
	 * This retrieves the wallclocktime. If attr is not set it is forced its
	 * default value
	 * 
	 * @return the wall clock time in seconds
	 * @since 8.2.0
	 */
	public int getMaxWallClockTime() {
		final Long ret = (Long) getValue(Columns.MAXWALLCLOCKTIME);
		if (ret != null) {
			return ret.intValue();
		}
		setMaxWallClockTime(Long.parseLong(XWPropertyDefs.WALLCLOCKTIME
				.defaultValue()));
		return 0;
	}

	/**
	 * This retrieves a comma separated ports list. The ports are those this job
	 * listens to
	 * 
	 * @return attribute value or null if not set
	 * @since 8.0.0
	 * @see #getSmartSocketAddr()
	 */
	public String getListenPort() {
		return (String) getValue(Columns.LISTENPORT);
	}

	/**
	 * This retrieves the retry if attr is not set it is forced to 0
	 * 
	 * @return attribute value or 0 if not set
	 */
	public int getRetry() {
		final Integer ret = (Integer) getValue(Columns.RETRY);
		if (ret != null) {
			return ret.intValue();
		}
		setRetry(0);
		return 0;
	}

	/**
	 * This retrieves the minimal amount of disk spave needed by this work. This
	 * is in Mb.
	 * 
	 * @return the attribute or 0 if not set
	 * @since 8.0.0
	 */
	public long getDiskSpace() {
		final Long ret = (Long) getValue(Columns.DISKSPACE);
		if (ret != null) {
			return ret.longValue();
		}
		return 0L;
	}

	/**
	 * This retrieves the minimal amount of memory needed by this work; if not
	 * set it is forced to 0. This is in Kb
	 * 
	 * @return the minimal amount of memory needed by this work in Kb
	 */
	public int getMinMemory() {
		final Integer ret = (Integer) getValue(Columns.MINMEMORY);
		if (ret != null) {
			return ret.intValue();
		}
		setMinMemory(0);
		return 0;
	}

	/**
	 * This retrieves an attribute; if not set it is forced to 0
	 * 
	 * @return the attribute or 0 if not set
	 */
	public int getMinCpuSpeed() {
		final Integer ret = (Integer) getValue(Columns.MINCPUSPEED);
		if (ret != null) {
			return ret.intValue();
		}
		setMinCpuSpeed(0);
		return 0;
	}

	/**
	 * This gets an attribute
	 * 
	 * @return this attribute, or null if not set
	 */
	public String getServer() {
		try {
			return ((String) getValue(Columns.SERVER));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This retrieves a comma separated SmartSockets addresses list. These aim
	 * to connect to ports the job is listening to. The SmartSockets addresses
	 * are stored in the order of the listening ports.
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 * @see #getListenPort()
	 */
	public String getSmartSocketAddr() {
		try {
			return ((String) getValue(Columns.SMARTSOCKETADDR));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This retrieves a semicolon list containing tuple of SmartSockets address
	 * and local port. This helps a job running on XWHEP worker side to connect
	 * to a server like application running on XWHEP client side. e.g.
	 * "Saddr1, port1; Saddr2, port2" This will be used to create one proxy per
	 * port to forward outgoing connections to the associated SmartSockets
	 * address.
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 */
	public String getSmartSocketClient() {
		try {
			return ((String) getValue(Columns.SMARTSOCKETCLIENT));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This retrieves the command line parameter
	 * 
	 * @return this attribute, or null if not set
	 */
	public String getCmdLine() {
		try {
			return ((String) getValue(Columns.CMDLINE));
		} catch (final Exception e) {
		}
		return null;
	}

	/**
	 * This retrieves the URI where to get stdin
	 * 
	 * @return this attribute, or null if not set
	 */
	public URI getStdin() {
		return (URI) getValue(Columns.STDINURI);
	}

	/**
	 * This retrieves the URI where to get dirin
	 * 
	 * @return this attribute, or null if not set
	 */
	public URI getDirin() {
		return (URI) getValue(Columns.DIRINURI);
	}

	/**
	 * This retrieves the URI where to get result
	 * 
	 * @return this attribute, or null if not set
	 */
	public URI getResult() {
		return (URI) getValue(Columns.RESULTURI);
	}

	/**
	 * This retrieves the submission date
	 * 
	 * @return this attribute, or null if not set
	 */
	public Date getArrivalDate() {
		return (Date) getValue(Columns.ARRIVALDATE);
	}

	/**
	 * This retrieves the date when this work has been downloaded by the worker
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 */
	public Date getReadyDate() {
		return (Date) getValue(Columns.READYDATE);
	}

	/**
	 * This retrieves the date when all this work data have been downloaded by
	 * the worker
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 */
	public Date getDataReadyDate() {
		return (Date) getValue(Columns.DATAREADYDATE);
	}

	/**
	 * This retrieves the date when this work has been started on worker side
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 */
	public Date getCompStartDate() {
		return (Date) getValue(Columns.COMPSTARTDATE);
	}

	/**
	 * This retrieves the date when this work computation has ended on worker
	 * side
	 * 
	 * @return this attribute, or null if not set
	 * @since 8.0.0
	 */
	public Date getCompEndDate() {
		return (Date) getValue(Columns.COMPENDDATE);
	}

	/**
	 * This gets an attribute
	 * 
	 * @return this attribute, or null if not set
	 */
	public Date getCompletedDate() {
		return (Date) getValue(Columns.COMPLETEDDATE);
	}

	/**
	 * This gets an attribute<br />
	 * This attr is forced to false if not set
	 * 
	 * @return this attribute
	 */
	public final boolean isSendToClient() {
		try {
			final Boolean ret = (Boolean) getValue(Columns.SENDTOCLIENT);
			return ret.booleanValue();
		} catch (final NullPointerException e) {
			setSendToClient(false);
			return false;
		}
	}

	/**
	 * This retrieves whether this work is local<br />
	 * This attr is forced to true, if not set
	 * 
	 * @return this attribute
	 */
	public final boolean isLocal() {
		try {
			final Boolean ret = (Boolean) getValue(Columns.LOCAL);
			return ret.booleanValue();
		} catch (final NullPointerException e) {
			setLocal(true);
			return true;
		}
	}

	/**
	 * This tests if this job should be selected for computation.
	 * This attr is forced to true, if not set
	 * 
	 * @return this attribute
	 */
	public final boolean isActive() {
		try {
			final Boolean ret = (Boolean) getValue(Columns.ACTIVE);
			return ret.booleanValue();
		} catch (final NullPointerException e) {
			setActive(true);
			return true;
		}
	}

	/**
	 * This retrieves the current amount of submitted replica.
	 * This forces the total amount of replica to 0, if not set
	 * @since 10.0.0
	 * @return this attribute
	 */
	public final int getTotalReplica() {
		final Integer ret = (Integer) getValue(Columns.TOTALR);
		if (ret != null) {
			return ret.intValue();
		}
		setTotalReplica(0);
		return 0;
	}

	/**
	 * This retrieves the replica set size (how many replica can be run simultaneously).
	 * This forces the replica set size to 0, if not set
	 * @since 10.0.0
	 * @return this attribute
	 */
	public final int getReplicaSetSize() {
		final Integer ret = (Integer) getValue(Columns.SIZER);
		if (ret != null) {
			return ret.intValue();
		}
		setReplicaSetSize(0);
		return 0;
	}

	/**
	 * This retrieves the amount of expected replica.
	 * This forces the amount of expected replica to 0, if not set
	 * @since 10.0.0
	 * @return this attribute
	 */
	public final int getExpectedReplications() {
		final Integer ret = (Integer) getValue(Columns.REPLICATIONS);
		if (ret != null) {
			return ret.intValue();
		}
		setExpectedReplications(0);
		return 0;
	}

	/**
	 * This marks this work as not managed by server yet
	 * 
	 * @since 9.0.0
	 */
	public final void setWaiting() {
		setArrivalDate(new java.util.Date());
		setStatus(StatusEnum.WAITING);
		setActive(true);
		setLocal(true);
		setSendToClient(false);
	}

	/**
	 * This tells whether this work is waiting to be inserted in server works
	 * pool
	 * 
	 * @return true if this work is waiting
	 * @since RPCXW
	 */
	public boolean isWaiting() {
		return (getStatus() == StatusEnum.WAITING);
	}

	/**
	 * This set the service grid identifier
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 7.2.0
	 */
	public final boolean setSgId(String v) {
		return setValue(Columns.SGID, v);
	}

	/**
	 * This retrieves the service grid id
	 * 
	 * @return the service grid identifier, or null if not set
	 * @since 7.2.0
	 */
	public String getSgId() {
		return (String) getValue(Columns.SGID);
	}

	/**
	 * This sets parameter value; this is called from
	 * TableInterface#fromXml(Attributes)
	 * 
	 * @param attribute
	 *            is the name of the attribute to set
	 * @param v
	 *            is the new attribute value
	 * @return true if value has changed, false otherwise
	 * @see Table#fromXml(Attributes)
	 */
	@Override
	public final boolean setValue(String attribute, Object v)
			throws IllegalArgumentException {
		final String A = attribute.toUpperCase();
		try {
			return setValue(TableColumns.valueOf(A), v);
		} catch (final Exception e) {
			return setValue(Columns.valueOf(A), v);
		}
	}

	/**
	 * This sets the session of this work
	 * 
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 */
	public final boolean setSession(UID v) {
		return setValue(Columns.SESSIONUID, v);
	}

	/**
	 * This sets the data of this work drives
	 * 
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 * @since 10.0.0
	 */
	public final boolean setDataDriven(final URI v) {
		return setValue(Columns.DATADRIVENURI, v);
	}

	/**
	 * This marks this work as a replication of the given work UID.
	 * @since 10.0.0
	 * @param v is the original work UID
	 * @return true if modified, false otherwise
	 */
	public final boolean setReplicatedUid(final UID v) {
		return setValue(Columns.REPLICATEDUID, v);
	}

	/**
	 * This marks this work as a replication of the given work UID.
	 * If (v != null), this work is marked as non replica-t-able (because we don't want to replicate a replica).
	 * @since 10.0.0
	 * @param v is the original work UID
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 */
	public final boolean replicate(final UID v) {
		setExpectedReplications(0);
		setTotalReplica(0);
		setReplicaSetSize(0);
		setError(null);
		setReturnCode(0);
		setPending();
		setArrivalDate(new java.util.Date());
		setResult(null);

		return setReplicatedUid(v);
	}

	/**
	 * This set the user proxy certificate URI
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setUserProxy(URI v) {
		return setValue(Columns.USERPROXY, v);
	}

	/**
	 * This sets the job group group is optional
	 * 
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 */
	public final boolean setGroup(UID v) {
		return setValue(Columns.GROUPUID, v);
	}

	/**
	 * This sets the expected worker UID
	 * 
	 * @param v
	 *            is the new expected worker UID
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 * @since RPCXW
	 */
	public final boolean setExpectedHost(UID v) {
		return setValue(Columns.EXPECTEDHOSTUID,
				(v == null ? null : v.toString()));
	}

	/**
	 * This sets this work application
	 * 
	 * @param v
	 *            is the application UID
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 */
	public final boolean setApplication(UID v) {
		return setValue(Columns.APPUID, v);
	}

	/**
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 * @since 8.0.0 (FG)
	 */
	public final boolean setEnvVars(String v) {
		return setValue(Columns.ENVVARS, v);
	}

	/**
	 * This sets the job name as defined by user at submission time. <br>
	 * Label is optional
	 * 
	 * @return true if job name modified (and thus this work should be updated),
	 *         false otherwise
	 */
	public final boolean setLabel(String v) {
		return setValue(Columns.LABEL, (v == null ? null : v));
	}

	/**
	 * This calls setStatusId(v.ordinal())
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setStatus(final StatusEnum v)
			throws ArrayIndexOutOfBoundsException {
		return setValue(Columns.STATUS, v);
	}

	/**
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setReturnCode(int v) {
		final Integer i = Integer.valueOf(v);
		final boolean ret = setValue(Columns.RETURNCODE, i);
		return ret;
	}

	/**
	 * This sets the maximum amount of times this work should be scheduled
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setMaxRetry(int v) {
		final Integer i = Integer.valueOf(v);
		final boolean ret = setValue(Columns.MAXRETRY, i);
		return ret;
	}

	/**
	 * This sets the wall clock time
	 * 
	 * @param v
	 *            is the new value in seconds
	 * @return true if value has changed, false otherwise
	 * @since 8.2.0
	 */
	public final boolean setMaxWallClockTime(long v) {
		final Long i = new Long(v);
		final boolean ret = setValue(Columns.MAXWALLCLOCKTIME, i);
		return ret;
	}

	/**
	 * This increments trial
	 */
	public void incRetry() {
		setRetry(getRetry() + 1);
	}

	/**
	 * This sets the amount of times this work has been scheduled
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setRetry(int v) {
		Integer i = Integer.valueOf(v);
		final boolean ret = setValue(Columns.RETRY, i);
		i = null;
		return ret;
	}

	/**
	 * This resets an aborted/lost Task.<BR>
	 * This increments "trials" attribute and set status to PENDING so that this
	 * is subject to be reschedulled
	 * 
	 * @param server
	 *            is the server name
	 * @since 8.0.0
	 */
	public void lost(String server) {
		setServer(server);
		incRetry();
		setPending();
	}

	/**
	 * This marks this work result as not available yet
	 */
	public void setPending() {
		setStatus(StatusEnum.PENDING);
		setActive(true);
		setLocal(true);
		setSendToClient(false);
	}

	/**
	 * This marks this work as completed
	 */
	public void setCompleted() {

		setStatus(StatusEnum.COMPLETED);

		try {
			if (getCompletedDate() == null) {
				setCompletedDate(new Date());
			}
		} catch (final Exception e) {
			getLogger().exception(e);
		}
	}

	/**
	 * This sets the status
	 */
	public void setRunning() {
		setStatus(StatusEnum.RUNNING);
	}

	/**
	 * This marks this work as still waiting for result
	 * 
	 * @since 7.5.0
	 */
	public void setDataRequest() {
		setStatus(StatusEnum.DATAREQUEST);
	}

	/**
	 * This marks this work as waiting for intermediate results
	 * 
	 * @since 8.2.0
	 */
	public void setResultRequest() {
		setStatus(StatusEnum.RESULTREQUEST);
	}

	/**
	 * This tests if this work is still waiting for result
	 * 
	 * @since 7.5.0
	 */
	public boolean isDataRequest() {
		return (getStatus() == StatusEnum.DATAREQUEST);
	}

	/**
	 * This tests if this work is waiting for intermediate results
	 * 
	 * @since 8.2.0
	 */
	public boolean isResultRequest() {
		return (getStatus() == StatusEnum.RESULTREQUEST);
	}

	/**
	 * This marks this work as ERROR
	 * 
	 * @param v
	 *            is the error msg, if any
	 */
	public void setError(String v) {
		setStatus(StatusEnum.ERROR);
		setErrorMsg(v);
	}

	/**
	 * This sets the port this work may listen to
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setListenPort(String v) {
		return setValue(Columns.LISTENPORT, v);
	}

	/**
	 * This sets the minimal amount of RAM this work needs, in Kb.
	 * Provided value must be positive and can not exceed XWPropertyDefs.MAXRAMSPACE.
	 * If(v > XWPropertyDefs.MAXDISKSPACE) v is forced to XWPropertyDefs.MAXDISKSPACE.
	 * If(v < 0) v is forced to 0.
	 * @param v is  the minimal amount of RAM this work needs in Kb
	 * @return true if value has changed, false otherwise
	 * @see XWPropertyDefs#MAXRAMSPACE
	 */
	public final boolean setMinMemory(final int v) {
		try {
			final String sysValueStr = System.getProperty(XWPropertyDefs.MAXRAMSPACE.toString());
			final String maxValueStr = sysValueStr == null ? XWPropertyDefs.MAXRAMSPACE.defaultValue() : sysValueStr;
			final int maxValue = Integer.valueOf(maxValueStr); 
			final int value = v > maxValue ? maxValue : v;
			return setValue(Columns.MINMEMORY, Integer.valueOf(value < 0 ? 0 : value));
		} catch(Exception e) {
			return setValue(Columns.MINMEMORY, 0);
		}
	}

	/**
	 * This sets the minimal amount of disk this work needs, in Mb.
	 * Provided value must be positive and can not exceed XWPropertyDefs.MAXDISKSPACE.
	 * If(v > XWPropertyDefs.MAXDISKSPACE) v is forced to XWPropertyDefs.MAXDISKSPACE.
	 * If(v < 0) v is forced to 0.
	 * @param v is the disk space in Mb.
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 * @see XWPropertyDefs#MAXDISKSPACE
	 */
	public final boolean setDiskSpace(final long v) {
		try {
			final String sysValueStr = System.getProperty(XWPropertyDefs.MAXDISKSPACE.toString());
			final String maxValueStr = sysValueStr == null ? XWPropertyDefs.MAXDISKSPACE.defaultValue() : sysValueStr;
			final long maxValue = Long.valueOf(maxValueStr); 
			final long value = v > maxValue ? maxValue : v;
			setValue(Columns.MINFREEMASSSTORAGE, Long.valueOf(value < 0L ? 0L : value));
			return setValue(Columns.DISKSPACE,   Long.valueOf(value < 0L ? 0L : value));
		} catch(Exception e) {
			return setValue(Columns.DISKSPACE, 0L);
		}
	}

	/**
	 * This sets the minimal CPU clock rate this work needs
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setMinCpuSpeed(int v) {
		return setValue(Columns.MINCPUSPEED, Integer.valueOf(v < 0 ? 0 : v));
	}

	/**
	 * This sets the server managing this work
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setServer(String v) {
		return setValue(Columns.SERVER, (v == null ? null : v));
	}

	/**
	 * This sets the command line parameters
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setCmdLine(String v) {
		final String vtrim = (v != null)  ? v.trim() : null;
		return setValue(Columns.CMDLINE, 
				(vtrim == null ? null : vtrim));
	}

	/**
	 * This sets the SmartSockets address to connect to server like job running
	 * on worker side
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setSmartSocketAddr(String v) {
		final String vtrim = (v != null)  ? v.trim() : null;
		return setValue(Columns.SMARTSOCKETADDR,
				(vtrim == null ? null : vtrim));
	}

	/**
	 * This sets the SmartSockets address so that a job running on worker side
	 * can connect to a server like application running on client side
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setSmartSocketClient(String v) {
		final String vtrim = (v != null)  ? v.trim() : null;
		return setValue(Columns.SMARTSOCKETCLIENT,
				(vtrim == null ? null : vtrim));
	}

	/**
	 * This set the URI where to get the stdin
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setStdin(URI v) {
		return setValue(Columns.STDINURI, v);
	}

	/**
	 * This set the URI where to get the dirin
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setDirin(URI v) {
		return setValue(Columns.DIRINURI, v);
	}

	/**
	 * This set the URI where to get the result
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setResult(URI v) {
		return setValue(Columns.RESULTURI, v);
	}

	/**
	 * This set the submission date
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setArrivalDate(Date v) {
		return setValue(Columns.ARRIVALDATE, v);
	}

	/**
	 * This set the date when this has been downloaded by the worker
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setReadyDate(Date v) {
		return setValue(Columns.READYDATE, v);
	}

	/**
	 * This set the date when all data have been downloaded by the worker
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setDataReadyDate(Date v) {
		return setValue(Columns.DATAREADYDATE, v);
	}

	/**
	 * This set the date when this has been started on worker side
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setCompStartDate(Date v) {
		return setValue(Columns.COMPSTARTDATE, v);
	}

	/**
	 * This set the date when this has been ended on worker side
	 * 
	 * @return true if value has changed, false otherwise
	 * @since 8.0.0
	 */
	public final boolean setCompEndDate(Date v) {
		return setValue(Columns.COMPENDDATE, v);
	}

	/**
	 * This sets the completion date
	 * 
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setCompletedDate(Date v) {
		return setValue(Columns.COMPLETEDDATE, v);
	}

	/**
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setSendToClient(boolean v) {
		final Boolean b = new Boolean(v);
		return setValue(Columns.SENDTOCLIENT, b);
	}

	/**
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setLocal(boolean v) {
		final Boolean b = new Boolean(v);
		return setValue(Columns.LOCAL, b);
	}

	/**
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setActive(boolean v) {
		final Boolean b = new Boolean(v);
		return setValue(Columns.ACTIVE, b);
	}

	/**
	 * This set the service flag; this flag is true if this works runs a service
	 * (and not an application)
	 * 
	 * @return true if value has changed, false otherwise
	 * @since RPCXW
	 */
	final public boolean setService(boolean v) {
		final Boolean b = new Boolean(v);
		return setValue(Columns.ISSERVICE, b);
	}

	/**
	 * This sets the total amount of submitted replica
	 * @return true if value has changed, false otherwise
	 * @since 10.0.0
	 */
	public final boolean setTotalReplica(int v) {
		final Integer b = new Integer(v);
		return setValue(Columns.TOTALR, b);
	}

	/**
	 * This increments the total amount of submitted replica
	 * @return true if value has changed, false otherwise
	 * @since 10.0.0
	 */
	public final boolean incTotalReplica() {
		return setTotalReplica(getTotalReplica() + 1);
	}

	/**
	 * This sets the amount of replica that can be computed simultaneously
	 * @return true if value has changed, false otherwise
	 * @since 10.0.0
	 */
	public final boolean setReplicaSetSize(int v) {
		final Integer b = new Integer(v);
		return setValue(Columns.SIZER, b);
	}

	/**
	 * This sets the expected amount of replica
	 * @since 10.0.0
	 * @return true if value has changed, false otherwise
	 */
	public final boolean setExpectedReplications(int v) {
		final Integer b = new Integer(v);
		return setValue(Columns.REPLICATIONS, b);
	}

	/**
	 * This is for testing only. Without any argument, this dumps a
	 * WorkInterface object. If the first argument is an XML file containing a
	 * description of a WorkInterface, this creates a WorkInterface from XML
	 * description and dumps it. <br />
	 * Usage : java -cp xtremweb.jar xtremweb.common.WorkInterface [xmlFile]
	 */
	public static void main(String[] argv) {
		try {
			final WorkInterface itf = new WorkInterface();
			itf.setUID(UID.getMyUid());
			if (argv.length > 0) {
				try {
					final XMLReader reader = new XMLReader(itf);
					reader.read(new FileInputStream(argv[0]));
				} catch (final XMLEndParseException e) {
				}
			}
			itf.setLoggerLevel(LoggerLevel.DEBUG);
			itf.setDUMPNULLS(true);
			final XMLWriter writer = new XMLWriter(new DataOutputStream(System.out));
			writer.write(itf);
		} catch (final Exception e) {
			final Logger logger = new Logger();
			logger.exception("Usage : java -cp " + XWTools.JARFILENAME
					+ " xtremweb.common.WorkInterface [anXMLDescriptionFile]",
					e);
		}
	}
}
