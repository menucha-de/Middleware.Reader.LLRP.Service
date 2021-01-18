package havis.middleware.reader.llrp.client;

import havis.middleware.ale.base.exception.ValidationException;

import java.util.Map;

/**
 * Class that provides objects to hold LLRP connection informations.
 */
public class LLRPConnection {
	private LLRPProperties connectionProperties;
	private LLRPConnectionType connectionType;
	private String host;
	private int port;
	private int timeout = 5000;

	/**
	 * Returns the LLRP connection type.
	 *
	 * @return {@link LLRPConnectionType}
	 */
	public LLRPConnectionType getConnectionType() {
		return connectionType;
	}

	/**
	 * Sets an object that holds all connection properties.
	 *
	 * @param connectionProperties
	 *            To set
	 */
	public void setConnectionProperties(LLRPProperties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * @return {@link LLRPProperties}
	 */
	public LLRPProperties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Returns the LLRP Host address.
	 *
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets the connection port.
	 *
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gets the timeout interval after which a non response from the reader
	 * raise a timeout exception. The default value is 5000 milli seconds.
	 *
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Returns a new instance of the havis.middleware.llrp.client.LLRPConnection
	 * class for connection type TCP.
	 *
	 * @param host
	 *            The host of the reader
	 * @param port
	 *            The tcp port to use for connection to the reader
	 * @return The new instance
	 */
	private static LLRPConnection getTCPConnection(String host, int port) {
		LLRPConnection llrpConnection = new LLRPConnection();
		llrpConnection.setConnectionProperties(new LLRPProperties());
		llrpConnection.connectionType = LLRPConnectionType.TCP;
		llrpConnection.host = host;
		llrpConnection.port = port;

		return llrpConnection;
	}

	/**
	 * Validates all connector properties within <paramref name="properties"/>
	 * and returns a new instance of
	 * Havis.Middleware.LLRP.Client.LLRPConnection.
	 *
	 * @param properties
	 *            The property list to validate
	 * @return The new instace
	 * @throws ValidationException
	 */
	public static LLRPConnection validateConnectorProperties(Map<String, String> properties) throws ValidationException {
		for (Map.Entry<String, String> pair : properties.entrySet()) {
			switch (pair.getKey()) {
			case havis.middleware.ale.reader.Property.Connector.ConnectionType:
				break;
			case havis.middleware.ale.reader.Property.Connector.Host:
				break;
			case havis.middleware.ale.reader.Property.Connector.Port:
				break;
			case havis.middleware.ale.reader.Property.Connector.Timeout:
				break;
			case LLRPProperties.PropertyName.InventoryAttempts:
				break;
			case LLRPProperties.PropertyName.Keepalive:
				break;
			default:
				if (pair.getKey().startsWith(havis.middleware.ale.reader.Prefix.Connector)) {
					throw new ValidationException("Connector property '" + pair.getKey() + "' is not recognized for LLRP Reader!");
				}
				break;
			}
		}

		LLRPConnection connection = null;
		boolean bResult = true;
		String property = "";
		do {
			property = havis.middleware.ale.reader.Property.Connector.ConnectionType;
			String typeString = properties.get(property);

			LLRPConnectionType type;
			if (typeString == null)
				type = LLRPConnectionType.TCP;
			else {
				try {
					type = Enum.valueOf(LLRPConnectionType.class, typeString);
				} catch (Exception exc) {
					bResult = false;
					break;
				}
			}

			if (type == LLRPConnectionType.TCP) {
				property = havis.middleware.ale.reader.Property.Connector.Host;
				String host = properties.get(property);

				if (host == null || host.trim().length() == 0) {
					bResult = false;
					break;
				}

				property = havis.middleware.ale.reader.Property.Connector.Port;
				String portString = properties.get(property);

				int port;
				if (portString == null)
					port = 5084;
				else {
					try {
						port = Integer.parseInt(portString);
					} catch (NumberFormatException nfe) {
						bResult = false;
						break;
					}
				}

				if ((port < 0) || (port > 65535)) {
					bResult = false;
					break;
				}

				connection = LLRPConnection.getTCPConnection(host, port);
			}

			if (connection != null)
				connection.connectionProperties = new LLRPProperties();

			property = havis.middleware.ale.reader.Property.Connector.Timeout;
			String timeoutString = properties.get(property);

			if (timeoutString != null) {
				int timeout;

				try {
					timeout = Integer.parseInt(timeoutString);
				} catch (NumberFormatException nfe) {
					bResult = false;
					break;
				}

				connection.setTimeout(timeout);
			}

			property = LLRPProperties.PropertyName.InventoryAttempts;
			String inventoryAttemptsString = properties.get(property);

			if (inventoryAttemptsString != null) {
				int inventoryAttempts;

				try {
					inventoryAttempts = Integer.parseInt(inventoryAttemptsString);
				} catch (NumberFormatException nfe) {
					bResult = false;
					break;
				}

				connection.getConnectionProperties().setInventoryAttempts(inventoryAttempts);
			}

			property = LLRPProperties.PropertyName.Keepalive;
			String keepaliveString = properties.get(property);

			if (keepaliveString != null) {
				int keepalive;

				try {
					keepalive = Integer.parseInt(keepaliveString);
				} catch (NumberFormatException nfe) {
					bResult = false;
					break;
				}

				connection.getConnectionProperties().setKeepalive(keepalive);
			}
		} while (false);

		if (bResult) {
			return connection;
		} else {
			throw new ValidationException("Missing or wrong connector property '" + property + "' for LLRP Reader!");
		}
	}

	/*
	 * /(non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		LLRPConnection connection = (LLRPConnection) obj;

		return (this.connectionType == connection.connectionType) && (this.getHost().equals(connection.getHost())) && (this.getPort() == connection.getPort())
				&& (this.getTimeout() == connection.getTimeout())
				&& (this.getConnectionProperties().getKeepalive() == connection.getConnectionProperties().getKeepalive());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Type: '" + this.getConnectionType() + "'; Host: '" + this.getHost() + "'; Port: '" + this.getPort() + "'";
	}
}
