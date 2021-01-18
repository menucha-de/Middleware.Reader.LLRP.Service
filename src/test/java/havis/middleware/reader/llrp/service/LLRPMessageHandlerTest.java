package havis.middleware.reader.llrp.service;

import havis.llrpservice.data.message.ClientRequestOP;
import havis.llrpservice.data.message.GetSupportedVersion;
import havis.llrpservice.data.message.GetSupportedVersionResponse;
import havis.llrpservice.data.message.Keepalive;
import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.ROAccessReport;
import havis.llrpservice.data.message.ReaderEventNotification;
import havis.llrpservice.data.message.parameter.LLRPStatus;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.ReaderEventNotificationData;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.llrpservice.data.message.parameter.TagReportData;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.middleware.reader.llrp.client.LLRPClient;
import havis.middleware.reader.llrp.service.event.LLRPEventArgs;
import havis.middleware.reader.llrp.util.LLRPReturnContainerUtil;

import java.io.IOException;
import java.util.EventObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class LLRPMessageHandlerTest {

	@Test
	public void checkSendMessage(final @Mocked LLRPService service, final @Mocked Message message, final @Mocked LLRPClient client) throws IOException,
			InvalidMessageTypeException, InvalidParameterTypeException {
		LLRPMessageHandler llrpMessageHandler = new LLRPMessageHandler(service);
		llrpMessageHandler.setClient(client);

		new NonStrictExpectations() {
			{
				client.sendMessage(message);
			}
		};

		llrpMessageHandler.sendMessage(message);

		new Verifications() {
			{
				client.sendMessage(this.<Message> withEqual(message));
			}
		};
	}

	@Test
	public void checkWaitOnResponse(final @Mocked LLRPService service, final @Mocked GetSupportedVersion message, final @Mocked LLRPClient client,
			final @Mocked LLRPSyncObject llrpSyncObject) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		LLRPMessageHandler llrpMessageHandler = new LLRPMessageHandler(service);
		llrpMessageHandler.setClient(client);

		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
		MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);
		GetSupportedVersionResponse response = new GetSupportedVersionResponse(messageHeader, ProtocolVersion.LLRP_V1_1, ProtocolVersion.LLRP_V1_1, llrpStatus);
		final LLRPReturnContainerUtil<Message> res = new LLRPReturnContainerUtil<>();

		res.setValue(response);
		res.setTrue(true);

		new NonStrictExpectations() {
			{
				client.sendMessage(message);

				llrpSyncObject.await();
				result = res;
			}
		};

		final LLRPReturnContainerUtil<Message> containerUtil = llrpMessageHandler.waitOnResponse(message, 1000);

		Assert.assertNotNull(containerUtil);
		Assert.assertEquals(true, containerUtil.isTrue());
		Assert.assertTrue(containerUtil.getValue() instanceof GetSupportedVersionResponse);
		Assert.assertSame(containerUtil, res);
	}

	@Test
	public void checkNotifyResponse(final @Mocked LLRPService service, final @Mocked LLRPClient client) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, InterruptedException {
		final LLRPMessageHandler llrpMessageHandler = new LLRPMessageHandler(service);
		llrpMessageHandler.setClient(client);

		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.M_SUCCESS, "");
		MessageHeader messageHeader = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);
		GetSupportedVersionResponse response = new GetSupportedVersionResponse(messageHeader, ProtocolVersion.LLRP_V1_1, ProtocolVersion.LLRP_V1_1, llrpStatus);
		final GetSupportedVersion request = new GetSupportedVersion(messageHeader);
		final LLRPReturnContainerUtil<Message> testResponse = new LLRPReturnContainerUtil<>();
		final CountDownLatch ready = new CountDownLatch(1);

		Thread waitingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				ready.countDown();

				try {
					LLRPReturnContainerUtil<Message> tmpResponse = llrpMessageHandler.waitOnResponse(request, 5000);
					testResponse.setTrue(tmpResponse.isTrue());
					testResponse.setValue(tmpResponse.getValue());

				} catch (IOException | InvalidMessageTypeException | InvalidParameterTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		waitingThread.start();

		boolean success = false;

		try {
			Assert.assertTrue("Thread is ready", ready.await(100, TimeUnit.MILLISECONDS));

			// thread is waiting now, let it do that for some time
			Thread.sleep(50);
			Assert.assertTrue("Thread is waiting", waitingThread.isAlive());

			llrpMessageHandler.notifyResponse(response);

			// wait for thread to finish
			waitingThread.join(100);

			Assert.assertFalse("Thread is not waiting", waitingThread.isAlive());

			success = true;

			Assert.assertEquals(testResponse.getValue(), response);

		} finally {
			if (!success) {
				// make sure the thread is stopped in case of error
				waitingThread.interrupt();
			}
		}
	}

	@Test
	public void checkNotifyEvent(final @Mocked LLRPService service, final @Mocked LLRPClient client, final @Mocked TagReportData tagReportData,
			final @Mocked ReaderEventNotificationData readerEventNotificationData) throws IOException, InvalidMessageTypeException,
			InvalidParameterTypeException, InterruptedException {
		final LLRPMessageHandler llrpMessageHandler = new LLRPMessageHandler(service);
		llrpMessageHandler.setClient(client);

		MessageHeader messageHeaderROAccessReport = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);
		MessageHeader messageHeaderClientRequestOP = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);
		MessageHeader messageHeaderKeepalive = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);
		MessageHeader messageHeaderReaderEventNotification = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 4321);

		final ROAccessReport requestROAccessReport = new ROAccessReport(messageHeaderROAccessReport);
		final ClientRequestOP requestClientRequestOP = new ClientRequestOP(messageHeaderClientRequestOP, tagReportData);
		final Keepalive requestKeepalive = new Keepalive(messageHeaderKeepalive);
		final ReaderEventNotification requestReaderEventNotification = new ReaderEventNotification(messageHeaderReaderEventNotification, readerEventNotificationData);

		llrpMessageHandler.notifyEvent(requestROAccessReport);
		llrpMessageHandler.notifyEvent(requestClientRequestOP);
		llrpMessageHandler.notifyEvent(requestKeepalive);
		llrpMessageHandler.notifyEvent(requestReaderEventNotification);

		llrpMessageHandler.notifyNoDataReceived();

		new Verifications() {
			{
				service.onROAccessReportEvent(withAny(new LLRPEventArgs<ROAccessReport>(requestROAccessReport)));
				times = 1;

				service.onClientRequestOpEvent(withAny(new LLRPEventArgs<ClientRequestOP>(requestClientRequestOP)));
				times = 1;

				service.onKeepaliveEvent(withAny(new LLRPEventArgs<Keepalive>(requestKeepalive)));
				times = 1;

				service.onReaderNotificationEvent(withAny(new LLRPEventArgs<ReaderEventNotification>(requestReaderEventNotification)));
				times = 1;

				service.onNoDataReceivedEvent(withAny(new EventObject(llrpMessageHandler)));
				times = 1;
			}
		};
	}
}
