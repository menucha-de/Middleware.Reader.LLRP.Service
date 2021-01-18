package havis.middleware.reader.llrp.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface that defines operations and properties for connection
 * implementations. This interface extends the <see cref="IDisposable"/>
 * interface.
 */
public interface Connection {
	/**
	 * Retrieves the connection timeout value.
	 *
	 * @return timeout
	 */
	int getTimeout();

	/**
	 * Retrieves the timespan after which reader should send keepalive.
	 *
	 * @return keepalive
	 */
	int getKeepalive();

	/**
	 * Retrieves the connection state.
	 *
	 * @return connected
	 */
	boolean isConnected();

	/**
	 * Method to establish the connection.
	 *
	 * @return Indicator if the connection was successfully established or not
	 */
	boolean openConnection();

	/**
	 * Method to disconnect.
	 *
	 * @throws IOException
	 */
	void closeConnection();

	/**
	 * Method to read a number of bytes according to <paramref name="size"/>.
	 *
	 * @param size
	 *            Number of bytes to read.
	 * @param timeout
	 *            The timeout in milliseconds
	 * @return Returns the requested byte array.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	ByteBuffer retrieveMessage(int size, int timeout) throws IOException, InterruptedException;

	/**
	 * Method to send messages as byte array to the server.
	 *
	 * @param data
	 *            The messages to be send
	 * @throws IOException
	 */
	void sendMessage(ByteBuffer data) throws IOException;

	/**
	 * Disposes this instance.
	 *
	 * @throws IOException
	 */
	void dispose() throws IOException;
}
