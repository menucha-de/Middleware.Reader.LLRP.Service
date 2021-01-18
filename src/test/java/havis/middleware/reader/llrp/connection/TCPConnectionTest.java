package havis.middleware.reader.llrp.connection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class TCPConnectionTest {
	@Test
	public void checkContructor() {
		String host = "10.10.10.10";
		int port = 8080;
		int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		Assert.assertEquals(timeout, connection.getTimeout());
		Assert.assertEquals(keepalive, connection.getKeepalive());
	}

	@Test
	public void checkConnectionSuccess(@Mocked final SocketChannel socketChannel, @Mocked final Socket socket) throws Exception {

		final String host = "10.10.10.10";
		final int port = 9090;
		final int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		new NonStrictExpectations() {
			{
				SocketChannel.open();
				result = socketChannel;

				socketChannel.socket();

				result = socket;
			}
		};

		connection.openConnection();

		new Verifications() {
			{
				socket.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
			}
		};

		Assert.assertEquals(true, connection.isConnected());

		connection.dispose();

		Assert.assertEquals(false, connection.isConnected());
	}

	@Test
	public void checkConnectionRetryOnce(@Mocked final SocketChannel socketChannel1, @Mocked final SocketChannel socketChannel2, @Mocked final Socket socket1, @Mocked final Socket socket2) throws Exception {

		final String host = "10.10.10.10";
		final int port = 9090;
		final int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		new StrictExpectations() {
			{
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
				
				SocketChannel.open();
				times = 1;
				result  = socketChannel2;
				
				socketChannel2.socket();
				times = 1;
				result = socket2;
				
				socket2.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				
				socketChannel2.close();
				times = 1;
			}
		};

		connection.openConnection();

		Assert.assertEquals(true, connection.isConnected());

		connection.dispose();

		Assert.assertEquals(false, connection.isConnected());
	}

	@Test
	public void checkConnectionRetryTwice(@Mocked final SocketChannel socketChannel1, @Mocked final SocketChannel socketChannel2, @Mocked final Socket socket1, @Mocked final Socket socket2) throws Exception {

		final String host = "10.10.10.10";
		final int port = 9090;
		final int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		new StrictExpectations() {
			{
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
				
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
				
				SocketChannel.open();
				times = 1;
				result  = socketChannel2;
				
				socketChannel2.socket();
				times = 1;
				result = socket2;
				
				socket2.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				
				socketChannel2.close();
				times = 1;
			}
		};

		connection.openConnection();

		Assert.assertEquals(true, connection.isConnected());

		connection.dispose();

		Assert.assertEquals(false, connection.isConnected());
	}

	@Test
	public void checkConnectionRetryFail(@Mocked final SocketChannel socketChannel1, @Mocked final SocketChannel socketChannel2, @Mocked final Socket socket1, @Mocked final Socket socket2) throws Exception {

		final String host = "10.10.10.10";
		final int port = 9090;
		final int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		new StrictExpectations() {
			{
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
				
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
				
				SocketChannel.open();
				times = 1;
				result  = socketChannel1;

				socketChannel1.socket();
				times = 1;
				result = socket1;
				
				socket1.connect(this.<InetSocketAddress> withEqual(new InetSocketAddress(host, port)), timeout);
				times = 1;
				result = new ConnectException("Connection refused");
			}
		};

		connection.openConnection();

		Assert.assertEquals(false, connection.isConnected());

		connection.dispose();

		Assert.assertEquals(false, connection.isConnected());
	}

	@Test
	public void checkOpenConnectionFail(@Mocked final SocketChannel socketChannel, @Mocked final Socket socket) throws Exception {

		final String host = "10.10.10.10";
		final int port = 8080;
		final int timeout = 10000;
		int keepalive = 60000;

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);

		new NonStrictExpectations() {
			{
				SocketChannel.open();
				result = new IOException();
			}
		};

		connection.openConnection();

		Assert.assertEquals(false, connection.isConnected());
	}

	@Test
	public void checkRetrievingMessage(final @Mocked SocketChannel socketChannel, @Mocked final Socket socket, final @Mocked ByteBuffer dst) throws Exception {
		final String host = "10.10.10.10";
		final int port = 8080;
		final int timeout = 10000;
		final int size = 10;

		int keepalive = 60000;

		new NonStrictExpectations() {
			{
				SocketChannel.open();
				result = socketChannel;

				socketChannel.socket();
				result = socket;

				socketChannel.read(dst);
				result = 10;
			}
		};

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);
		connection.openConnection();
		connection.retrieveMessage(size, keepalive);

		new Verifications() {
			{
				dst.flip();
				times = 1;
			}
		};
	}

	@Test
	public void checkRetrievingMessageFail(final @Mocked SocketChannel socketChannel, @Mocked final Socket socket, final @Mocked ByteBuffer dst)
			throws Exception {
		final String host = "10.10.10.10";
		final int port = 8080;
		final int timeout = 10000;
		final int size = 10;

		int keepalive = 60000;

		new NonStrictExpectations() {
			{
				SocketChannel.open();
				result = socketChannel;

				socketChannel.socket();
				result = socket;

				socketChannel.read(dst);
				result = -1;
			}
		};

		TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);
		connection.openConnection();
		boolean exception = false;

		try {
			connection.retrieveMessage(size, keepalive);
		} catch (IOException exc) {
			exception = true;
		}

		Assert.assertTrue(exception);

	}

	@Test
	public void checkSendMessage(final @Mocked SocketChannel socketChannel, @Mocked final Socket socket, final @Mocked ByteBuffer dst) throws Exception {
		final String host = "10.10.10.10";
		final int port = 8080;
		final int timeout = 10000;

		int keepalive = 60000;

		new NonStrictExpectations() {
			{
				SocketChannel.open();
				result = socketChannel;

				socketChannel.socket();
				result = socket;

				dst.flip();
				socketChannel.write(dst);
			}
		};

		final TCPConnection connection = new TCPConnection(host, port, timeout, keepalive);
		connection.openConnection();

		connection.sendMessage(dst);
		
		new Verifications() {
			{
				socketChannel.write(this.<ByteBuffer>withSameInstance(dst));
			}
		};
	}
}
