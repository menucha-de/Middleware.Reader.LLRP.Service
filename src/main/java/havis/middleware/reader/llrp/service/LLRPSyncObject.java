package havis.middleware.reader.llrp.service;

import havis.llrpservice.data.message.Message;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that represents an object to wait on a response after sending a request
 * in form of an <see cref="LLRPMessage"/> to the reader.
 */
public class LLRPSyncObject {
	private Message request;
	private Message response;
	private Lock monitor = new ReentrantLock();
	private Condition condition = monitor.newCondition();
	private int timeout;

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.service.LLRPSyncObject class.
	 * 
	 * @param request
	 *            The request message
	 * @param timeout
	 *            The timeout value in ms
	 */
	public LLRPSyncObject(Message request, int timeout) {
		this.request = request;
		this.timeout = timeout;
	}

	/**
	 * Gets the request message.
	 * 
	 * @return Message
	 */
	public Message getRequest() {
		return this.request;
	}

	/**
	 * Gets the response message to wait for.
	 * 
	 * @return Message
	 */
	public Message getResponse() {
		return this.response;
	}

	/**
	 * Gets the timeout value in ms.
	 * 
	 * @return timeout
	 */
	public int getTimeout() {
		return this.timeout;
	}

	/**
	 * Method to lock the syncobject.
	 */
	public void lock() {
		monitor.lock();
	}

	/**
	 * Method to unlock the syncobject.
	 */
	public void unlock() {
		monitor.unlock();
	}

	/**
	 * Method to enter the wait for a response message.
	 * 
	 * @return {@link LLRPReturnContainerUtil} object that contains the response
	 *         message set by this method and True property if response was
	 *         received within timout, false otherwise
	 */
	public LLRPReturnContainerUtil<Message> await() {
		LLRPReturnContainerUtil<Message> containerUtil = new LLRPReturnContainerUtil<>();
		containerUtil.setTrue(true);

		monitor.lock();

		try {
			if (!condition.await(this.timeout, TimeUnit.MILLISECONDS)) {
				containerUtil.setTrue(false);
			}
		} catch (InterruptedException ie) {
			// Empty
		} finally {
			monitor.unlock();
		}

		containerUtil.setValue(this.response);

		return containerUtil;
	}

	/**
	 * Method to notify the waiting thread that a response was send.
	 * 
	 * @param response
	 *            The response that was send
	 */
	public void notify(Message response) {
		monitor.lock();
		try {
			this.response = response;
			condition.signal();
		} finally {
			monitor.unlock();
		}
	}
}
