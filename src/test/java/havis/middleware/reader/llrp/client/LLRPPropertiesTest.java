package havis.middleware.reader.llrp.client;

import org.junit.Assert;
import org.junit.Test;

public class LLRPPropertiesTest {
	@Test
	public  void checkLLRPProperties() {
		LLRPProperties llrpProperties = new LLRPProperties();
		
		llrpProperties.setInventoryAttempts(2000);
		llrpProperties.setKeepalive(60000);
		
		Assert.assertEquals(2000, llrpProperties.getInventoryAttempts());
		Assert.assertEquals(60000, llrpProperties.getKeepalive());
	}
	
	@Test
	public  void checkLLRPPropertyName() {
		String inventoryAttempts = LLRPProperties.PropertyName.InventoryAttempts;
		String keepalive = LLRPProperties.PropertyName.Keepalive;
		
		Assert.assertEquals(LLRPProperties.PropertyName.InventoryAttempts, inventoryAttempts);
		Assert.assertEquals(LLRPProperties.PropertyName.Keepalive, keepalive);
	}
}
