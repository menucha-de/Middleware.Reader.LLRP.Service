package havis.middleware.reader.llrp.connection;

import havis.middleware.utils.threading.NamedThreadFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents a TCP connection. This class implements the <see
 * cref="Connection"/> interface.
 */
public class TCPConnection implements Connection {
	private static final int WAIT_MS = 30;
	private static final int DISCONNECT_DELAY_MS = 100;
	private static final int MAX_CONNECTION_ATTEMPTS = 3;
	private static final int CONNECTION_RETRY_DELAY_MS = 1000;
	private static final String CONNECTION_REFUSED_MESSAGE = "Connection refused";

	private boolean isDisposed = false;
	private int port;
	private String hostname;
	private SocketChannel client;
	private int timeout;
	private int keepalive;
	private boolean isConnected;
	private ExecutorService executor;

	private final static Logger log = Logger.getLogger(TCPConnection.class.getName());

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.Connection.TCPConnection class.
	 *
	 * @param host
	 *            The host of the TCP server
	 * @param port
	 *            The TCP port for this connection
	 * @param timeout
	 *            The TCP timeout value for this connection
	 * @param keepalive
	 *            The TCP keepalive value for this connection
	 */
	public TCPConnection(String host, int port, int timeout, int keepalive) {
		this.hostname = host;
		this.port = port;
		this.timeout = timeout;
		this.keepalive = keepalive;
		this.isConnected = false;
	}

	/**
	 * Retrieves the connection timeout value.
	 */
	@Override
	public int getTimeout() {
		return this.timeout;
	}

	/**
	 * Retrieves the timespan after which reader should send keepalive
	 */
	@Override
	public int getKeepalive() {
		return this.keepalive;
	}

	/**
	 * Retrieves the connection state.
	 */
	@Override
	public boolean isConnected() {
		return this.isConnected;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see havis.middleware.reader.llrp.connection.IConnection#openConnection()
	 */
	@Override
	public boolean openConnection() {
		log.log(Level.FINE, "Attempting connection to \"" + this.hostname + ":" + this.port + "\"");
		this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("TCPConnection retrieveMessage() for " + this.hostname + ":" + this.port));
		try {
			if (this.client == null || !this.client.isConnected()) {
				int connectionAttempts = 0;
				while (true) {
					try {
						this.client = SocketChannel.open();
						this.client.socket().connect(new InetSocketAddress(this.hostname, this.port), this.timeout);
						log.log(Level.FINE, "Successfully opened connection to \"" + this.hostname + ":" + this.port + "\"");
						break;
					} catch (ConnectException e) {
						log.log(Level.FINE, "Failed to open connection to \"" + this.hostname + ":" + this.port + "\": " + e.toString());
						if (!CONNECTION_REFUSED_MESSAGE.equals(e.getMessage()) || ++connectionAttempts == MAX_CONNECTION_ATTEMPTS) {
							throw e;
						}
						Thread.sleep(CONNECTION_RETRY_DELAY_MS);
					}
				}

				this.isConnected = true;
			}

			return this.isConnected;
		} catch (Exception exc) {
			log.log(Level.FINE, "Failed to open connection to \"" + this.hostname + ":" + this.port + "\": " + exc.toString());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * havis.middleware.reader.llrp.connection.IConnection#closeConnection()
	 */
	@Override
	public void closeConnection() {
		this.executor.shutdownNow();
		try {
			this.client.close();
			// TODO: sleeping to avoid connection refusal on reconnect
			Thread.sleep(DISCONNECT_DELAY_MS);
		} catch (IOException e) {
			log.log(Level.FINE, "Failed to close connection to \"" + this.hostname + ":" + this.port + "\": " + e.toString());
		} catch (InterruptedException e) {
			log.log(Level.FINE, "Failed to close connection to \"" + this.hostname + ":" + this.port + "\": " + e.toString());
		}
		this.isConnected = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * havis.middleware.reader.llrp.connection.IConnection#retrieveMessage(int,int)
	 */
	@Override
	public ByteBuffer retrieveMessage(final int size, final int timeout) throws IOException, InterruptedException {
		// TODO: either use TCP connector from LLRP or implement clean asynchronous message retrieval

		// Since the OpenJDK SocketInputStream.read0 method might hang and
		// not respond to Thread.interrupt calls, we have to wrap the
		// connection handling into a separate thread. If waiting on the
		// thread is interrupted, we try canceling the socket operation,
		// this might fail and the thread will hang until the socket timeout
		// is reached.
		Future<ByteBuffer> task = executor.submit(new Callable<ByteBuffer>() {
			@Override
			public ByteBuffer call() throws IOException, InterruptedException {
				try {
					long startMs = System.currentTimeMillis();
					ByteBuffer data = ByteBuffer.allocate(size);

					int received = 0;
					do {
						int dataReceived = client.read(data);
						if (dataReceived == -1) {
							if ((System.currentTimeMillis() - startMs) >= timeout) {
								throw new IOException("Unable to read data[" + size + "] from stream within " + timeout + "ms.");
							} else {
								Thread.sleep(WAIT_MS);
							}
						} else {
							received += dataReceived;
						}
					} while (received < size);

					data.flip();
					return data;
				} catch (Throwable e) {
					log.log(Level.SEVERE, "Failed to read: " + e.toString());
					throw e;
				}
			}
		});

		try {
			return task.get(timeout, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException)
				throw (IOException) e.getCause();
			else
				throw new IOException(e.getCause().toString());
		} catch (TimeoutException e) {
			task.cancel(true);
			throw new IOException("Unable to read data[" + size + "] from stream within " + timeout + "ms.");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * havis.middleware.reader.llrp.connection.IConnection#sendMessage(java.
	 * nio.ByteBuffer)
	 */
	@Override
	public void sendMessage(ByteBuffer data) throws IOException {
		data.flip();
		client.write(data);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see havis.middleware.reader.llrp.connection.IConnection#dispose()
	 */
	@Override
	public void dispose() throws IOException {
		dispose(true);
	}

	/**
	 * Disposes this instance. According to <paramref name="disposing"/> also
	 * managed resources will be disposed.
	 *
	 * @param disposing
	 *            Indicator if also managed resources should be dispoed.
	 * @throws IOException
	 */
	protected void dispose(boolean disposing) throws IOException {
		if (!this.isDisposed) {
			if (disposing) {
				if (this.isConnected)
					this.closeConnection();
			}
			this.client = null;
			this.isDisposed = true;
		}
	}
}
