package havis.middleware.reader.llrp.service.exception;

/**
 * Class to abstract all LLRP exceptions
 */
public class LLRPException extends Exception {

	private static final long serialVersionUID = 7182503769546569651L;

	/**
	 * Abstract constructor to create a LLRP exception.
	 * 
	 * @param message
	 *            The message to describe the exception
	 */
	public LLRPException(String message) {
		super(message);
	}
}
