package havis.middleware.reader.llrp.util;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public final class BitConverterTest {

	@Test
	public void checkConverter() {
		int lim = 257;
		byte[][] btsVector = new byte[lim][];

		for (int i = 0; i < lim; i++) {
			byte[] bts = ByteBuffer.allocate(4).putInt(i).array();

			Integer compVal = ByteBuffer.wrap(bts).getInt();

			Assert.assertEquals(i, compVal.intValue());

			btsVector[i] = bts;
		}

		for (int i = 0; i < lim; i++) {
			byte[] bts = btsVector[i];
			int value = BitConverter.toInt(bts);

			byte[] compBts = BitConverter.getBytes(value);

			Assert.assertArrayEquals(bts, compBts);
		}

		byte[] bt1 = BitConverter.getBytes(127);

		Assert.assertArrayEquals(new byte[] { 0x00, 0x00, 0x00, 0x7F }, bt1);
		
		byte[] bt2 = BitConverter.getBytes(128);

		Assert.assertArrayEquals(new byte[] { 0x00, 0x00, 0x00, (byte)(128 & 0xFF) }, bt2);
	}
}