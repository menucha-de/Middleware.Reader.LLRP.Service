package havis.middleware.reader.llrp.service.event;

import havis.llrpservice.data.message.Message;

import java.util.EventObject;

/**
 * Abstract class to abstract all LLRP event arguments
 * 
 * @param <Msg>
 */
public class LLRPEventArgs<Msg extends Message> extends EventObject {

	private static final long serialVersionUID = -7042721007562610450L;

	/**
	 * Gets the RO and Access report message.
	 * 
	 * @return Msg
	 */

	public Msg getMessage() {
		@SuppressWarnings("unchecked")
		Msg msg = (Msg) getSource();

		return msg;
	}

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.service.events.ROAccessReportEventArgs class.
	 * 
	 * @param message
	 *            The reader message
	 */
	public LLRPEventArgs(Msg message) {
		super(message);
	}
}
