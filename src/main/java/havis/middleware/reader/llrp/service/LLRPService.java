package havis.middleware.reader.llrp.service;

import havis.llrpservice.data.message.AddAccessSpec;
import havis.llrpservice.data.message.AddAccessSpecResponse;
import havis.llrpservice.data.message.AddROSpec;
import havis.llrpservice.data.message.AddROSpecResponse;
import havis.llrpservice.data.message.ClientRequestOP;
import havis.llrpservice.data.message.ClientRequestOPResponse;
import havis.llrpservice.data.message.CloseConnection;
import havis.llrpservice.data.message.CloseConnectionResponse;
import havis.llrpservice.data.message.DeleteAccessSpec;
import havis.llrpservice.data.message.DeleteAccessSpecResponse;
import havis.llrpservice.data.message.DeleteROSpec;
import havis.llrpservice.data.message.DeleteROSpecResponse;
import havis.llrpservice.data.message.DisableAccessSpec;
import havis.llrpservice.data.message.DisableAccessSpecResponse;
import havis.llrpservice.data.message.DisableROSpec;
import havis.llrpservice.data.message.DisableROSpecResponse;
import havis.llrpservice.data.message.EnableAccessSpec;
import havis.llrpservice.data.message.EnableAccessSpecResponse;
import havis.llrpservice.data.message.EnableEventsAndReports;
import havis.llrpservice.data.message.EnableROSpec;
import havis.llrpservice.data.message.EnableROSpecResponse;
import havis.llrpservice.data.message.ErrorMessage;
import havis.llrpservice.data.message.GetAccessSpecs;
import havis.llrpservice.data.message.GetAccessSpecsResponse;
import havis.llrpservice.data.message.GetROSpecs;
import havis.llrpservice.data.message.GetROSpecsResponse;
import havis.llrpservice.data.message.GetReaderCapabilities;
import havis.llrpservice.data.message.GetReaderCapabilitiesResponse;
import havis.llrpservice.data.message.GetReaderConfig;
import havis.llrpservice.data.message.GetReaderConfigResponse;
import havis.llrpservice.data.message.GetReport;
import havis.llrpservice.data.message.GetSupportedVersion;
import havis.llrpservice.data.message.GetSupportedVersionResponse;
import havis.llrpservice.data.message.Keepalive;
import havis.llrpservice.data.message.KeepaliveAck;
import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.SetProtocolVersion;
import havis.llrpservice.data.message.SetProtocolVersionResponse;
import havis.llrpservice.data.message.SetReaderConfig;
import havis.llrpservice.data.message.SetReaderConfigResponse;
import havis.llrpservice.data.message.StartROSpec;
import havis.llrpservice.data.message.StartROSpecResponse;
import havis.llrpservice.data.message.StopROSpec;
import havis.llrpservice.data.message.StopROSpecResponse;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.reader.llrp.client.LLRPClient;
import havis.middleware.reader.llrp.client.LLRPConnection;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.service.event.LLRPEventHandler;
import havis.middleware.reader.llrp.service.exception.LLRPErrorException;
import havis.middleware.reader.llrp.service.exception.LLRPException;
import havis.middleware.reader.llrp.service.exception.LLRPTimeoutException;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;

import java.io.IOException;
import java.util.EventObject;

/**
 * Class that abstracts the Request/Response <see cref="LLRPMessage"/> as
 * synchronous operations. The class also provides events for all <see
 * cref="LLRPMessage"/> received asynchroniously from the <see
 * cref="LLRPClient"/>
 */
public class LLRPService {
	private LLRPMessageHandler handler;
	private LLRPClient client;
	private int timeout;
	private boolean isDisposed = false;

	/**
	 * Occurred when reader send RO_ACCESS_REPORT event.
	 */
	private LLRPEventHandler<LLRPEventArgs<ROAccessReport>> roAccessReportEvent = new LLRPEventHandler<LLRPEventArgs<ROAccessReport>>();

	/**
	 * Occurred when reader send CLIENT_REQUEST_OP event.
	 */
	public LLRPEventHandler<LLRPEventArgs<ClientRequestOP>> clientRequestOpEvent = new LLRPEventHandler<LLRPEventArgs<ClientRequestOP>>();

	/**
	 * Occurred when reader send KEEPALIVE event.
	 */
	private LLRPEventHandler<LLRPEventArgs<Keepalive>> keepaliveEvent = new LLRPEventHandler<LLRPEventArgs<Keepalive>>();

	/**
	 * The event will be raised if LLRPClient received no data within keepalive
	 * timespan.
	 */
	private LLRPEventHandler<EventObject> noDataReceivedEvent = new LLRPEventHandler<EventObject>();

	/**
	 * Occurred when reader send READER_EVENT_NOTIFICATION event.
	 */
	private LLRPEventHandler<LLRPEventArgs<ReaderEventNotification>> readerNotificationEvent = new LLRPEventHandler<LLRPEventArgs<ReaderEventNotification>>();

	/**
	 * Retrieves the llrp client used by this service
	 *
	 * @return LLRPClient
	 */
	public LLRPClient getClient() {
		return client;
	}

	/**
	 * Initializes a new instance of the
	 * havis.middleware.llrp.service.LLRPService class.
	 */
	public LLRPService() {
		this.handler = new LLRPMessageHandler(this);
		this.client = new LLRPClient(this.handler);
		this.handler.setClient(this.client);
	}

	/**
	 * Methode to handel async llrp request and wait for response.
	 *
	 * @param request
	 *            The request message to be sent
	 * @param methodName
	 *            The name of the calling methode
	 * @return The corresponding llrp response.
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	private synchronized <Response extends Message> Response handleAsyncLLRPMessage(Message request, String methodName) throws LLRPException,
			LLRPTimeoutException {

		LLRPReturnContainerUtil<Message> result;

		try {
			result = this.handler.waitOnResponse(request, this.timeout);
		} catch (IOException | InvalidMessageTypeException | InvalidParameterTypeException e) {
			throw new LLRPException(e.getMessage());
		}

		Message response = result.getValue();

		if (result.isTrue()) {
			if (response instanceof ErrorMessage) {
				throw new LLRPErrorException((ErrorMessage) response);
			} else if (response instanceof Message) {
				@SuppressWarnings("unchecked")
				Response res = (Response) response;
				return res;
			} else {
				return null;
			}
		}

		throw new LLRPTimeoutException("Timeout during '" + methodName + "' Occurred at LLRP Reader");
	}

	/**
	 * Method to establish the connection to a LLRP reader.
	 *
	 * @param llrpConnection
	 *            The object that provide all connection informations
	 * @return Indicator if the connection was successfully established or not
	 */
	public boolean openConnection(LLRPConnection llrpConnection) {
		this.timeout = llrpConnection.getTimeout();
		return this.client.openConnection(llrpConnection);
	}

	/**
	 * Method to inform the reader that the connection will be closed.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public CloseConnectionResponse closeConnection(CloseConnection request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "CloseConnection");
	}

	/**
	 * Method to disconnect form the llrp reader.
	 *
	 * @throws IOException
	 */
	public void closeConnection() {
		this.client.closeConnection();
	}

	/**
	 * Method to request the highest supported LLRP version from the reader.
	 *
	 * <pre>
	 * Message supported since version 1.1
	 * </pre>
	 *
	 * @param request
	 *            The request message
	 * @return Response message with the highest supported version or
	 *         LLRPErrorException "Unsupported-Version" if hightest version is
	 *         1.0
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public GetSupportedVersionResponse getSupportedVersion(GetSupportedVersion request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "GetSupportedVersion");
	}

	/**
	 * Method to set the protocol version for the current connection
	 *
	 * <pre>
	 * Message supported since version 1.1
	 * </pre>
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public SetProtocolVersionResponse setProtocolVersion(SetProtocolVersion request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "SetProtocolVersion");
	}

	/**
	 * Method to request the reader capabilities.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with the requested capabilities
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public GetReaderCapabilitiesResponse getReaderCapabilities(GetReaderCapabilities request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "GetReaderCapabilities");
	}

	/**
	 * Method to add a ROSpec to the LLRP reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public AddROSpecResponse addROSpec(AddROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "AddROSpec");
	}

	/**
	 * Method to remove a ROSpec form the LLRP reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public DeleteROSpecResponse deleteROSpec(DeleteROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "DeleteROSpec");
	}

	/**
	 * Method to start a ROSpec on the LLRP reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public StartROSpecResponse startROSpec(StartROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "StartROSpec");
	}

	/**
	 * Method to stop a ROSpec on the LLRp reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response messsge with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public StopROSpecResponse stopROSpec(StopROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "StopROSpec");
	}

	/**
	 * Method to enable a ROSpec on the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public EnableROSpecResponse enableROSpec(EnableROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "EnableROSpec");
	}

	/**
	 * Method to disable a ROSpec on the reader.
	 *
	 * @param request
	 *            The request message<
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public DisableROSpecResponse disableROSpec(DisableROSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "DisableROSpec");
	}

	/**
	 * Method to request all ROSpecs from the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with all ROSpecs
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public GetROSpecsResponse getROSpecs(GetROSpecs request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "GetROSpecs");
	}

	/**
	 * Method to add a AccessSpec to the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public AddAccessSpecResponse addAccessSpec(AddAccessSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "AddAccessSpec");
	}

	/**
	 * Method to remove a AccessSpec from the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public DeleteAccessSpecResponse deleteAccessSpec(DeleteAccessSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "DeleteAccessSpec");
	}

	/**
	 * Method to enable a AccessSpec on the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public EnableAccessSpecResponse enableAccessSpec(EnableAccessSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "EnableAccessSpec");
	}

	/**
	 * Method to disable a AccessSpec on the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public DisableAccessSpecResponse disableAccessSpec(DisableAccessSpec request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "DisableAccessSpec");
	}

	/**
	 * Method to request all AccessSpecs from the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with all AccessSpecs
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public GetAccessSpecsResponse getAccessSpecs(GetAccessSpecs request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "GetAccessSpecs");
	}

	/**
	 * Method to request all the configuration from the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with the requested configuration
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public GetReaderConfigResponse getReaderConfig(GetReaderConfig request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "GetReaderConfig");
	}

	/**
	 * Method to set a configuration on the reader.
	 *
	 * @param request
	 *            The request message
	 * @return Response message with LLRP status code
	 * @throws LLRPErrorException
	 * @throws LLRPTimeoutException
	 */
	public SetReaderConfigResponse setReaderConfig(SetReaderConfig request) throws LLRPException {
		return handleAsyncLLRPMessage(request, "SetReaderConfig");
	}

	/**
	 * Method to get the tag reports from the reader.
	 *
	 * @param request
	 *            The request message
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public void getReport(GetReport request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		this.handler.sendMessage(request);
	}

	/**
	 * Method to inform the reader that it can remove its hold on events and
	 * report messages.
	 *
	 * @param request
	 *            The request message
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public void enableEventsAndReports(EnableEventsAndReports request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		this.handler.sendMessage(request);
	}

	/**
	 * Method to send the CLIENT_REQUEST_OP_RESPONSE to the reader.
	 *
	 * @param response
	 *            The response message
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public void clientRequestOpResponse(ClientRequestOPResponse response) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		this.handler.sendMessage(response);
	}

	/**
	 * Method to send KEEPALIVE_ACK to the reader.
	 *
	 * @param response
	 *            The response message
	 * @throws InvalidParameterTypeException
	 * @throws InvalidMessageTypeException
	 * @throws IOException
	 */
	public void keepaliveAck(KeepaliveAck response) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		this.handler.sendMessage(response);
	}

	public LLRPEventHandler<LLRPEventArgs<ROAccessReport>> getROAccessReportEvent() {
		return roAccessReportEvent;
	}

	/**
	 * Method to inform LLRPService about ro access report event occurred.
	 *
	 * @param e
	 *            event arguments
	 */
	public void onROAccessReportEvent(LLRPEventArgs<ROAccessReport> e) {
		if (roAccessReportEvent != null) {
			roAccessReportEvent.handleEvent(this, e);
		}
	}

	public LLRPEventHandler<LLRPEventArgs<ClientRequestOP>> getClientRequestOpEvent() {
		return clientRequestOpEvent;
	}

	/**
	 * Method to inform LLRPService about client request op event occurred.
	 *
	 * @param e
	 *            event arguments
	 */
	public void onClientRequestOpEvent(LLRPEventArgs<ClientRequestOP> e) {
		if (clientRequestOpEvent != null)
			clientRequestOpEvent.handleEvent(this, e);
	}

	public LLRPEventHandler<LLRPEventArgs<Keepalive>> getKeepaliveEvent() {
		return keepaliveEvent;
	}

	/**
	 * Method to inform LLRPService about keepalive event occurred.
	 *
	 * @param e
	 *            event arguments
	 */
	public void onKeepaliveEvent(LLRPEventArgs<Keepalive> e) {
		if (keepaliveEvent != null)
			keepaliveEvent.handleEvent(this, e);
	}

	public LLRPEventHandler<LLRPEventArgs<ReaderEventNotification>> getReaderNotificationEvent() {
		return readerNotificationEvent;
	}

	/**
	 * Method to inform LLRPService about reader event notification event
	 * occurred.
	 *
	 * @param e
	 *            event arguments
	 */
	public void onReaderNotificationEvent(LLRPEventArgs<ReaderEventNotification> e) {
		if (readerNotificationEvent != null) {
			readerNotificationEvent.handleEvent(this, e);
		}
	}

	public LLRPEventHandler<EventObject> getNoDataReceivedEvent() {
		return noDataReceivedEvent;
	}

	/**
	 * Method to inform LLRPService about no data received event occurred.
	 *
	 * @param e
	 *            event arguments
	 */
	public void onNoDataReceivedEvent(EventObject e) {
		if (noDataReceivedEvent != null) {
			noDataReceivedEvent.handleEvent(this, e);
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
	 *            Indicator if also managed resources should be disposed.
	 * @throws IOException
	 */
	protected void dispose(boolean disposing) throws IOException {
		if (!this.isDisposed) {
			if (disposing) {
				if (this.client != null)
					this.client.dispose();
				if (this.handler != null)
					this.handler.dispose();
			}
			this.client = null;
			this.handler = null;
			this.isDisposed = true;
		}
	}
}
