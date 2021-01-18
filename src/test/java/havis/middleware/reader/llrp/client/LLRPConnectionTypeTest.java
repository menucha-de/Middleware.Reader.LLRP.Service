package havis.middleware.reader.llrp.client;

import org.junit.Assert;
import org.junit.Test;

public class LLRPConnectionTypeTest {
	@Test
	public void checkLLRPConnectionType() {
		LLRPConnectionType t = LLRPConnectionType.TCP;
		
		Assert.assertEquals(LLRPConnectionType.TCP, t);
	}
}
