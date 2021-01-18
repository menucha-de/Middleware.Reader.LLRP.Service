package havis.middleware.reader.llrp.util;

import org.junit.Assert;
import org.junit.Test;

public class LLRPReturnContainerTest {
	@Test
	public void checkContainer() {
		Integer value = new Integer(10);
		Boolean iT = new Boolean(true);
		
		LLRPReturnContainerUtil<Integer> containerUtil = new LLRPReturnContainerUtil<>();
		
		containerUtil.setTrue(iT);
		containerUtil.setValue(value);
		
		Assert.assertEquals(value, containerUtil.getValue());
		Assert.assertEquals(iT, containerUtil.isTrue());
	}
}