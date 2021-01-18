package havis.middleware.reader.llrp.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IDGeneratorTest {

	@Test
	public void checkIndex() throws InterruptedException {
		final List<Long> list = new ArrayList<>();
		List<Long> compList = new ArrayList<>();

		Runnable run1 = new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < 30; i++) {
					synchronized (list) {
						list.add(IDGenerator.getUniqueMessageID());
					}
				}
			}
		};

		Runnable run2 = new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < 30; i++) {
					synchronized (list) {
						list.add(IDGenerator.getUniqueMessageID());
					}
				}
			}
		};

		Thread th1 = new Thread(run1);
		Thread th2 = new Thread(run2);

		th1.start();
		th2.start();

		th1.join();
		th2.join();

		for (long i = 1; i <= 60; i++) {
			compList.add(i);
		}

		Assert.assertEquals(compList.size(), list.size());
		Assert.assertArrayEquals(compList.toArray(), list.toArray());
	}
}