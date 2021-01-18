package havis.middleware.reader.llrp.service.exception;

/**
 * Class that represents a exception that occurred when the reader did not
 * response on a request.
 */
public class LLRPTimeoutException extends LLRPException {

	private static final long serialVersionUID = 6253762320589133335L;

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.service.exceptions.LLRPTimeoutException class.
	 * 
	 * @param message
	 *            The message to describe the exception
	 */
	public LLRPTimeoutException(String message) {
		super(message);
	}
}
