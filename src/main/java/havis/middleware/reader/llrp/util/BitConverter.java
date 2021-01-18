package havis.middleware.reader.llrp.util;

import havis.llrpservice.data.message.parameter.serializer.ByteBufferSerializer;

import java.nio.ByteBuffer;
import java.util.BitSet;

public final class BitConverter {

	private BitConverter() {
	}

	public static byte[] getBytes(int x) {
		return ByteBuffer.allocate(4).putInt(x).array();
	}

	public static int toInt(byte[] b) {
		return ByteBuffer.wrap(b).getInt();
	}

	public static BitSet toBitSet(byte[] bytes) {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			data[i] = ByteBufferSerializer.reverseBits(bytes[i]);
		}
		return BitSet.valueOf(data);
	}

	public static byte[] fromBitSet(BitSet data, int bitCount) {
		int length = bitCount / 8;
		if (bitCount % 8 != 0) {
			length++;
		}
		byte[] invertedBytes = data.toByteArray();
		byte[] fullBytes = new byte[length];
		for (int i = 0; i < invertedBytes.length; i++) {
			fullBytes[i] = ByteBufferSerializer.reverseBits(invertedBytes[i]);
		}
		return fullBytes;
	}
}