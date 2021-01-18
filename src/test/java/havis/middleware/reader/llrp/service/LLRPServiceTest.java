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
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
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
import havis.llrpservice.data.message.parameter.LLRPStatus;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class LLRPServiceTest {
	@Test
	public void checkConstructAndDispose() throws IOException {
		LLRPService llrpService = new LLRPService();

		Assert.assertNotNull(llrpService.getClient());

		llrpService.dispose();

		Assert.assertNull(llrpService.getClient());
	}

	private <T> void generateResponse(Class<T> clazz, long id, LLRPStatusCode statusCode) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), statusCode, "TESTETST");
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, id);
		Constructor<T> constructor = clazz.getConstructor(MessageHeader.class, LLRPStatus.class);
		T response = constructor.newInstance(header, llrpStatus);
		final LLRPReturnContainerUtil<T> containerUtil = new LLRPReturnContainerUtil<>();

		containerUtil.setValue(response);
		containerUtil.setTrue(true);

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<T> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {

				return containerUtil;
			}
		};
	}

	private <T extends Message> void check(T t, long id, LLRPStatusCode statusCode) {
		Assert.assertNotNull(t);
		Assert.assertNotNull(t.getMessageHeader());
		Assert.assertEquals(id, t.getMessageHeader().getId());
	}

	@Test
	public void checkDeleteROSpec(final @Mocked DeleteROSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,

	LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(DeleteROSpecResponse.class, id, statusCode);

		DeleteROSpecResponse deleteROSpecResponse = llrpService.deleteROSpec(request);

		check(deleteROSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, deleteROSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkstartROSpec(final @Mocked StartROSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(StartROSpecResponse.class, id, statusCode);

		StartROSpecResponse startROSpecResponse = llrpService.startROSpec(request);

		check(startROSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, startROSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkStopROSpec(final @Mocked StopROSpec request) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, LLRPException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(StopROSpecResponse.class, id, statusCode);

		StopROSpecResponse stopROSpecResponse = llrpService.stopROSpec(request);

		check(stopROSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, stopROSpecResponse.getStatus().getStatusCode());

	}

	@Test
	public void checkEnableROSpec(final @Mocked EnableROSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(EnableROSpecResponse.class, id, statusCode);

		EnableROSpecResponse enableROSpecResponse = llrpService.enableROSpec(request);

		check(enableROSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, enableROSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkDisableROSpec(final @Mocked DisableROSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(DisableROSpecResponse.class, id, statusCode);

		DisableROSpecResponse disableROSpecResponse = llrpService.disableROSpec(request);

		check(disableROSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, disableROSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkGetROSpecs(final @Mocked GetROSpecs request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(GetROSpecsResponse.class, id, statusCode);

		GetROSpecsResponse getROSpecsResponse = llrpService.getROSpecs(request);

		check(getROSpecsResponse, id, statusCode);

		Assert.assertEquals(statusCode, getROSpecsResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkAddAccessSpec(final @Mocked AddAccessSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(AddAccessSpecResponse.class, id, statusCode);

		AddAccessSpecResponse addAccessSpecResponse = llrpService.addAccessSpec(request);

		check(addAccessSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, addAccessSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkDeleteAccessSpec(final @Mocked DeleteAccessSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(DeleteAccessSpecResponse.class, id, statusCode);

		DeleteAccessSpecResponse deleteAccessSpecResponse = llrpService.deleteAccessSpec(request);

		check(deleteAccessSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, deleteAccessSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkEnableAccessSpec(final @Mocked EnableAccessSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(EnableAccessSpecResponse.class, id, statusCode);

		EnableAccessSpecResponse enableAccessSpecResponse = llrpService.enableAccessSpec(request);

		check(enableAccessSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, enableAccessSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkDisableAccessSpec(final @Mocked DisableAccessSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(DisableAccessSpecResponse.class, id, statusCode);

		DisableAccessSpecResponse disableAccessSpecResponse = llrpService.disableAccessSpec(request);

		check(disableAccessSpecResponse, id, statusCode);

		Assert.assertEquals(statusCode, disableAccessSpecResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkGetAccessSpecs(final @Mocked GetAccessSpecs request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(GetAccessSpecsResponse.class, id, statusCode);

		GetAccessSpecsResponse getAccessSpecsResponse = llrpService.getAccessSpecs(request);

		check(getAccessSpecsResponse, id, statusCode);

		Assert.assertEquals(statusCode, getAccessSpecsResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkROSpecHappyPath(final @Mocked AddROSpec request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(AddROSpecResponse.class, id, statusCode);

		AddROSpecResponse setProtocolVersionResponse = llrpService.addROSpec(request);

		check(setProtocolVersionResponse, id, statusCode);

		Assert.assertEquals(statusCode, setProtocolVersionResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkGetReaderConfig(final @Mocked GetReaderConfig request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(GetReaderConfigResponse.class, id, statusCode);

		GetReaderConfigResponse getReaderConfigResponse = llrpService.getReaderConfig(request);

		check(getReaderConfigResponse, id, statusCode);

		Assert.assertEquals(statusCode, getReaderConfigResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkSetReaderConfig(final @Mocked SetReaderConfig request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(SetReaderConfigResponse.class, id, statusCode);

		SetReaderConfigResponse setReaderConfigResponse = llrpService.setReaderConfig(request);

		check(setReaderConfigResponse, id, statusCode);

		Assert.assertEquals(statusCode, setReaderConfigResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkGetReaderCapabilitiesHappyPath(final @Mocked GetReaderCapabilities request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(GetReaderCapabilitiesResponse.class, id, statusCode);

		GetReaderCapabilitiesResponse setReaderCapabilities = llrpService.getReaderCapabilities(request);

		check(setReaderCapabilities, id, statusCode);

		Assert.assertEquals(statusCode, setReaderCapabilities.getStatus().getStatusCode());
	}

	@Test
	public void checkSetProtocolVersionHappyPath(final @Mocked SetProtocolVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(SetProtocolVersionResponse.class, id, statusCode);

		SetProtocolVersionResponse setProtocolVersionResponse = llrpService.setProtocolVersion(request);

		check(setProtocolVersionResponse, id, statusCode);

		Assert.assertEquals(statusCode, setProtocolVersionResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkCloseConnection(final @Mocked CloseConnection request) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;
		generateResponse(CloseConnectionResponse.class, id, statusCode);

		CloseConnectionResponse closeConnectionResponse = llrpService.closeConnection(request);

		check(closeConnectionResponse, id, statusCode);

		Assert.assertEquals(statusCode, closeConnectionResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkGetSupportedVersionHappyPath(final @Mocked GetSupportedVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		LLRPService llrpService = new LLRPService();
		long id = 4321;
		LLRPStatusCode statusCode = LLRPStatusCode.M_SUCCESS;

		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), statusCode, "TESTETST");
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, id);
		GetSupportedVersionResponse response = new GetSupportedVersionResponse(header, ProtocolVersion.LLRP_V1_1, ProtocolVersion.LLRP_V1_1, llrpStatus);
		final LLRPReturnContainerUtil<GetSupportedVersionResponse> containerUtil = new LLRPReturnContainerUtil<>();

		containerUtil.setValue(response);
		containerUtil.setTrue(true);

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<GetSupportedVersionResponse> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {

				return containerUtil;
			}
		};

		GetSupportedVersionResponse getSupportedVersionResponse = llrpService.getSupportedVersion(request);

		check(getSupportedVersionResponse, id, statusCode);

		Assert.assertEquals(statusCode, getSupportedVersionResponse.getStatus().getStatusCode());
	}

	@Test
	public void checkHandleAsyncLLRPMessageTimeout(final @Mocked GetSupportedVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "TESTETST");
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 1234);
		GetSupportedVersionResponse response = new GetSupportedVersionResponse(header, ProtocolVersion.LLRP_V1_1, ProtocolVersion.LLRP_V1_1, llrpStatus);
		final LLRPReturnContainerUtil<GetSupportedVersionResponse> containerUtil = new LLRPReturnContainerUtil<>();

		containerUtil.setValue(response);
		containerUtil.setTrue(false);

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<GetSupportedVersionResponse> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {

				return containerUtil;
			}
		};

		boolean errortimeout = false;

		try {
			llrpService.getSupportedVersion(request);
		} catch (LLRPTimeoutException llrpee) {
			errortimeout = true;
		}

		Assert.assertTrue(errortimeout);
	}

	@Test
	public void checkHandleAsyncLLRPMessageErrorMessage(final @Mocked GetSupportedVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_UNEXPECTED_MESSAGE, "TESTETST");
		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 1234);
		ErrorMessage response = new ErrorMessage(header, llrpStatus);
		final LLRPReturnContainerUtil<Message> containerUtil = new LLRPReturnContainerUtil<>();

		containerUtil.setValue(response);
		containerUtil.setTrue(true);

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<Message> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {
				return containerUtil;
			}
		};

		boolean errorOccured = false;

		try {
			llrpService.getSupportedVersion(request);
		} catch (LLRPErrorException llrpee) {
			errorOccured = true;
		}

		Assert.assertTrue(errorOccured);
	}

	@Test
	public void checkHandleAsyncLLRPMessageReturnsNull(final @Mocked GetSupportedVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		final LLRPReturnContainerUtil<GetSupportedVersionResponse> containerUtil = new LLRPReturnContainerUtil<>();

		containerUtil.setValue(null);
		containerUtil.setTrue(true);

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<GetSupportedVersionResponse> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {

				return containerUtil;
			}
		};

		GetSupportedVersionResponse getSupportedVersionResponse = llrpService.getSupportedVersion(request);

		Assert.assertNull(getSupportedVersionResponse);
	}

	@Test
	public void checkHandleAsyncLLRPMessageIOException(final @Mocked GetSupportedVersion request) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public LLRPReturnContainerUtil<GetSupportedVersionResponse> waitOnResponse(Invocation invocation, Message request, int timeout) throws IOException,
					InvalidMessageTypeException, InvalidParameterTypeException {

				throw new IOException();
			}
		};

		boolean errorOccured = false;

		try {
			llrpService.getSupportedVersion(request);
		} catch (LLRPException llrpe) {
			errorOccured = true;
		}

		Assert.assertTrue(errorOccured);
	}

	@Test
	public void checkHandleAsyncLLRPMessageGetter() throws IOException, InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		Assert.assertNotNull(llrpService.getKeepaliveEvent());
		Assert.assertNotNull(llrpService.getClientRequestOpEvent());
		Assert.assertNotNull(llrpService.getNoDataReceivedEvent());
		Assert.assertNotNull(llrpService.getReaderNotificationEvent());
		Assert.assertNotNull(llrpService.getROAccessReportEvent());
	}

	@Test
	public void checkSendMessages(final @Mocked GetReport getReport, final @Mocked EnableEventsAndReports enableEventsAndReports,
			final @Mocked ClientRequestOPResponse clientRequestOPResponse, final @Mocked KeepaliveAck keepaliveAck) throws IOException,
			InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		LLRPService llrpService = new LLRPService();

		new MockUp<LLRPMessageHandler>() {

			@SuppressWarnings("unused")
			@Mock
			public void sendMessage(Message message) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
				// Dummy
			}
		};

		llrpService.getReport(getReport);
		llrpService.enableEventsAndReports(enableEventsAndReports);
		llrpService.clientRequestOpResponse(clientRequestOPResponse);
		llrpService.keepaliveAck(keepaliveAck);
	}

	@Test
	public void checkROAccessReportEvent(final @Mocked LLRPEventHandler<LLRPEventArgs<ROAccessReport>> roAccessReportEvent,
			final @Mocked LLRPEventArgs<ROAccessReport> roAccessReport, final @Mocked LLRPEventArgs<ClientRequestOP> clientRequestOP,
			final @Mocked LLRPEventArgs<Keepalive> keepalive) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		final LLRPService llrpService = new LLRPService();

		llrpService.onROAccessReportEvent(roAccessReport);

		new Verifications() {
			{
				roAccessReportEvent.handleEvent(this.<LLRPService> withSameInstance(llrpService),
						this.<LLRPEventArgs<ROAccessReport>> withEqual(roAccessReport));
				times = 1;
			}
		};
	}

	@Test
	public void checkClientRequestOpEvent(final @Mocked LLRPEventHandler<LLRPEventArgs<ClientRequestOP>> clientRequestOPEvent,
			final @Mocked LLRPEventArgs<ClientRequestOP> clientRequestOP) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException,
			LLRPException {

		final LLRPService llrpService = new LLRPService();

		llrpService.onClientRequestOpEvent(clientRequestOP);

		new Verifications() {
			{
				clientRequestOPEvent.handleEvent(this.<LLRPService> withSameInstance(llrpService),
						this.<LLRPEventArgs<ClientRequestOP>> withSameInstance(clientRequestOP));
				times = 1;
			}
		};
	}

	@Test
	public void checkKeepaliveEvent(final @Mocked LLRPEventHandler<LLRPEventArgs<Keepalive>> keepaliveEvent, final @Mocked LLRPEventArgs<Keepalive> keepalive)
			throws IOException, InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		final LLRPService llrpService = new LLRPService();

		llrpService.onKeepaliveEvent(keepalive);
		new Verifications() {
			{
				keepaliveEvent.handleEvent(this.<LLRPService> withSameInstance(llrpService), this.<LLRPEventArgs<Keepalive>> withSameInstance(keepalive));
				times = 1;
			}
		};
	}

	@Test
	public void checkReaderNotificationEvent(final @Mocked LLRPEventHandler<LLRPEventArgs<ReaderEventNotification>> readerEventNotificationEvent,
			final @Mocked LLRPEventArgs<ReaderEventNotification> readerEventNotification) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, LLRPException {
		final LLRPService llrpService = new LLRPService();

		llrpService.onReaderNotificationEvent(readerEventNotification);
		new Verifications() {
			{
				readerEventNotificationEvent.handleEvent(this.<LLRPService> withSameInstance(llrpService),
						this.<LLRPEventArgs<ReaderEventNotification>> withSameInstance(readerEventNotification));
				times = 1;
			}
		};
	}

	@Test
	public void checkNoDataReceivedEvent(final @Mocked LLRPEventHandler<EventObject> eventObjectEvent, final @Mocked EventObject eventObject)
			throws IOException, InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		final LLRPService llrpService = new LLRPService();

		llrpService.onNoDataReceivedEvent(eventObject);

		new Verifications() {
			{
				eventObjectEvent.handleEvent(this.<LLRPService> withSameInstance(llrpService), this.<EventObject> withSameInstance(eventObject));
				times = 1;
			}
		};
	}

	@Test
	public void checkOpenAndCloseConnection(final @Mocked LLRPConnection llrpConnection, final @Mocked LLRPClient llrpClient)
			throws IOException, InvalidMessageTypeException, InvalidParameterTypeException, LLRPException {

		final LLRPService llrpService = new LLRPService();

		new NonStrictExpectations() {{
			llrpClient.openConnection(llrpConnection);
			
			result = true;
		}};
		
		boolean opened = llrpService.openConnection(llrpConnection);
		
		new Verifications() {{
			llrpClient.openConnection(this.withSameInstance(llrpConnection));
			times = 1;
		}};
		
		Assert.assertTrue(opened);
		
		llrpService.closeConnection();
	}
}