package havis.middleware.reader.llrp.client;

import havis.llrpservice.data.message.GetSupportedVersion;
import havis.llrpservice.data.message.Message;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.MessageTypes.MessageType;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.parameter.serializer.InvalidParameterTypeException;
import havis.llrpservice.data.message.serializer.ByteBufferSerializer;
import havis.llrpservice.data.message.serializer.InvalidMessageTypeException;
import havis.llrpservice.data.message.serializer.InvalidProtocolVersionException;
import havis.middleware.reader.llrp.connection.TCPConnection;
import havis.middleware.reader.llrp.service.LLRPMessageHandler;

import java.io.IOException;
import java.nio.ByteBuffer;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Test;

public class LLRPClientTest {
	@Test
	public void checkOpenConnectionNotifyResponse(final @Mocked LLRPConnection llrpConnection, final @Mocked Message message,
			final @Mocked MessageHeader messageHeader, final @Mocked LLRPMessageHandler llrpMessageHandler, final @Mocked TCPConnection tcpConnection,
			final @Mocked ByteBufferSerializer bufferSerializer) throws InterruptedException, InvalidProtocolVersionException, InvalidMessageTypeException,
			IOException, InvalidParameterTypeException {

		LLRPClient llrpClient = new LLRPClient(llrpMessageHandler);

		new NonStrictExpectations() {
			{
				tcpConnection.isConnected();
				result = true;

				llrpConnection.getConnectionType();
				result = LLRPConnectionType.TCP;

				tcpConnection.openConnection();
				result = true;

				bufferSerializer.deserializeMessageHeader(null);

				messageHeader.getMessageLength();
				result = 11;

				tcpConnection.retrieveMessage(11 - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, anyInt);
				result = ByteBuffer.allocate(11);

				bufferSerializer.deserializeMessage(null, null);

				result = message;

				messageHeader.getMessageType();

				result = MessageType.ERROR_MESSAGE;
			}
		};

		llrpClient.openConnection(llrpConnection);

		Thread.sleep(20);
		llrpClient.closeConnection();

		new Verifications() {
			{
				tcpConnection.retrieveMessage(11 - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, anyInt);
				minTimes = 1;

				llrpMessageHandler.notifyResponse(message);
				minTimes = 1;
			}
		};
	}

	@Test
	public void checkOpenConnectionNotifyEvent(final @Mocked LLRPConnection llrpConnection, final @Mocked Message message,
			final @Mocked MessageHeader messageHeader, final @Mocked LLRPMessageHandler llrpMessageHandler, final @Mocked TCPConnection tcpConnection,
			final @Mocked ByteBufferSerializer bufferSerializer) throws InterruptedException, InvalidProtocolVersionException, InvalidMessageTypeException,
			IOException, InvalidParameterTypeException {

		LLRPClient llrpClient = new LLRPClient(llrpMessageHandler);

		new NonStrictExpectations() {
			{
				tcpConnection.isConnected();
				result = true;

				llrpConnection.getConnectionType();
				result = LLRPConnectionType.TCP;

				tcpConnection.openConnection();
				result = true;

				bufferSerializer.deserializeMessageHeader(null);

				messageHeader.getMessageLength();
				result = 11;

				tcpConnection.retrieveMessage(11 - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, anyInt);

				result = ByteBuffer.allocate(11);

				bufferSerializer.deserializeMessage(null, null);

				result = message;

				messageHeader.getMessageType();

				result = MessageType.KEEPALIVE;
			}
		};

		llrpClient.openConnection(llrpConnection);

		Thread.sleep(20);
		llrpClient.closeConnection();

		new Verifications() {
			{
				tcpConnection.retrieveMessage(11 - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, anyInt);
				minTimes = 1;

				llrpMessageHandler.notifyEvent(message);
				minTimes = 1;
			}
		};

		llrpClient.dispose();
	}

	@Test
	public void checkOpenConnectionExceptionThrown(final @Mocked LLRPConnection llrpConnection, final @Mocked Message message,
			final @Mocked MessageHeader messageHeader, final @Mocked LLRPMessageHandler llrpMessageHandler, final @Mocked TCPConnection tcpConnection,
			final @Mocked ByteBufferSerializer bufferSerializer) throws InterruptedException, InvalidProtocolVersionException, InvalidMessageTypeException,
			IOException, InvalidParameterTypeException {

		LLRPClient llrpClient = new LLRPClient(llrpMessageHandler);

		new NonStrictExpectations() {
			{
				tcpConnection.isConnected();
				result = true;

				llrpConnection.getConnectionType();
				result = LLRPConnectionType.TCP;

				tcpConnection.openConnection();
				result = true;

				bufferSerializer.deserializeMessageHeader(null);

				messageHeader.getMessageLength();
				result = 11;

				tcpConnection.retrieveMessage(11 - ByteBufferSerializer.MESSAGE_HEADER_LENGTH, anyInt);

				result = new Exception("This is a test!");
			}
		};

		llrpClient.openConnection(llrpConnection);

		Thread.sleep(50);
		llrpClient.closeConnection();

		new Verifications() {
			{
				llrpMessageHandler.notifyNoDataReceived();
				times = 1;
			}
		};

		llrpClient.dispose();
	}

	@Test
	public void checkSendMessage(final @Mocked TCPConnection tcpConnection, final @Mocked LLRPMessageHandler llrpMessageHandler, final @Mocked ByteBufferSerializer serializer) throws IOException, InvalidMessageTypeException, InvalidParameterTypeException {
		LLRPClient llrpClient = new LLRPClient(llrpMessageHandler);

		MessageHeader header = new MessageHeader((byte) 0, ProtocolVersion.LLRP_V1_1, 7654);
		final GetSupportedVersion message = new GetSupportedVersion(header);
		llrpClient.connection = tcpConnection;

		llrpClient.sendMessage(message);

		new Verifications() {
			{
				tcpConnection.sendMessage(this.<ByteBuffer>withNotNull());
				minTimes = 1;
			}
		};
	}
}