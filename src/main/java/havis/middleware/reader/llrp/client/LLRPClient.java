package havis.middleware.reader.llrp.client;

import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.ByteBufferSerializer;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.reader.llrp.connection.Connection;
import havis.middleware.reader.llrp.connection.TCPConnection;
import havis.middleware.reader.llrp.service.LLRPMessageHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;

/**
 * Class that represents a client to handle incoming and outgoing
 *
 * @see Message
 */
public class LLRPClient {
	Connection connection;
	private LLRPConnection llrpConnection;
	private Thread retrieveThread;
	private boolean isDisposed = false;
	private boolean retrieveLoop = false;
	private LLRPMessageHandler handler;

	/**
	 * Creates a new LLRP client
	 *
	 * @param handler the message handler to use
	 */
	public LLRPClient(LLRPMessageHandler handler) {
		this.handler = handler;
	}

	/**
	 * @return the LLRP connection properties
	 */
	public LLRPConnection getLlrpConnection() {
		return llrpConnection;
	}

	/**
	 * Method to establish the connection to a LLRP reader.
	 * 
	 * @param llrpConnection
	 *            The object that provide all connection informations
	 * @return Indicator if the connection was successfully established or not
	 */
	public boolean openConnection(LLRPConnection llrpConnection) {
		synchronized (this) {
			try {
				this.llrpConnection = llrpConnection;
				if (!(this.connection != null && this.connection.isConnected())) {
					switch (llrpConnection.getConnectionType()) {
					case TCP:
						this.connection = new TCPConnection(llrpConnection.getHost(), llrpConnection.getPort(), llrpConnection.getTimeout(),
								(int) (llrpConnection.getConnectionProperties().getKeepalive() * 1.1));
						break;
					default:
						return false;
					}
					if (!this.connection.openConnection())
						return false;
					this.retrieveLoop = true;
					this.retrieveThread = new Thread(new Runnable() {
						@Override
						public void run() {
							retrieveMessageLoop();
						}
					}, "LLRPClient retrieveMessageLoop() for " + llrpConnection.getHost() + ":" + llrpConnection.getPort());
					this.retrieveThread.start();
					return true;
				} else
					return false;
			} catch (Exception exc) {
				return false;
			}
		}
	}

	/**
	 * Method to disconnect form the llrp reader.
	 *
	 * @throws IOException
	 */
	public void closeConnection() {
		synchronized (this) {
			this.retrieveLoop = false;

			if (this.retrieveThread != null) {
				this.retrieveThread.interrupt();
			}

			if (this.connection != null) {
				this.connection.closeConnection();
			}
		}
	}

	/**
	 * Method to send a LLRP message to the llrp reader.
	 *
	 * @param message
	 *            The message to be send
	 * @throws IOException
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 */
	public void sendMessage(Message message) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		synchronized (this) {
			ByteBufferSerializer serializer = new ByteBufferSerializer();
			ByteBuffer data = ByteBuffer.allocate((int) serializer.getLength(message));

			serializer.serialize(message, data);

			this.connection.sendMessage(data);
		}
	}

	/**
	 * Loop method for the client thread where incomming bits are translated
	 * into LLRP messages. This method uses the <see cref="LLRPMessageHandler"/>
	 * to send the incomming responses to the related requestor. Furthermore the
	 * method triggers events for all unrequested messages from the LLRP reader.
	 */
	private void retrieveMessageLoop() {
		try {
			boolean lastWasError = false;
			while (this.retrieveLoop) {
				try {
					ByteBuffer header = this.connection.retrieveMessage(ByteBufferSerializer.MESSAGE_HEADER_LENGTH, this.connection.getKeepalive());
					ByteBufferSerializer serializer = new ByteBufferSerializer();

					MessageHeader msgHeader = serializer.deserializeMessageHeader(header);
					ByteBuffer body;

					if (msgHeader.getMessageLength() > 10) {
						body = this.connection.retrieveMessage((int) msgHeader.getMessageLength() - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, this.connection.getTimeout());
					} else {
						body = ByteBuffer.allocate(0);
					}

					Message message = serializer.deserializeMessage(msgHeader, body);
					delegateEventsAndReports(message);

					lastWasError = false;
				} catch (ClosedByInterruptException e) {
					// nothing to do, we have been interrupted (retrieveLoop is most likely false)
				} catch (ClosedChannelException e) {
					if (lastWasError)
						break; // end loop
					else {
						if (this.handler != null) {
							this.handler.notifyNoDataReceived();
						}
					}
				} catch (IOException e) {
					lastWasError = true;
					if (this.handler != null) {
						this.handler.notifyNoDataReceived();
					}
				}
			}
		} catch (InterruptedException e) {
			// nothing to do
		} catch (Exception e) {
			if (this.handler != null) {
				this.handler.notifyNoDataReceived();
			}
		}
	}

	private void delegateEventsAndReports(Message message) {
		switch (message.getMessageHeader().getMessageType()) {
		case GET_SUPPORTED_VERSION_RESPONSE:
		case SET_PROTOCOL_VERSION_RESPONSE:
		case GET_READER_CAPABILITIES_RESPONSE:
		case ADD_ROSPEC_RESPONSE:
		case DELETE_ROSPEC_RESPONSE:
		case START_ROSPEC_RESPONSE:
		case STOP_ROSPEC_RESPONSE:
		case ENABLE_ROSPEC_RESPONSE:
		case DISABLE_ROSPEC_RESPONSE:
		case GET_ROSPECS_RESPONSE:
		case ADD_ACCESSSPEC_RESPONSE:
		case DELETE_ACCESSSPEC_RESPONSE:
		case ENABLE_ACCESSSPEC_RESPONSE:
		case DISABLE_ACCESSSPEC_RESPONSE:
		case GET_ACCESSSPECS_RESPONSE:
		case GET_READER_CONFIG_RESPONSE:
		case SET_READER_CONFIG_RESPONSE:
		case CLOSE_CONNECTION_RESPONSE:
		case ERROR_MESSAGE:
			if (this.handler != null)
				this.handler.notifyResponse(message);
			break;
		case RO_ACCESS_REPORT:
		case CLIENT_REQUEST_OP:
		case KEEPALIVE:
		case READER_EVENT_NOTIFICATION:
			if (this.handler != null)
				this.handler.notifyEvent(message);
			break;
		default:
			break;
		}
	}

	/**
	 * Disposes this instance.
	 *
	 * @throws IOException
	 */
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
				if (this.connection != null) {
					this.connection.dispose();
				}
			}

			if (this.retrieveThread != null) {
				if (this.retrieveThread.isAlive()) {
					this.retrieveLoop = false;
				}
			}

			this.retrieveThread = null;
			this.connection = null;
			this.handler = null;
			this.isDisposed = true;
		}
	}
}
