package havis.middleware.reader.llrp.service.exception;

import havis.llrpservice.data.message.ErrorMessage;

/**
 * Class that represents a LLRP exception send by the reader
 */
public class LLRPErrorException extends LLRPException {

	private static final long serialVersionUID = 3970241333299509714L;

	private ErrorMessage llrpError;

	/**
	 * Gets the LLRP error message.
	 * 
	 * @return ErrorMessage
	 */
	public ErrorMessage getLLRPError() {
		return this.llrpError;
	}

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.service.exceptions.LLRPErrorException class.
	 * 
	 * @param llrpError
	 *            The reader error message
	 */
	public LLRPErrorException(ErrorMessage llrpError) {
		super(llrpError.getStatus().getErrorDescription());
		this.llrpError = llrpError;
	}
}
