package havis.middleware.reader.llrp.service;

import havis.llrpservice.data.message.ClientRequestOP;
import havis.llrpservice.data.message.Keepalive;
import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.reader.llrp.client.LLRPClient;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;
import havis.middleware.utils.threading.Pipeline;

import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that provides mechanisms to handle all messages send to or
 * received from a <see cref="LLRPClient"/>.
 */
public class LLRPMessageHandler implements Runnable {
	/**
	 * List of all waiting objects
	 */
	private Map<Long, LLRPSyncObject> waitingList;
	private LLRPClient client;
	private LLRPService service;
	private Object syncWaitingList = new Object();
	private Thread eventThread;
	private Pipeline<Message> eventPipe = new Pipeline<Message>();
	private boolean isDisposed = false;

	/**
	 * Retrieves the used LLRPClient
	 *
	 * @return LLRPClient
	 */
	public LLRPClient getClient() {
		return this.client;
	}

	/**
	 * Sets the used LLRPClient
	 *
	 * @param client LLRPClient
	 */
	void setClient(LLRPClient client) {
		this.client = client;
	}

	/**
	 * Retrieves the LLRPService
	 *
	 * @return LLRPService
	 */
	public LLRPService getService() {
		return this.service;
	}

	/**
	 * Creates a new instance of LLRPMessageHandler
	 *
	 * @param service
	 */
	public LLRPMessageHandler (LLRPService service) {
		this.service = service;
	}

	/**
	 * @return Initialized waitingList
	 */
	private Map<Long, LLRPSyncObject> getWaitingList() {

		if (this.waitingList == null)
        {
			this.waitingList = new HashMap<Long, LLRPSyncObject>();
        }

		return this.waitingList;
	}

	/**
	 * Method to send a message with no response.
	 *
	 * @param message
	 *            The messages to be send
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public void sendMessage(Message message) throws IOException,
			InvalidMessageTypeException, InvalidParameterTypeException {
		if (this.client == null)
			throw new IllegalStateException("Client not set");
		this.client.sendMessage(message);
	}

	/**
	 * Method to send a request and wait for the corresponding response.
	 *
	 * @param request
	 *            The request message to be send
	 * @param timeout
	 *            The value after which no response lead to a timeout
	 * @return True if response was received within timout, false otherwise
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public LLRPReturnContainerUtil<Message> waitOnResponse(Message request, int timeout) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException {
		if (this.client == null)
			throw new IllegalStateException("Client not set");

		LLRPSyncObject sync = new LLRPSyncObject(request, timeout);
		LLRPReturnContainerUtil<Message> containerUtil;

		sync.lock();
		try {
			synchronized (this.syncWaitingList) {
				this.getWaitingList().put(Long.valueOf(request.getMessageHeader().getId()), sync);
				this.client.sendMessage(request);
			}

			containerUtil = sync.await();
			synchronized (this.syncWaitingList) {
				this.getWaitingList().remove(Long.valueOf(request.getMessageHeader().getId()));
			}
		} finally {
			sync.unlock();
		}

		return containerUtil;
	}

	/**
	 * Method to notify a waiting object on the manager about a response
	 * message.
	 *
	 * @param response
	 *            The response message
	 */
	public void notifyResponse(Message response) {
		LLRPSyncObject sync;
		synchronized (this.syncWaitingList) {
			sync = this.getWaitingList().get(Long.valueOf(response
					.getMessageHeader().getId()));

			if (sync == null) {
				return;
			}
		}

		sync.lock();
		try {
			sync.notify(response);
		} finally {
			sync.unlock();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				Message evt = this.eventPipe.dequeue();
				if (evt == null) {
					break; // pipeline was disposed
				}
				switch (evt.getMessageHeader().getMessageType()) {
				case RO_ACCESS_REPORT:
					this.service.onROAccessReportEvent(new LLRPEventArgs<ROAccessReport>((ROAccessReport) evt));
					break;
				case CLIENT_REQUEST_OP:
					this.service.onClientRequestOpEvent(new LLRPEventArgs<ClientRequestOP>((ClientRequestOP) evt));
					break;
				case KEEPALIVE:
					this.service.onKeepaliveEvent(new LLRPEventArgs<Keepalive>((Keepalive) evt));
					break;
				case READER_EVENT_NOTIFICATION:
					this.service.onReaderNotificationEvent(new LLRPEventArgs<ReaderEventNotification>((ReaderEventNotification) evt));
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.eventThread = null;
		}
	}

	/**
	 * Method to notify the LLRPService asynchroniusly about an incomming event.
	 *
	 * @param evt
	 *            The incomming event
	 */
	public void notifyEvent(Message evt) {
		this.eventPipe.enqueue(evt);

		if (this.eventThread == null) {
			this.eventThread = new Thread(this, "LLRPMessageHandler run()"
					+ (client != null && client.getLlrpConnection() != null ? (" for " + client.getLlrpConnection().getHost() + ":" + client
							.getLlrpConnection().getPort()) : ""));
			this.eventThread.start();
		}
	}

	/**
	 * Method to notify the LLRPService asynchroniusly about the no data
	 * received event.
	 */
	public void notifyNoDataReceived() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (LLRPMessageHandler.this.service != null) {
					LLRPMessageHandler.this.service.onNoDataReceivedEvent(new EventObject(LLRPMessageHandler.this));
				}
			}
		}).start();
	}

	/**
	 * Disposes this instance.
	 */
	public void dispose() {
		dispose(true);
	}

	/**
	 * Disposes this instance. According to <paramref name="disposing"/> also
	 * managed resources will be disposed.
	 *
	 * @param disposing
	 *            Indicator if also managed resources should be disposed.
	 */
	protected void dispose(boolean disposing) {
		if (!this.isDisposed) {
			this.isDisposed = true;
			if (disposing) {
				this.eventPipe.dispose();
			}
		}

		this.client = null;
		this.service = null;
		this.waitingList = null;
		this.eventPipe = null;
		this.eventPipe = new Pipeline<Message>();
	}
}
